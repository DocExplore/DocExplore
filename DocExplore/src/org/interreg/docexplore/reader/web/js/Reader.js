var Reader = {};

Reader.currentPage = -4;
Reader.requestPage = -3;
Reader.dbg = {};
Reader.width = 0;
Reader.height = 0;
Reader.$roiArea = null;
Reader.roiAlpha = 0;
Reader.ecoMode = false;
Reader.pageDepth = .025;
Reader.useShadows = false;
Reader.bookModel = null;

var Detector = {

    canvas: !! window.CanvasRenderingContext2D,
    webgl: ( function () { try { var canvas = document.createElement( 'canvas' ); return !! window.WebGLRenderingContext && ( canvas.getContext( 'webgl' ) || canvas.getContext( 'experimental-webgl' ) ); } catch( e ) { return false; } } )(),
    workers: !! window.Worker,
    fileapi: window.File && window.FileReader && window.FileList && window.Blob,

    getWebGLErrorMessage: function () {

        var element = document.createElement( 'div' );
        element.id = 'webgl-error-message';
        element.style.fontFamily = 'monospace';
        element.style.fontSize = '13px';
        element.style.fontWeight = 'normal';
        element.style.textAlign = 'center';
        element.style.background = '#fff';
        element.style.color = '#000';
        element.style.padding = '1.5em';
        element.style.width = '400px';
        element.style.margin = '5em auto 0';

        if ( ! this.webgl ) {

            element.innerHTML = window.WebGLRenderingContext ? [
                'Your graphics card does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation" style="color:#000">WebGL</a>.<br />',
                'Find out how to get it <a href="http://get.webgl.org/" style="color:#000">here</a>.'
            ].join( '\n' ) : [
                'Your browser does not seem to support <a href="http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation" style="color:#000">WebGL</a>.<br/>',
                'Find out how to get it <a href="http://get.webgl.org/" style="color:#000">here</a>.'
            ].join( '\n' );

        }

        return element;

    },

    addGetWebGLMessage: function ( parameters ) {

        var parent, id, element;

        parameters = parameters || {};

        parent = parameters.parent !== undefined ? parameters.parent : document.body;
        id = parameters.id !== undefined ? parameters.id : 'oldie';

        element = Detector.getWebGLErrorMessage();
        element.id = id;

        parent.appendChild( element );

    }

};

Reader.init = function()
{
	var $container = $('#container');
	Reader.width = $container.width();
	Reader.height = $container.height();
	var elem = document.getElementById('container');
	elem.addEventListener("touchstart", function(event) {event.preventDefault(); Input.htmlMouseDown(event.targetTouches[0].pageX, event.targetTouches[0].pageY);}, false);
	elem.addEventListener("touchmove", function(event) {event.preventDefault(); Input.htmlMouseMove(event.targetTouches[0].pageX, event.targetTouches[0].pageY);}, false);
	elem.addEventListener("touchend", function(event) {event.preventDefault(); Input.htmlMouseUp();}, false);
	
	Reader.trace("canvas size: "+Reader.width+", "+Reader.height);
	Reader.$roiArea = $("#roiContainer");
	
	Reader.$roiArea.css("position", "fixed");
	Reader.$roiArea.offset({left: $container.offset().left+.5*$container.width(), top: $container.offset().top});
	Reader.$roiArea.css("height", Reader.height);
	
	if (Detector.webgl)
	{
        Reader.renderer = new THREE.WebGLRenderer();
        if (Reader.useShadows)
        {
        	Reader.renderer.shadowMap.enabled = true;
        	Reader.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        }
	}
    else
    {
        Reader.renderer = new THREE.CanvasRenderer();
        Reader.renderer.sortObjects = false;
        Reader.renderer.sortElements = false;
        Reader.ecoMode = true;
    }
	Reader.renderer.setSize(Reader.width, Reader.height);
	Reader.renderer.autoClear = true;
    Reader.renderer.autoClearColor = true;
    Reader.renderer.setClearColor(new THREE.Color(0xf0f0f0));
	$container.append(Reader.renderer.domElement);
	
	var distFactor = 2*Spec.aspect*Reader.height/Reader.width;
    Camera.init(0, 0, 5.2*Math.max(1, distFactor));
    Reader.scene = new THREE.Scene();
    Reader.heightBound = Reader.width*1./Reader.height > 2*Spec.aspect;
    
    var pageHeight = Spec.pageHeight;
    var pageWidth = pageHeight*Spec.aspect;
	
	// and the camera
	Reader.scene.add(Camera.camera);
	
	// create a point light
	var pointLight = new THREE.DirectionalLight(0xDFDFDF);
	pointLight.castShadow = Reader.useShadows;
	if (Reader.useShadows)
	{
		//pointLight.shadow.bias = .6;
		pointLight.shadow.mapSize.width = 1024;
		pointLight.shadow.mapSize.height = 1024;
		pointLight.shadow.camera.left = -3;
		pointLight.shadow.camera.right = 3;
		pointLight.shadow.camera.top = 3;
		pointLight.shadow.camera.bottom = -3;
		pointLight.shadow.camera.near = 1;
		pointLight.shadow.camera.far = 30;
		//Reader.scene.add(new THREE.AmbientLight(0x404040));
	}
	// set its position
	pointLight.position.x = 1;
	pointLight.position.y = 2;
	pointLight.position.z = 13;
	
	// add to the scene
	Reader.scene.add(pointLight);
	Reader.scene.add(new THREE.AmbientLight(0x202020));
	
	Reader.fcnt = 0;
	
	Input.listeners.push(Reader);
	
	Reader.bookModel = new BookModel(pageWidth, pageHeight);
	
	Reader.scene.add(Reader.bookModel.model);
	Reader.scene.add(Reader.bookModel.page.model);
	
	document.getElementById('slider').max = Spec.pages.length;
	
	TexLoader.refresh();
	Reader.trace("init OK");
}

