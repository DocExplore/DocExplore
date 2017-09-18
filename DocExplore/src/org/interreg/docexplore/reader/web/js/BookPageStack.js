function BookPageStack(cover, nPages, pageWidth, pageHeight, pathLength, left)
{
	this.cover = cover;
	this.nPages = nPages;
	this.pageWidth = pageWidth;
	this.pageHeight = pageHeight;
	this.pathLength = pathLength;
	this.left = left;
	this.vx0 = left ? -1 : 1;
	this.path = [];
	this.projection = [];
	this.normals = [];
	this.margin = .5*cover.coverHeight*(1-pageHeight);
	this.nStackPages = nPages/2+(!left && nPages%2==1 ? 1 : 0);
	this.sidef1 = null;
	this.sidef2 = null;
	
	for (var i=0;i<pathLength;i++)
	{
		this.path[i] = [0, 0];
		this.projection[i] = [0, 0];
		this.normals[i] = [0, 0];
	}
	
	this.stackFrontGeom = new THREE.Geometry();
	for (var i=0;i<2*pathLength;i++)
		this.stackFrontGeom.vertices.push(new THREE.Vector3(0, 0, 0));
	this.stackSideGeom = new THREE.Geometry();
	for (var i=0;i<4*pathLength+4;i++)
		this.stackSideGeom.vertices.push(new THREE.Vector3(0, 0, 0));
	for (var i=0;i<pathLength-1;i++)
	{
		var va = left ? 2*i+1 : 2*i+3, 
			vb = left ? 2*i+3 : 2*i+1, 
			vc = left ? 2*i+3 : 2*i+2, 
			vd = left ? 2*i+2 : 2*i+3;
		var s0 = left ? 1-i*1./(pathLength-1) : i*1./(pathLength-1),
			s1 = left ? 1-(i+1)*1./(pathLength-1) : (i+1)*1./(pathLength-1);
		var sa = left ? s0 : s1, ta = 1,
			sb = left ? s1 : s0, tb = 1,
			sc = s1, tc = left ? 1 : 0,
			sd = s1, td = left ? 0 : 1;
		this.stackFrontGeom.faces.push(new THREE.Face3(2*i, va, vb));
		this.stackFrontGeom.faceVertexUvs[0].push([new THREE.Vector2(s0, 0), new THREE.Vector2(sa, ta), new THREE.Vector2(sb, tb)]);
		this.stackFrontGeom.faces.push(new THREE.Face3(2*i, vc, vd));
		this.stackFrontGeom.faceVertexUvs[0].push([new THREE.Vector2(s0, 0), new THREE.Vector2(sc, tc), new THREE.Vector2(sd, td)]);
		
		
		this.stackSideGeom.faces.push(new THREE.Face3(2*i, va, vb));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
		this.stackSideGeom.faces.push(new THREE.Face3(2*i, vc, vd));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
	}
	for (var i=pathLength-1;i<2*pathLength-2;i++)
	{
		var vi0 = i+1;
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+3 : 2*vi0+1, left ? 2*vi0+1 : 2*vi0+3));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+2 : 2*vi0+3, left ? 2*vi0+3 : 2*vi0+2));
		this.stackSideGeom.faceVertexUvs[0].push([new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)]);
	}
	{
		var vi0 = 2*pathLength;
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+1 : 2*vi0+3, left ? 2*vi0+3 : 2*vi0+1));
		this.sidef1 = [new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)];
		this.stackSideGeom.faceVertexUvs[0].push(this.sidef1);
		this.stackSideGeom.faces.push(new THREE.Face3(2*vi0, left ? 2*vi0+3 : 2*vi0+2, left ? 2*vi0+2 : 2*vi0+3));
		this.sidef2 = [new THREE.Vector2(0, 0), new THREE.Vector2(0, 0), new THREE.Vector2(0, 0)];
		this.stackSideGeom.faceVertexUvs[0].push(this.sidef2);
	}
	
	var coverPath = cover.path;
	this.spineGeom = new THREE.Geometry();
	for (var i=0;i<coverPath.length-2;i++)
		this.spineGeom.vertices.push(new THREE.Vector3(coverPath[i+1][0], -.5*cover.coverHeight+this.margin, coverPath[i+1][1]));
	for (var i=0;i<coverPath.length-2;i++)
		this.spineGeom.vertices.push(new THREE.Vector3(coverPath[i+1][0], .5*cover.coverHeight-this.margin, coverPath[i+1][1]));
	for (var i=0;i<coverPath.length-4;i++)
		this.spineGeom.faces.push(new THREE.Face3(0, i+1, i+2));
	for (var i=0;i<coverPath.length-4;i++)
		this.spineGeom.faces.push(new THREE.Face3(coverPath.length-2, coverPath.length+i, coverPath.length+i-1));
	
	this.stackFrontGeom.computeFaceNormals();
	this.stackFrontGeom.computeVertexNormals();
	this.stackSideGeom.computeFaceNormals();
	this.stackSideGeom.computeVertexNormals();
	this.spineGeom.computeFaceNormals();
	this.spineGeom.computeVertexNormals();
	
	this.stackFrontMesh = new THREE.Mesh(this.stackFrontGeom, new THREE.MeshLambertMaterial({map: Spec.loadingTex}));
	this.stackFrontMesh.material.transparent = true;
	this.stackFrontMesh.frustumCulled = false;
	this.stackFrontMesh.material.side = THREE.FrontSide;
	this.stackFrontMesh.material.shading = THREE.SmoothShading;
	this.stackSideMesh = new THREE.Mesh(this.stackSideGeom, new THREE.MeshLambertMaterial({color: 0xC0B0A0}));
	this.stackSideMesh.frustumCulled = false;
	this.stackSideMesh.material.side = THREE.FrontSide;
	this.stackSideMesh.material.shading = THREE.SmoothShading;
	this.spineMesh = new THREE.Mesh(this.spineGeom, new THREE.MeshLambertMaterial({}));
	this.spineMesh.frustumCulled = false;
	this.spineMesh.material.side = THREE.FrontSide;
	this.spineMesh.material.shading = THREE.SmoothShading;
	
	TexLoader.loadSideTex(this);
	
	this.model = new THREE.Group();
	this.model.add(this.stackFrontMesh);
	this.model.add(this.stackSideMesh);
	this.model.add(this.spineMesh);
	this.model.frustumCulled = false;
	
	this.bufv = [0, 0];
}

