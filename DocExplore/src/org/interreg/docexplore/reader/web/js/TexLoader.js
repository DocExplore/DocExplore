var TexLoader = {};
TexLoader.loader = new THREE.TextureLoader();
TexLoader.spread = 3;
TexLoader.max = 3;
TexLoader.images = {};
TexLoader.queued = 0;

TexLoader.onReq = function()
{
	TexLoader.queued++;
	document.getElementById("loadingSpan").innerHTML = "loading ("+TexLoader.queued+")";
}
TexLoader.onRes = function()
{
	TexLoader.queued--;
	document.getElementById("loadingSpan").innerHTML = TexLoader.queued == 0 ? "" : "loading ("+TexLoader.queued+")";
}

function genericOnError(v) {TexLoader.onRes();}

function buildOnSuccess(pageImage) {return function(image) {TexLoader.onRes(); pageImage.tex = image; pageImage.loading = false; TexLoader.refresh();};}
function buildOnTSuccess(pageImage) {return function(image) {TexLoader.onRes(); pageImage.ttex = image; pageImage.tloading = false; TexLoader.refresh();};}
function buildOnError(pageImage) {return function(v) {TexLoader.onRes(); pageImage.tex = null; pageImage.loading = false; TexLoader.refresh();};}
function buildOnTError(pageImage) {return function(v) {TexLoader.onRes(); pageImage.ttex = null; pageImage.tloading = false; TexLoader.refresh();};}

function PageImage(page, path, tpath)
{
	this.page = page;
	this.tex = null;
	this.ttex = null;
	this.loading = true;
	this.tloading = tpath !== null;
	
	TexLoader.loader.load(path, 
		buildOnSuccess(this),
		function(v) {},
		buildOnError(this)
	);
	TexLoader.onReq();
	
	if (tpath !== null)
	{
		TexLoader.loader.load(tpath, 
			buildOnTSuccess(this),
			function(v) {},
			buildOnTError(this)
		);
		TexLoader.onReq();
	}
}

TexLoader.refresh = function()
{
	var from = Math.max(0, Reader.currentPage-TexLoader.spread);
	var to = Math.min(Spec.pages.length-1, Reader.currentPage+TexLoader.spread+(Hand.active ? 2 : 0));
	
	for (var i=from;i<=to;i++)
	{
		if (!("p"+i in TexLoader.images) && TexLoader.queued < TexLoader.max)
			TexLoader.images["p"+i] = new PageImage(i, Spec.pages[i].path, Spec.pages[i].tpath);
		else
		{
			var pi = "p"+i in TexLoader.images ? TexLoader.images["p"+i] : null;
			var tex = pi !== null && !pi.loading && pi.tex !== null ? pi.tex : Spec.loadingTex;
			var ttex = pi !== null && !pi.tloading && pi.ttex !== null ? pi.ttex : Spec.loadingTex;
			
			if (i == Reader.leftPageIndex) Reader.bookModel.setLeftTex(tex);
			else if (i == Reader.rightPageIndex) Reader.bookModel.setRightTex(tex);
			else if (i == Reader.leftPageIndex+1 && Hand.active) Reader.bookModel.page.setFrontTex(Spec.pages[i].tpath !== null ? ttex : tex);
			else if (i == Reader.leftPageIndex+2 && Hand.active) Reader.bookModel.page.setBackTex(Spec.pages[i].tpath !== null ? ttex : tex);
		}
	}
	
	for (prop in TexLoader.images)
		if (!TexLoader.images[prop].loading && (TexLoader.images[prop].page < from || TexLoader.images[prop].page > to))
		{
			if (TexLoader.images[prop].tex != null)
				TexLoader.images[prop].tex.dispose();
			if (TexLoader.images[prop].ttex != null)
				TexLoader.images[prop].ttex.dispose();
			delete TexLoader.images[prop];
		}
	//console.log(Object.keys(TexLoader.images).length);
}

function buildOnSideSuccess(stack) {return function(tex) {TexLoader.onRes(); stack.stackSideMesh.material.color = new THREE.Color(0xffffff); stack.stackSideMesh.material.map = tex; stack.stackSideMesh.material.needsUpdate = true;};}
TexLoader.loadSideTex = function(stack)
{
	var path = stack.left ? Spec.leftSide : Spec.rightSide;
	if (path !== null)
	{
		TexLoader.loader.load(path, 
			buildOnSideSuccess(stack),
			function(v) {},
			genericOnError
		);
		TexLoader.onReq();
	}
}

function buildOnOuterCoverSuccess(cover) {return function(tex) {TexLoader.onRes(); cover.meshes[cover.outer].material.map = tex; cover.meshes[cover.outer].material.needsUpdate = true;};}
function buildOnInnerCoverSuccess(cover) {return function(tex) {TexLoader.onRes(); cover.meshes[cover.inner].material.map = tex; cover.meshes[cover.inner].material.needsUpdate = true;};}
TexLoader.loadCoverTex = function(cover)
{
	TexLoader.loader.load(Spec.cover, 
		buildOnOuterCoverSuccess(cover),
		function(v) {},
		genericOnError
	);
	TexLoader.onReq();
	
	TexLoader.loader.load(Spec.innerCover, 
		buildOnInnerCoverSuccess(cover),
		function(v) {},
		genericOnError
	);
	TexLoader.onReq();
}
