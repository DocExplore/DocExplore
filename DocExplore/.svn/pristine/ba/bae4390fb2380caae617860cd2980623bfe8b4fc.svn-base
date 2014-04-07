var Paper = {};

Paper.w = 10;
Paper.h = 10;
Paper.nodes = [];
Paper.springs = [];
Paper.damping = .6;
Paper.springFactor = .05;
Paper.geom = {};
Paper.backGeom = {};
Paper.mesh = {};
Paper.backMesh = {};

Paper.init = function(w, h)
{
	for (var i=0;i<Paper.w;i++)
	{
		Paper.nodes[i] = [];
		for (var j=0;j<Paper.h;j++)
			Paper.nodes[i][j] = Paper.buildNode(i*w/(Paper.w-1), j*h/(Paper.h-1), 0, i, j, i == 0);
	}
	var maxSpread = 3;
	for (var s=1;s<=maxSpread;s++)
		for (var i=0;i<Paper.w;i++)
			for (var j=0;j<Paper.h;j++)
			{
				for (var jt=j;jt<=j+s;jt++)
					if (i+s < Paper.w && jt < Paper.h)
						Paper.springs.push(Paper.buildSpring(Paper.nodes[i][j], Paper.nodes[i+s][jt]));
				for (var it=i-s;it<i+s;it++)
					if (it >= 0 && it < Paper.w && j+s < Paper.h)
						Paper.springs.push(Paper.buildSpring(Paper.nodes[i][j], Paper.nodes[it][j+s]));
				for (var jt=j+1;jt<j+s;jt++)
					if (i-s >= 0 && jt < Paper.h)
						Paper.springs.push(Paper.buildSpring(Paper.nodes[i][j], Paper.nodes[i-s][jt]));
				
			}
	Reader.dbg.innerHTML += Paper.springs.length+" springs\n"
	
	Paper.geom = new THREE.Geometry();
	Paper.backGeom = new THREE.Geometry();
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
		{
			Paper.geom.vertices.push(new THREE.Vector3(0, 0, 0));
			Paper.backGeom.vertices.push(new THREE.Vector3(0, 0, 0));
		}
	for (var i=0;i<Paper.w-1;i++)
		for (var j=0;j<Paper.h-1;j++)
		{
			Paper.geom.faces.push(new THREE.Face3(i*Paper.h+j, (i+1)*Paper.h+j, i*Paper.h+(j+1)));
			Paper.geom.faceVertexUvs[0].push([new THREE.Vector2(i/(Paper.w-1), j/(Paper.h-1)),
			                                  new THREE.Vector2((i+1)/(Paper.w-1), j/(Paper.h-1)),
			                                  new THREE.Vector2(i/(Paper.w-1), (j+1)/(Paper.h-1))]);
			Paper.geom.faces.push(new THREE.Face3((i+1)*Paper.h+(j+1), i*Paper.h+(j+1), (i+1)*Paper.h+j));
			Paper.geom.faceVertexUvs[0].push([new THREE.Vector2((i+1)/(Paper.w-1), (j+1)/(Paper.h-1)),
			                                  new THREE.Vector2(i/(Paper.w-1), (j+1)/(Paper.h-1)),
			                                  new THREE.Vector2((i+1)/(Paper.w-1), j/(Paper.h-1))]);
			
			Paper.backGeom.faces.push(new THREE.Face3(i*Paper.h+j, (i+1)*Paper.h+j, i*Paper.h+(j+1)));
			Paper.backGeom.faceVertexUvs[0].push([new THREE.Vector2(1-i/(Paper.w-1), j/(Paper.h-1)),
			                                      new THREE.Vector2(1-(i+1)/(Paper.w-1), j/(Paper.h-1)),
			                                      new THREE.Vector2(1-i/(Paper.w-1), (j+1)/(Paper.h-1))]);
			Paper.backGeom.faces.push(new THREE.Face3((i+1)*Paper.h+(j+1), i*Paper.h+(j+1), (i+1)*Paper.h+j));
			Paper.backGeom.faceVertexUvs[0].push([new THREE.Vector2(1-(i+1)/(Paper.w-1), (j+1)/(Paper.h-1)),
			                                      new THREE.Vector2(1-i/(Paper.w-1), (j+1)/(Paper.h-1)),
			                                      new THREE.Vector2(1-(i+1)/(Paper.w-1), j/(Paper.h-1))]);
		}
	Paper.updateGeom();
	
	Paper.mesh = new THREE.Mesh(Paper.geom, new THREE.MeshLambertMaterial({map: null}));
	Paper.mesh.material.side = THREE.FrontSide;
	Paper.mesh.material.shading = THREE.SmoothShading;
	Paper.mesh.renderDepth = 2;
	Paper.backMesh = new THREE.Mesh(Paper.backGeom, new THREE.MeshLambertMaterial({map: null}));
	Paper.backMesh.material.side = THREE.BackSide;
	Paper.backMesh.material.shading = THREE.SmoothShading;
	Paper.backMesh.renderDepth = 2;
	
}

