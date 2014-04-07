Hand = {}

Hand.active = false;
Hand.grabbedNode = null;
Hand.grabPos = [0, 0, 0];
Hand.goLeft = false;
Hand.dropped = 0;

Hand.zoomGrab = [];
Hand.zoomOrigin = [];

Hand.grab = function(x, y)
{
	if (!Reader.zoomed)
	{
		var p = Camera.toWorldRay(x, y);
		if (!Hand.active)
		{
			if (p.x < 0 && Reader.currentPage < 0)
				return;
			if (p.x > 0 && Reader.currentPage > Spec.pages.length-2)
				return;
			Hand.active = true;
			if (p.x < 0)
				Reader.requestPage = Reader.currentPage-2;
			Reader.scene.add(Paper.mesh);
			Reader.scene.add(Paper.backMesh);
			//Reader.dbg.innerHTML += p[0]+"\n";
			if (p.x < 0)
				Hand.setToLeftPage();
			else Hand.setToRightPage();
		}
		Hand.grabbedNode = Paper.closestNode(Camera.camera.position, p);
	}
	else
	{
		Hand.zoomGrab = Camera.toWorldCoords(x, y);
		Hand.zoomOrigin = [Camera.attractorPos[0], Camera.attractorPos[1]];
	}
}

Hand.tmpx = null;
Hand.drag = function(x, y)
{
	if (!Reader.zoomed)
	{
		if (Hand.grabbedNode == null)
			return;
		var p = Camera.toWorldCoords(x, y);
		var pageHeight = Spec.pageHeight;
		var pageWidth = pageHeight*Spec.aspect;
		
		if (Hand.tmpx == null)
			Hand.tmpx = -p[0];
		else Hand.tmpx += .1*(-p[0]-Hand.tmpx);
			
		var h = Hand.handPos(Hand.grabbedNode, Hand.tmpx, Hand.grabbedNode.i*pageWidth/(Paper.w-1));
		if (h.x != Hand.grabPos[0])
			Hand.goLeft = h.x < Hand.grabPos[0];
		Hand.grabPos = [h.x, -.5*pageHeight+Hand.grabbedNode.j*pageHeight/(Paper.h-1), h.z];
	}
	else
	{
		var p = Camera.toWorldCoords(x, y);
		Camera.setPos(Hand.zoomOrigin[0]+3*(Hand.zoomGrab[0]-p[0]), Hand.zoomOrigin[1]+3*(Hand.zoomGrab[1]-p[1]), Camera.attractorPos[2]);
		//Camera.setDiffPos(3*(Hand.zoomGrab[0]-p[0]), 3*(Hand.zoomGrab[1]-p[1]), 0);
		//Hand.zoomGrab = p;
		//Reader.trace(p[0]+","+p[1]);
	}
}

Hand.drop = function(x, y)
{
	Hand.grabbedNode = null;
	Hand.dropped = 0;
	Hand.tmpx = null;
}

Hand.update = function()
{
	if (Hand.grabbedNode == null)
	{
		if (Hand.goLeft)
			Hand.attractToLeftPage();
		else Hand.attractToRightPage();
		Hand.dropped++;
		if (Hand.dropped > 20)
		{
			Hand.active = false;
			Reader.scene.remove(Paper.mesh);
			Reader.scene.remove(Paper.backMesh);
			if (Hand.goLeft)
				Reader.requestPage = Reader.currentPage+2;
		}
	}
	for (var i=0;i<4;i++)
	{
		if (Hand.grabbedNode != null)
		{
			Hand.grabbedNode.vx += Hand.grabPos[0]-Hand.grabbedNode.x;
			Hand.grabbedNode.vy += Hand.grabPos[1]-Hand.grabbedNode.y;
			Hand.grabbedNode.vz += Hand.grabPos[2]-Hand.grabbedNode.z;
		}
		Paper.update();
	}
	for (var i=0;i<4;i++)
		Paper.update();
	Paper.updateGeom();
}

Hand.handPos = function(n, x, ray)
{
	var d = {x: 0, y: 0, z: -1};
	var r = {x: x-Camera.camera.position.x, y: 0, z: Camera.camera.position.z};
	r = Math3D.normalize(r);
	var k = (Math3D.dot(n, d)-Math3D.dot(Camera.camera.position, d))/Math3D.dot(r, d);
	var p = Math3D.add(Camera.camera.position, Math3D.scale(r, k));
	p.z += .01;
	p = Math3D.scale(Math3D.normalize(p), ray);
	return p;
}

Hand.setToRightPage = function()
{
	var pageHeight = Spec.pageHeight;
	var pageWidth = pageHeight*Spec.aspect;
	
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
		{
			Paper.nodes[i][j].x = i*pageWidth/(Paper.w-1);
			Paper.nodes[i][j].y = -.5*pageHeight+j*pageHeight/(Paper.h-1);
		}
}
Hand.attractToRightPage = function()
{
	var pageHeight = Spec.pageHeight;
	var pageWidth = pageHeight*Spec.aspect;
	
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
		{
			Paper.nodes[i][j].vx += .1*(i*pageWidth/(Paper.w-1)-Paper.nodes[i][j].x);
			Paper.nodes[i][j].vy += .1*(-.5*pageHeight+j*pageHeight/(Paper.h-1)-Paper.nodes[i][j].y);
			Paper.nodes[i][j].vz += -.1*Paper.nodes[i][j].z;
		}
}
Hand.setToLeftPage = function()
{
	var pageHeight = Spec.pageHeight;
	var pageWidth = pageHeight*Spec.aspect;
	
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
		{
			Paper.nodes[i][j].x = -i*pageWidth/(Paper.w-1);
			Paper.nodes[i][j].y = -.5*pageHeight+j*pageHeight/(Paper.h-1);
		}
}
Hand.attractToLeftPage = function()
{
	var pageHeight = Spec.pageHeight;
	var pageWidth = pageHeight*Spec.aspect;
	
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
		{
			Paper.nodes[i][j].vx += .1*(-i*pageWidth/(Paper.w-1)-Paper.nodes[i][j].x);
			Paper.nodes[i][j].vy += .1*(-.5*pageHeight+j*pageHeight/(Paper.h-1)-Paper.nodes[i][j].y);
			Paper.nodes[i][j].vz += -.1*Paper.nodes[i][j].z;
		}
}
