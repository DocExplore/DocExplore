var Reader = {};

Reader.leftPage = {};
Reader.rightPage = {};
Reader.currentPage = -2;
Reader.requestPage = -1;
Reader.dbg = {};
Reader.width = 0;
Reader.height = 0;
Reader.$roiArea = null;
Reader.roiAlpha = 0;

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
	//$container.height(Reader.width/2.5);
	//$container.height($container.height()-70);
	Reader.height = $container.height();
//	$container.bind("touchstart", function(event) {Input.htmlMouseDown(event.pageX, event.pageY);});
//	$container.bind("touchmove", function(event) {Input.htmlMouseMove(event.pageX, event.pageY);});
//	$container.bind("touchend", function(event) {Input.htmlMouseUp(event.pageX, event.pageY);});
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
        Reader.renderer = new THREE.WebGLRenderer();
    else
    {
        Reader.renderer = new THREE.CanvasRenderer();
        Reader.renderer.sortObjects = false;
        Reader.renderer.sortElements = false;
    }
	Reader.renderer.setSize(Reader.width, Reader.height);
	Reader.renderer.autoClear = true;
    Reader.renderer.autoClearColor = true;
    Reader.renderer.setClearColorHex(0xffffff);
	$container.append(Reader.renderer.domElement);
	
	Camera.init(0, 0, 6);
	Reader.scene = new THREE.Scene();
	
	var pageHeight = Spec.pageHeight;
	var pageWidth = pageHeight*Spec.aspect;
	
	var leftPageGeom = new THREE.Geometry();
	leftPageGeom.vertices.push(new THREE.Vector3(-pageWidth,  -.5*pageHeight, 0));
	leftPageGeom.vertices.push(new THREE.Vector3(0,  -.5*pageHeight, 0));
	leftPageGeom.vertices.push(new THREE.Vector3(0,  .5*pageHeight, 0));
	leftPageGeom.vertices.push(new THREE.Vector3(-pageWidth,  .5*pageHeight, 0));
	leftPageGeom.faces.push(new THREE.Face3(0, 1, 2));
	leftPageGeom.faces.push(new THREE.Face3(0, 2, 3));
	leftPageGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0),
	                                    new THREE.Vector2(1, 0),
	                                    new THREE.Vector2(1, 1)]);
	leftPageGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0),
	                                    new THREE.Vector2(1, 1),
	                                    new THREE.Vector2(0, 1)]);
	leftPageGeom.computeCentroids();
	leftPageGeom.computeFaceNormals();
	
	var rightPageGeom = new THREE.Geometry();
	rightPageGeom.vertices.push(new THREE.Vector3(0,  -.5*pageHeight, 0));
	rightPageGeom.vertices.push(new THREE.Vector3(pageWidth,  -.5*pageHeight, 0));
	rightPageGeom.vertices.push(new THREE.Vector3(pageWidth,  .5*pageHeight, 0));
	rightPageGeom.vertices.push(new THREE.Vector3(0,  .5*pageHeight, 0));
	rightPageGeom.faces.push(new THREE.Face3(0, 1, 2));
	rightPageGeom.faces.push(new THREE.Face3(0, 2, 3));
	rightPageGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0),
	                                     new THREE.Vector2(1, 0),
	                                     new THREE.Vector2(1, 1)]);
	rightPageGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0),
	                                     new THREE.Vector2(1, 1),
	                                     new THREE.Vector2(0, 1)]);
	rightPageGeom.computeCentroids();
	rightPageGeom.computeFaceNormals();
	
	Reader.leftPage = new THREE.Mesh(leftPageGeom, new THREE.MeshLambertMaterial({map: null}));
	Reader.leftPage.material.depthWrite = false;
	Reader.rightPage = new THREE.Mesh(rightPageGeom, new THREE.MeshLambertMaterial({map: null}));
	Reader.rightPage.material.depthWrite = false;
	
	Reader.scene.add(Reader.leftPage);
	Reader.scene.add(Reader.rightPage);
	
	// and the camera
	Reader.scene.add(Camera.camera);
	
	// create a point light
	var pointLight = new THREE.PointLight(0xFFFFFF);
	
	// set its position
	pointLight.position.x = 10;
	pointLight.position.y = 50;
	pointLight.position.z = 130;
	
	// add to the scene
	Reader.scene.add(pointLight);
	//Reader.scene.add(new THREE.AmbientLight(0xffffff));
	
	Reader.fcnt = 0;
	
	Paper.init(pageWidth, pageHeight);
	Input.listeners.push(Reader);
	
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
Reader.render = function()
{
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
		if (Reader.currentPage < -1)
			Reader.currentPage = -1;
		if (Reader.currentPage > Spec.pages.length-1)
			Reader.currentPage = Spec.pages.length-1;
		Reader.leftPageIndex = Reader.currentPage;
		Reader.rightPageIndex = Reader.currentPage+1+(Hand.active ? 2 : 0);
		if (Reader.leftPageIndex < 0)
			Reader.leftPageIndex = -1;
		if (Reader.rightPageIndex >= Spec.pages.length-1)
			Reader.rightPageIndex = -1;
		
		if (Hand.active)
			Paper.setTexes(Spec.pages[Reader.leftPageIndex+1].tex, Spec.pages[Reader.leftPageIndex+2].tex);
		
		Spec.refreshTextures(Reader.currentPage, 5);
		if (Reader.leftPageIndex >= 0)
		{
			var page = Spec.pages[Reader.leftPageIndex];
			Reader.leftPage.material.map = page.tex;
			for (i=0;i<page.regions.length;i++)
			{
				Reader.tempModels[Reader.tempModels.length] = page.regions[i].line;
				Reader.scene.add(page.regions[i].line);
			}
		}
		else Reader.leftPage.material.map = Spec.emptyTex;
		if (Reader.rightPageIndex >= 0)
		{
			var page = Spec.pages[Reader.rightPageIndex];
			Reader.rightPage.material.map = page.tex;
			for (i=0;i<page.regions.length;i++)
			{
				Reader.tempModels[Reader.tempModels.length] = page.regions[i].line;
				Reader.scene.add(page.regions[i].line);
			}
		}
		else Reader.rightPage.material.map = Spec.emptyTex;
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

Reader.start = function(xml)
{
	Reader.dbg = document.getElementById("dbgArea");
	
	Spec.init(xml);
	
	Reader.init();
	Reader.render();
}

Reader.selectedRegion = null;
Reader.onClick = function(x, y)
{
	if (!Reader.zoomed)
	{
		var worldp = Camera.toWorldCoords(x, y);
		var isLeft = Camera.isLeftPage(worldp);
		var p = isLeft ? Camera.toLeftPageCoords(worldp) : Camera.toRightPageCoords(worldp);
		
		var region = null;
		if (Reader.selectedRegion == null)
		{
			if (isLeft && Reader.leftPageIndex >= 0)
				region = Region.getRegionAt(Spec.pages[Reader.leftPageIndex], p[0], p[1]);
			if (!isLeft && Reader.rightPageIndex >= 0)
				region = Region.getRegionAt(Spec.pages[Reader.rightPageIndex], p[0], p[1]);
		}
		
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
		}
		else
		{
			Camera.setDefaultPos();
			Reader.roiAlpha = 0;
			$('#zoom').css('display', 'inline');
			$('#back').css('display', 'none');
			$('#prev').css('display', 'inline');
			$('#next').css('display', 'inline');
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
		$('#prev').css('display', 'none');
		$('#next').css('display', 'none');
		$('#zoom').css('display', 'none');
	}
}

Reader.trace = function(text)
{
	Reader.dbg.innerHTML = text+"\n"+Reader.dbg.innerHTML;
}

Reader.onGrab = function(x, y) {Hand.grab(x, y);}
Reader.onDrag = function(x, y) {Hand.drag(x, y);}
Reader.onDrop = function(x, y) {Hand.drop(x, y);}
