var Camera = {};

Camera.camera = {};
Camera.camTan = 0;
Camera.aspect = 0;
Camera.defaultPos = [0, 0, 0];
Camera.attractorPos = [0, 0, 0];
Camera.maxUnzoom = 6;
Camera.defaultFov = 23;

Camera.init = function(x, y, z)
{
	var near = 0.01, far = 100;
	
	Camera.aspect = Reader.width/Reader.height;
	Camera.camera = new THREE.PerspectiveCamera(Camera.defaultFov, Camera.aspect, near, far);
	
	Camera.defaultPos = [x, y, z];
	Camera.attractorPos = [x, y, z];
	Camera.camera.position.x = x;
	Camera.camera.position.y = y;
	Camera.camera.position.z = z;
	
	Camera.camTan = Math.tan(.5*Math.PI*Camera.camera.fov/180);
}

Camera.update = function()
{
	var spring = .1;
	Camera.camera.position.x += spring*(Camera.attractorPos[0]-Camera.camera.position.x);
	Camera.camera.position.y += spring*(Camera.attractorPos[1]-Camera.camera.position.y);
	Camera.camera.position.z += spring*(Camera.attractorPos[2]-Camera.camera.position.z);
}

Camera.unzoomed = function()
{
	return Camera.camera.position.z > Camera.maxUnzoom-.02;
}

Camera.translate = function(x, y)
{
	Camera.attractorPos[0] += x*Camera.camera.position.z;
	Camera.attractorPos[1] += y*Camera.camera.position.z;
}
Camera.setPos = function(x, y, z)
{
	z = z > Camera.maxUnzoom ? Camera.maxUnzoom : z < 2 ? 2 : z;
	Camera.attractorPos = [x, y, z];
}
Camera.setDiffPos = function(x, y, z)
{
	Camera.setPos(Camera.attractorPos[0]+x, Camera.attractorPos[1]+y, Camera.attractorPos[2]+z);
}
Camera.setDefaultPos = function()
{
	Camera.attractorPos[0] = Camera.defaultPos[0];
	Camera.attractorPos[1] = Camera.defaultPos[1];
	Camera.attractorPos[2] = Camera.defaultPos[2];
}
Camera.setPosToRegion = function(region)
{
	var xm = region.bounds[1][0];
	var ym = .5*(region.bounds[0][1]+region.bounds[1][1]);
	var l = Math.max(2*Reader.height*(region.bounds[1][0]-region.bounds[0][0])/Reader.width, region.bounds[1][1]-region.bounds[0][1]);
	var zm = l/(2*Math.sin(.5*Math.PI*Camera.camera.fov/180));
	Camera.setPos(xm, ym, zm);
}

Camera.toWorldCoords = function(x, y)
{
	x = (x/Reader.width*2-1)*Reader.width/Reader.height;
	y = y/Reader.height*2-1;
	var k = Camera.camera.position.z*Camera.camTan;
	x *= k;
	y *= k;
	return [x+Camera.camera.position.x, -y+Camera.camera.position.y];
}
Camera.toWorldRay = function(x, y)
{
	var p = Camera.toWorldCoords(x, y);
	return new THREE.Vector3(p[0]-Camera.camera.position.x, p[1]-Camera.camera.position.y, -Camera.camera.position.z);
}
Camera.isLeftPage = function(p) {return p[0] < 0;}
Camera.toRightPageCoords = function(p) {return [p[0]/(Spec.pageHeight*Spec.aspect), .5*(1-p[1]/(.5*Spec.pageHeight))];}
Camera.toLeftPageCoords = function(p) {return [1+p[0]/(Spec.pageHeight*Spec.aspect), .5*(1-p[1]/(.5*Spec.pageHeight))];}
Camera.fromRightPageCoords = function(p) {return [p[0]*Spec.pageHeight*Spec.aspect, Spec.pageHeight*(.5-p[1])];}
Camera.fromLeftPageCoords = function(p) {return [(p[0]-1)*Spec.pageHeight*Spec.aspect, Spec.pageHeight*(.5-p[1])];}