Paper.setTexes = function(left, right)
{
	Paper.mesh.material.map = left;
	Paper.backMesh.material.map = right;
}

Paper.updateGeom = function()
{
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
		{
			Paper.geom.vertices[i*Paper.h+j].x = Paper.nodes[i][j].x;
			Paper.geom.vertices[i*Paper.h+j].y = Paper.nodes[i][j].y;
			Paper.geom.vertices[i*Paper.h+j].z = Paper.nodes[i][j].z;
			
			Paper.backGeom.vertices[i*Paper.h+j].x = Paper.nodes[i][j].x;
			Paper.backGeom.vertices[i*Paper.h+j].y = Paper.nodes[i][j].y;
			Paper.backGeom.vertices[i*Paper.h+j].z = Paper.nodes[i][j].z;
		}
	Paper.geom.computeFaceNormals();
	Paper.geom.computeVertexNormals();
	Paper.geom.verticesNeedUpdate = true;
	Paper.geom.normalsNeedUpdate = true;
	Paper.backGeom.computeFaceNormals();
	Paper.backGeom.computeVertexNormals();
	Paper.backGeom.verticesNeedUpdate = true;
	Paper.backGeom.normalsNeedUpdate = true;
}

Paper.update = function()
{
	for (var i=0;i<Paper.springs.length;i++)
		Paper.updateSpring(Paper.springs[i]);
	for (var i=0;i<Paper.w;i++)
		for (var j=0;j<Paper.h;j++)
			Paper.updateNode(Paper.nodes[i][j]);
}

Paper.buildNode = function(x, y, z, i, j, isStatic)
{
	var node = {};
	node.x = x;
	node.y = y;
	node.z = z;
	node.vx = 0;
	node.vy = 0;
	node.vz = 0;
	node.i = i;
	node.j = j;
	node.isStatic = isStatic;
	
	return node;
}

Paper.buildSpring = function(a, b)
{
	var spring = {};
	spring.a = a;
	spring.b = b;
	spring.desired = Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z));
	
	return spring;
}

Paper.updateNode = function(n)
{
	if (n.isStatic)
		return;
	n.x += n.vx;
	n.y += n.vy;
	n.z += n.vz;
	if (n.z < 0)
		n.z = .001;
	n.vx *= Paper.damping;
	n.vy *= Paper.damping;
	n.vz *= Paper.damping;
}

Paper.updateSpring = function(s)
{
	var dist = Math.sqrt((s.a.x-s.b.x)*(s.a.x-s.b.x)+(s.a.y-s.b.y)*(s.a.y-s.b.y)+(s.a.z-s.b.z)*(s.a.z-s.b.z));
	var dx = (s.b.x-s.a.x)/dist;
	var dy = (s.b.y-s.a.y)/dist;
	var dz = (s.b.z-s.a.z)/dist;
	var cor = Paper.springFactor*(dist-s.desired);
	s.a.vx += dx*cor;
	s.a.vy += dy*cor;
	s.a.vz += dz*cor;
	s.b.vx -= dx*cor;
	s.b.vy -= dy*cor;
	s.b.vz -= dz*cor;
}

Paper.closestNode = function(p, v)
{
	var mind2 = -1, minn = null;
	var i = Paper.w-1;
	for (var j=0;j<Paper.h;j++)
	{
		var d2 = Paper.dist2(p, v, Paper.nodes[i][j]);
		if (mind2 < 0 || d2 < mind2)
			{mind2 = d2; minn = Paper.nodes[i][j];}
	}
	return minn;
}
Paper.dist2 = function(p, v, q)
{
	var k = (Math3D.dot(q, v)-Math3D.dot(p, v))/Math3D.dot(v, v);
	var i = Math3D.add(p, Math3D.scale(v, k));
	return Math3D.dist2(i, q);
}

//Paper.isInTriangle = function(x, y, z, a, b, c)
