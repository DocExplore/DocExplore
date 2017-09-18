Hand = {}

Hand.active = false;
Hand.grabbedNode = null;
Hand.grabbedX = 0;
Hand.grabPos = [0, 0, 0];
Hand.goLeft = false;
Hand.dropped = 0;

Hand.zoomGrab = [];
Hand.zoomOrigin = [];

Hand.grab = function(x, y)
{
	var book = Reader.bookModel;
	if (!Reader.zoomed)
	{
		if (!Reader.ecoMode)
		{
    		var p = Camera.toWorldRay(x, y);
    		if (!Hand.active && !Reader.bookModel.isAnimating)
    		{
    			if (Reader.currentPage < -1 || p.x >= 0 && Reader.currentPage > Spec.pages.length-2)
    				Reader.requestPage = Reader.currentPage+2;
    			else if (p.x < 0)
    				Reader.requestPage = Reader.currentPage-2;
    			if (Reader.currentPage == -3 || Reader.currentPage == Spec.pages.length+1)
    				return;
    			if (p.x < 0 && Reader.currentPage < 0)
    				return;
    			if (p.x >= 0 && Reader.currentPage > Spec.pages.length-2)
    				return;
    			Hand.active = true;
    			if (p.x < 0)
    				Reader.bookModel.page.setTo(Reader.bookModel.leftStack);
    			else
    				Reader.bookModel.page.setTo(Reader.bookModel.rightStack);
    		}
    		Hand.grabbedNode = Reader.bookModel.closestToLine(Camera.camera.position, p);
    		Hand.grabbedX = p.x;
    	}
	}
	else
	{
		Hand.zoomGrab = [x, y];
		//Hand.zoomOrigin = [Camera.attractorPos[0], Camera.attractorPos[1]];
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
		Hand.goLeft = p[0] < Hand.grabbedX;
		Hand.grabbedX = p[0];
	}
	//don't move cam in XY plane if we're in a pinch zoom (very annoying otherwise!)
	else if (Input.lastScale < 0)
	{
		Camera.translate((Hand.zoomGrab[0]-x)*.5/Reader.width, (y-Hand.zoomGrab[1])*.5/Reader.width);
		Hand.zoomGrab[0] = x;
		Hand.zoomGrab[1] = y;
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
		Hand.dropped++;
		if (Hand.dropped > 25)
		{
			Hand.active = false;
			if (Hand.goLeft)
				Reader.requestPage = Reader.currentPage+2;
		}
	}
}