if (!window.requestAnimationFrame)
{
	window.requestAnimationFrame = (function()
	{
		return window.webkitRequestAnimationFrame ||
			window.mozRequestAnimationFrame ||
			window.oRequestAnimationFrame ||
			window.msRequestAnimationFrame ||
			function(callback, element) {window.setTimeout(callback, 1000/60);};
	})();
}

Reader.tempModels = [];
Reader.leftPageIndex = 0;
Reader.rightPageIndex = 0;
Reader.handWasActive = false;
Reader.forceModelSynchro = false;
Reader.regionsAreShown = true;
Reader.render = function()
{
	Reader.bookModel.update();
	
	if (Reader.zoomed)
	{
		if (Reader.zoomIn) Camera.setDiffPos(0, 0, -.1);
		if (Reader.zoomOut) Camera.setDiffPos(0, 0, .1);
	}
	if (Hand.active)
		Hand.update();
	Camera.update();
	
	Reader.fcnt += .03;
	var opacity = Reader.$roiArea.css("opacity")*1+.1*(Reader.roiAlpha-Reader.$roiArea.css("opacity"));
	Reader.$roiArea.css("opacity", opacity);
	if (opacity > .05)
		Reader.$roiArea.css("z-index", 1);
	else Reader.$roiArea.css("z-index", -1);
	
	var nextPage = Reader.requestPage;
	if (nextPage != Reader.currentPage || Reader.handWasActive != Hand.active || Reader.forceModelSynchro)
	{
		Reader.setSelectedRegion(null);
		Reader.clearModels();
		
		Reader.currentPage = nextPage;
		if (Reader.currentPage < -3)
			Reader.currentPage = -3;
		if (Reader.currentPage > Spec.pages.length+1)
			Reader.currentPage = Spec.pages.length+1;
		document.getElementById('slider').value=Reader.currentPage+1;
        document.getElementById('sliderVal').value=formatVal(Reader.currentPage+1);
		Reader.leftPageIndex = Reader.currentPage;
		Reader.rightPageIndex = Reader.currentPage+1+(Hand.active ? 2 : 0);
		if (Reader.leftPageIndex < 0 || Reader.leftPageIndex > Spec.pages.length-1)
			Reader.leftPageIndex = -1;
		if (Reader.rightPageIndex < 0 || Reader.rightPageIndex >= Spec.pages.length-1)
			Reader.rightPageIndex = -1;
		
//		if (Hand.active)
//		{
//			Reader.bookModel.page.setFrontTex(Spec.pages[Reader.leftPageIndex+1].tex);
//			Reader.bookModel.page.setBackTex(Spec.pages[Reader.leftPageIndex+2].tex);
//		}
		
//		Spec.refreshTextures(Reader.currentPage, 5);
		if (Reader.leftPageIndex >= 0)
		{
			var page = Spec.pages[Reader.leftPageIndex];
//			Reader.bookModel.setLeftTex(page.tex);
			for (var i=0;i<page.regions.length;i++)
			{
				var region = page.regions[i];
				Region.buildRegionMesh(region);
				Reader.tempModels[Reader.tempModels.length] = region.line;
				Reader.scene.add(region.line);
				region.line.visible = Reader.regionsAreShown;
			}
		}
		
		if (Reader.rightPageIndex >= 0)
		{
			var page = Spec.pages[Reader.rightPageIndex];
//			Reader.bookModel.setRightTex(page.tex);
			for (var i=0;i<page.regions.length;i++)
			{
				var region = page.regions[i];
				Region.buildRegionMesh(region);
				Reader.tempModels[Reader.tempModels.length] = region.line;
				Reader.scene.add(region.line);
				region.line.visible = Reader.regionsAreShown;
			}
		}
		TexLoader.refresh();
	}
	
	var shouldRegionsBeShown = !Hand.active && Reader.currentPage > -3 && Reader.currentPage < Spec.pages.length+1;
	if (shouldRegionsBeShown != Reader.regionsAreShown)
	{
		if (shouldRegionsBeShown) Reader.showRegions();
		else Reader.hideRegions();
	}
	
	Reader.handWasActive = Hand.active;
	Reader.forceModelSynchro = false;
	
	Reader.renderer.render(Reader.scene, Camera.camera);
	window.requestAnimationFrame(Reader.render);
}
Reader.clearModels = function()
{
	for (i=0;i<Reader.tempModels.length;i++)
		Reader.scene.remove(Reader.tempModels[i]);
	Reader.tempModels = [];
}
Reader.showRegions = function()
{
	for (i=0;i<Reader.tempModels.length;i++)
		Reader.tempModels[i].visible = true;
	Reader.regionsAreShown = true;
}
Reader.hideRegions = function()
{
	for (i=0;i<Reader.tempModels.length;i++)
		Reader.tempModels[i].visible = false;
	Reader.regionsAreShown = false;
}