BookPageStack.prototype.setTex = function(tex)
{
	this.stackFrontMesh.material.map = tex;
	this.stackFrontMesh.material.needsUpdate = true;
}

BookPageStack.prototype.updateStack = function()
{
	this.model.visible = this.nStackPages != 0;
	
	var cover = this.cover;
	var hingex = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftHinge : cover.bottomRightHinge]].x,
		hingey = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftHinge : cover.bottomRightHinge]].z;
	var edgex = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftEdge : cover.bottomRightEdge]].x,
		edgey = cover.geoms[cover.inner].vertices[cover.indices[this.left ? cover.bottomLeftEdge : cover.bottomRightEdge]].z;
	
	//recompute path and projection
	var sep = this.nStackPages*1./this.nPages;
	paperCurveCompute(hingex, hingey, this.vx0, 1, edgex-hingex, edgey-hingey, this.pageWidth*cover.coverLength, this.pathLength-1, (1.+sep)*200, this.path);
	if (sep > 0)
		paperCurveProject(this.path, cover.coverDepth*(this.left ? sep : -sep), 0, this.pageWidth*cover.coverLength, this.projection);
	else for (var i=0;i<this.path.length;i++)
		{this.projection[i][0] = this.path[i][0]; this.projection[i][1] = this.path[i][1];}
	
	//update the meshes
	for (var i=0;i<this.pathLength;i++)
	{
		var nx, ny;
		if (i == 0)
		{
			nx = -(this.projection[1][1]-this.projection[0][1]);
			ny = this.projection[1][0]-this.projection[0][0];
		}
		else if (i == this.pathLength-1)
		{
			nx = -(this.projection[this.pathLength-1][1]-this.projection[this.pathLength-2][1]);
			ny = this.projection[this.pathLength-1][0]-this.projection[this.pathLength-2][0];
		}
		else
		{
			nx = -(this.projection[i+1][1]-this.projection[i-1][1]);
			ny = this.projection[i+1][0]-this.projection[i-1][0];
		}
		var nl = Math.sqrt(nx*nx+ny*ny);
		nx /= nl; ny /= nl;
		if (this.left)
			{nx = -nx; ny = -ny;}
		
		this.normals[i][0] = nx; 
		this.normals[i][1] = ny;
		this.stackFrontGeom.vertices[2*i].set(this.projection[i][0], -.5*cover.coverHeight+this.margin, this.projection[i][1]);
		this.stackFrontGeom.vertices[2*i+1].set(this.projection[i][0], .5*cover.coverHeight-this.margin, this.projection[i][1]);
		
		this.stackSideGeom.vertices[2*i].set(this.path[i][0], -.5*cover.coverHeight+this.margin, this.path[i][1]);
		this.stackSideGeom.vertices[2*i+1].set(this.projection[i][0], -.5*cover.coverHeight+this.margin, this.projection[i][1]);
		this.stackSideGeom.vertices[2*(this.pathLength+i)].set(this.path[i][0], .5*cover.coverHeight-this.margin, this.path[i][1]);
		this.stackSideGeom.vertices[2*(this.pathLength+i)+1].set(this.projection[i][0], .5*cover.coverHeight-this.margin, this.projection[i][1]);
	}
	
	var dx = this.projection[this.pathLength-1][0]-this.path[this.pathLength-1][0],
		dy = this.projection[this.pathLength-1][1]-this.path[this.pathLength-1][1];
	var nx = -dy, ny = dx;
	var nl = Math.sqrt(nx*nx+ny*ny);
	nx /= nl; ny /= nl;
	if (!this.left)
		{nx = -nx; ny = -ny;}
	
	this.stackSideGeom.vertices[4*this.pathLength].set(this.path[this.pathLength-1][0], -.5*cover.coverHeight+this.margin, this.path[this.pathLength-1][1]);
	this.stackSideGeom.vertices[4*this.pathLength+1].set(this.projection[this.pathLength-1][0], -.5*cover.coverHeight+this.margin, this.projection[this.pathLength-1][1]);
	this.stackSideGeom.vertices[4*this.pathLength+2].set(this.path[this.pathLength-1][0], .5*cover.coverHeight-this.margin, this.path[this.pathLength-1][1]);
	this.stackSideGeom.vertices[4*this.pathLength+3].set(this.projection[this.pathLength-1][0], .5*cover.coverHeight-this.margin, this.projection[this.pathLength-1][1]);
	
	if (this.left)
	{
		this.sidef1[0].set(0, 0);
		this.sidef1[1].set(sep, 0);
		this.sidef1[2].set(sep, 1);
		this.sidef2[0].set(0, 0);
		this.sidef2[1].set(sep, 1);
		this.sidef2[2].set(0, 1);
	}
	else
	{
		this.sidef1[0].set(1, 0);
		this.sidef1[1].set(1-sep, 1);
		this.sidef1[2].set(1-sep, 0);
		this.sidef2[0].set(1, 0);
		this.sidef2[1].set(1, 1);
		this.sidef2[2].set(1-sep, 1);
	}
	
	this.stackFrontGeom.computeFaceNormals();
	this.stackFrontGeom.computeVertexNormals();
	this.stackFrontGeom.verticesNeedUpdate = true;
	this.stackSideGeom.computeFaceNormals();
	this.stackSideGeom.computeVertexNormals();
	this.stackSideGeom.verticesNeedUpdate = true;
	this.stackSideGeom.uvsNeedUpdate = true;
	this.spineGeom.computeFaceNormals();
	this.spineGeom.computeVertexNormals();
	this.spineGeom.verticesNeedUpdate = true;
}

BookPageStack.prototype.toBookModel = function(x, y, res)
{
	this.projectionAt(x*this.cover.coverLength, this.bufv);
	res.x = this.bufv[0];
	res.y = -.5*this.cover.coverHeight+this.margin+(1-y)*(this.cover.coverHeight-2*this.margin);
	res.z = this.bufv[1];
}

BookPageStack.prototype.projectionAt = function(l, res)
{
	var unitLength = (this.cover.coverLength/(this.pathLength-1));
	var proji = Math.floor(l/unitLength);
	if (proji > this.pathLength-2)
		proji = this.pathLength-2;
	
	var k = (l-proji*unitLength)/unitLength;
	res[0] = this.projection[proji][0]+k*(this.projection[proji+1][0]-this.projection[proji][0]);
	res[1] = this.projection[proji][1]+k*(this.projection[proji+1][1]-this.projection[proji][1]);
	return res;
}
