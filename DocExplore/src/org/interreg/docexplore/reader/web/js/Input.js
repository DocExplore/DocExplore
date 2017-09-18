Input = {};
Input.down = [0, 0];
Input.isDown = false;
Input.isDrag = false;

Input.htmlMouseDown = function(x, y)
{
	var pos = $('#container').position();
	x -= pos.left;
	y -= pos.top;
	Input.down = [x, y];
	Input.isDown = true;
	if (Input.isDrag)
		Input.notifyDropped(x, y);
	Input.isDrag = false;
}

Input.htmlMouseUp = function()
{
	if (!Input.isDown && !Input.isDrag)
		return;
	var pos = $('#container').position();
	var x = Input.lastMove[0]-pos.left;
	var y = Input.lastMove[1]-pos.top;
	if (Input.isDrag)
		Input.notifyDropped(x, y);
	else Input.notifyClicked(Input.down[0], Input.down[1]);
	Input.isDown = false;
	Input.isDrag = false;
}

Input.lastMove = [0, 0];
Input.htmlMouseMove = function(x, y)
{
	if (x == Input.lastMove[0] && y == Input.lastMove[1])
		return;
	Input.lastMove = [x, y];
	
	if (!Input.isDown)
		return;
	var pos = $('#container').position();
	x -= pos.left;
	y -= pos.top;
	if (!Input.isDrag && (x-Input.down[0])*(x-Input.down[0])+(y-Input.down[1])*(y-Input.down[1]) > 3*3)
	{
		Input.isDrag = true;
		Input.notifyGrabbed(Input.down[0], Input.down[1]);
	}
	if (Input.isDrag)
		Input.notifyDragged(x, y);
}

Input.htmlMouseOut = function(x, y)
{
	//if (Input.isDown)
	//	Input.htmlMouseUp();
}

Input.listeners = [];
Input.notifyClicked = function(x, y)
{
	for (var i=0;i<Input.listeners.length;i++)
		if (Input.listeners[i].onClick != undefined)
			Input.listeners[i].onClick(x, y);
}
Input.notifyGrabbed = function(x, y)
{
	for (var i=0;i<Input.listeners.length;i++)
		if (Input.listeners[i].onGrab != undefined)
			Input.listeners[i].onGrab(x, y);
}
Input.notifyDragged = function(x, y)
{
	for (var i=0;i<Input.listeners.length;i++)
		if (Input.listeners[i].onDrag != undefined)
			Input.listeners[i].onDrag(x, y);
}
Input.notifyDropped = function(x, y)
{
	for (var i=0;i<Input.listeners.length;i++)
		if (Input.listeners[i].onDrop != undefined)
			Input.listeners[i].onDrop(x, y);
}

Input.lastScale = -1;
Input.waitForNextPinch = false;
Input.pinch = function(scale)
{
	if (Input.waitForNextPinch)
		return;
	if (Input.lastScale < 0)
		Input.lastScale = 1;
	var amount = Input.lastScale-scale;
	if (amount > 0)
		amount *= 4;
	Reader.zoomBy(amount);
	Input.lastScale = scale;
}
Input.pinchEnd = function()
{
	Input.lastScale = -1;
	Input.waitForNextPinch = false;
}
