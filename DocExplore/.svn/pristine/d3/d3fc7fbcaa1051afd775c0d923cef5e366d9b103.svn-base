Math3D = {}

Math3D.add = function(a, b)
{
	return {x: b.x+a.x, y: b.y+a.y, z: b.z+a.z};
}
Math3D.diff = function(a, b)
{
	return {x: b.x-a.x, y: b.y-a.y, z: b.z-a.z};
}
Math3D.dot = function(a, b)
{
	return b.x*a.x+b.y*a.y+b.z*a.z;
}
Math3D.cross = function(a, b)
{
	return {x: a.y*b.z-a.z*b.y, y: a.z*b.x-a.x*b.z, z: a.x*b.y-a.y*b.x};
}
Math3D.dist2 = function(a, b)
{
	return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z);
}
Math3D.dist = function(a, b)
{
	return Math.sqrt(Math3D.dist2(a, b));
}
Math3D.scale = function(a, k)
{
	return {x: a.x*k, y: a.y*k, z: a.z*k};
}
Math3D.length2 = function(a)
{
	return a.x*a.x+a.y*a.y+a.z*a.z;
}
Math3D.length = function(a)
{
	return Math.sqrt(Math3D.length2(a));
}
Math3D.normalize = function(a)
{
	var l = Math3D.length(a);
	return Math3D.scale(a, 1/l);
}