Reader.start = function(xml)
{
	Reader.dbg = document.getElementById("dbgArea");
	
	if (!bookBasePath.endsWith("/"))
		bookBasePath = bookBasePath+"/";
	Spec.init(bookBasePath, xml);
	
	Reader.init();
	Reader.render();
}

Reader.selectedRegion = null;
Reader.onClick = function(x, y)
{
	if (!Reader.zoomed)
	{
		var region = null;
		if (Reader.selectedRegion == null)
			region = Region.getRegionAt(x, y);
		Reader.setSelectedRegion(region);
	}
}
Reader.setSelectedRegion = function(region)
{
	if (region != Reader.selectedRegion)
	{
		if (Reader.selectedRegion != null)
			Reader.selectedRegion.line.material = Spec.roiMaterial;
		if (region != null)
			region.line.material = Spec.roiSelectedMaterial;
		Reader.selectedRegion = region;
		
		if (Reader.selectedRegion != null)
		{
			var info = "";
			for (var i=0;i<Reader.selectedRegion.infos.length;i++)
				info += Region.scaleInfo(Reader.selectedRegion.infos[i], Reader.$roiArea.width()/512.);
			Reader.roiAlpha = 1;
			var d = document.createElement("div");
			d.innerHTML = info;
			Reader.$roiArea.html(d.textContent);
			Camera.setPosToRegion(Reader.selectedRegion);
			$('#zoom').css('display', 'none');
			$('#back').css('display', 'inline');
			$('#prev').css('display', 'none');
			$('#next').css('display', 'none');
			$('#sliderDiv').css('display', 'none');
		}
		else
		{
			Camera.setDefaultPos();
			Reader.roiAlpha = 0;
			$('#zoom').css('display', 'inline');
			$('#back').css('display', 'none');
			$('#prev').css('display', 'inline');
			$('#next').css('display', 'inline');
			$('#sliderDiv').css('display', 'inline');
			var videos = document.getElementsByTagName("video");
			for (var i=0;i<videos.length;i++)
				videos[i].pause();
		}
	}
}
Reader.back = function()
{
	if (Reader.zoomed)
		Reader.zoom();
	else Reader.setSelectedRegion(null);
}

Reader.zoomed = false;
Reader.zoomIn = false;
Reader.zoomOut = false;
Reader.zoom = function()
{
	if (Reader.selectedRegion != null)
		return;
	
	if (Reader.zoomed)
	{
		Reader.zoomed = false;
		Reader.forceModelSynchro = true;
		Camera.setDefaultPos();
		$('#zoomin').css('display', 'none');
		$('#zoomout').css('display', 'none');
		$('#back').css('display', 'none');
		$('#sliderDiv').css('display', 'inline');
		$('#prev').css('display', 'inline');
		$('#next').css('display', 'inline');
		$('#zoom').css('display', 'inline');
	}
	else
	{
		Reader.zoomed = true;
		Reader.clearModels();
		Camera.setDiffPos(0, 0, -2);
		$('#zoomin').css('display', 'inline');
		$('#zoomout').css('display', 'inline');
		$('#back').css('display', 'inline');
		$('#sliderDiv').css('display', 'none');
		$('#prev').css('display', 'none');
		$('#next').css('display', 'none');
		$('#zoom').css('display', 'none');
	}
}
Reader.zoomBy = function(amount)
{
	if (!Reader.zoomed)
		Reader.zoom();
	else if (Reader.zoomed && Camera.unzoomed() && amount > .1)
	{
		Reader.zoom();
		Input.waitForNextPinch = true;
	}
	
	if (Reader.zoomed)
	{
		Camera.setDiffPos(0, 0, amount);
	}
}

Reader.quickNext = function()
{
	Reader.requestPage = Reader.currentPage+2;
}
Reader.quickPrev = function()
{
	Reader.requestPage = Reader.currentPage-2;
}
Reader.quickJump = function(page)
{
	Reader.requestPage = page;
}

Reader.trace = function(text)
{
	Reader.dbg.innerHTML = text+"\n"+Reader.dbg.innerHTML;
}

Reader.onGrab = function(x, y) {Hand.grab(x, y);}
Reader.onDrag = function(x, y) {Hand.drag(x, y);}
Reader.onDrop = function(x, y) {Hand.drop(x, y);}
