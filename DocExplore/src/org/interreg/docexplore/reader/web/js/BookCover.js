function BookCover(coverLength, coverHeight, coverDepth, bindingWidth, spineWidth, nSpinePoints)
{
	this.coverLength = coverLength;
	this.coverHeight = coverHeight;
	this.coverDepth = coverDepth;
	this.bindingWidth = bindingWidth;
	this.spineWidth = spineWidth;
	this.nSpinePoints = nSpinePoints;
	
	this.outer = 0;
	this.inner = 1;
	
	this.path = [];
	this.path[0] = [-.5*this.coverDepth, this.coverLength+this.spineWidth, 0.];
	this.path[1] = [-.5*this.coverDepth, this.spineWidth, .45];
	var p0 = [-.5*this.coverDepth, this.spineWidth];
	var p1 = [.5*this.coverDepth, this.spineWidth];
	var n0 = [0., -2*this.spineWidth];
	var n1 = [0., 2*this.spineWidth];
	for (var i=0;i<this.nSpinePoints;i++)
	{
		var t = (i+1)*1./(this.nSpinePoints+1);
		this.path[2+i] = [ 
			(1-t)*(p0[0]+n0[0]*t)+t*(p1[0]-n1[0]+n1[0]*t),
			(1-t)*(p0[1]+n0[1]*t)+t*(p1[1]-n1[1]+n1[1]*t), 
			.55+.1*t];
	}
	this.path[2+this.nSpinePoints] = [.5*this.coverDepth, this.spineWidth, .55];
	this.path[3+this.nSpinePoints] = [.5*this.coverDepth, this.coverLength+this.spineWidth, 1.];
	
	this.geoms = [new THREE.Geometry(), new THREE.Geometry()];
	
	var vcnt = 0;
	this.topLeftEdge = vcnt++;
	this.bottomLeftEdge = vcnt++;
	this.topRightEdge = vcnt++;
	this.bottomRightEdge = vcnt++;
	this.topLeftHinge = vcnt++;
	this.bottomLeftHinge = vcnt++;
	this.topRightHinge = vcnt++;
	this.bottomRightHinge = vcnt++;
	
	this.indices = [];
	for (var j=0;j<vcnt;j++)
		this.indices[i] = -1;
	this.indices[this.topLeftEdge] = 0;
	this.indices[this.bottomLeftEdge] = 1;
	this.indices[this.topLeftHinge] = 2;
	this.indices[this.bottomLeftHinge] = 3;
	this.indices[this.topRightHinge] = 2*this.path.length-4;
	this.indices[this.bottomRightHinge] = 2*this.path.length-3;
	this.indices[this.topRightEdge] = 2*this.path.length-2;
	this.indices[this.bottomRightEdge] = 2*this.path.length-1;
	
	for (var i=0;i<this.path.length;i++)
	{
		this.geoms[this.outer].vertices.push(new THREE.Vector3(0, 0, 0));
		this.geoms[this.outer].vertices.push(new THREE.Vector3(0, 0, 0));
		this.geoms[this.inner].vertices.push(new THREE.Vector3(0, 0, 0));
		this.geoms[this.inner].vertices.push(new THREE.Vector3(0, 0, 0));
	}
	
	var normal = [0, 0];
	for (var i=0;i<this.path.length;i++)
	{
		if (i == 0) {normal[0] = -1; normal[1] = 0;}
		else if (i == this.path.length-1) {normal[0] = 1; normal[1] = 0;}
		else this.getPathNormal(i, normal);
		//float so = i == 0 ? 1 : i == path.length-1 ? 0 : .5f;
		
		this.geoms[this.outer].vertices[2*i].set(this.path[i][0]+this.bindingWidth*normal[0], .5*this.coverHeight, this.path[i][1]+this.bindingWidth*normal[1]);
		this.geoms[this.outer].vertices[2*i+1].set(this.path[i][0]+this.bindingWidth*normal[0], -.5*this.coverHeight, this.path[i][1]+this.bindingWidth*normal[1]);
		this.geoms[this.inner].vertices[2*i].set(this.path[i][0], .5*this.coverHeight, this.path[i][1]);
		this.geoms[this.inner].vertices[2*i+1].set(this.path[i][0], -.5*this.coverHeight, this.path[i][1]);
	}
	for (var i=0;i<this.path.length-1;i++)
	{
		var s0 = i == 0 ? 1 : i == this.path.length-1 ? 0 : .5;
		var s1 = i == this.path.length-2 ? 0 : .5;
		this.geoms[this.outer].faces.push(new THREE.Face3(2*i, 2*i+2, 2*i+1));
		this.geoms[this.outer].faceVertexUvs[0].push([new THREE.Vector2(s0, 1), new THREE.Vector2(s1, 1), new THREE.Vector2(s0, 0)]);
		this.geoms[this.outer].faces.push(new THREE.Face3(2*i+1, 2*i+2, 2*i+3));
		this.geoms[this.outer].faceVertexUvs[0].push([new THREE.Vector2(s0, 0), new THREE.Vector2(s1, 1), new THREE.Vector2(s1, 0)]);
		this.geoms[this.inner].faces.push(new THREE.Face3(2*i, 2*i+1, 2*i+2));
		this.geoms[this.inner].faceVertexUvs[0].push([new THREE.Vector2(1-s0, 1), new THREE.Vector2(1-s0, 0), new THREE.Vector2(1-s1, 1)]);
		this.geoms[this.inner].faces.push(new THREE.Face3(2*i+1, 2*i+3, 2*i+2));
		this.geoms[this.inner].faceVertexUvs[0].push([new THREE.Vector2(1-s0, 0), new THREE.Vector2(1-s1, 0), new THREE.Vector2(1-s1, 1)]);
	}
	
	this.meshes = [];
	this.meshes[this.outer] = new THREE.Mesh(this.geoms[this.outer], new THREE.MeshLambertMaterial({map: Spec.loadingTex}));
	this.meshes[this.inner] = new THREE.Mesh(this.geoms[this.inner], new THREE.MeshLambertMaterial({map: Spec.loadingTex}));
	
	TexLoader.loadCoverTex(this);
	
	this.model = new THREE.Group();
	for (var i=0;i<this.geoms.length;i++)
	{
		this.meshes[i].material.side = THREE.FrontSide;
		this.meshes[i].material.shading = THREE.SmoothShading;
		this.meshes[i].material.transparent = true;
		this.meshes[i].frustumCulled = false;
		this.geoms[i].computeFaceNormals();
		this.geoms[i].computeVertexNormals();
		this.model.add(this.meshes[i]);
	}
	this.model.frustumCulled = false;
	this.time = 0.;
}

BookCover.prototype.getPathNormal = function(index, normal)
{
	var dx1 = this.path[index-1][0]-this.path[index][0], dy1 = this.path[index-1][1]-this.path[index][1],
		dx2 = this.path[index+1][0]-this.path[index][0], dy2 = this.path[index+1][1]-this.path[index][1];
	var l1 = Math.sqrt(dx1*dx1+dy1*dy1), l2 = Math.sqrt(dx2*dx2+dy2*dy2);
	dx1 /= l1; dy1 /= l1;
	dx2 /= l2; dy2 /= l2;
	var nx = -(dx1+dx2), ny = -(dy1+dy2);
	var ln = Math.sqrt(nx*nx+ny*ny);
	normal[0] = nx/ln;
	normal[1] = ny/ln;
}

BookCover.prototype.setCoverVertex = function(geom, vertex, x, y, z)
{
	if (this.indices[vertex] > -1)
		this.geoms[geom].vertices[this.indices[vertex]].set(x, y, z);
}

BookCover.prototype.setAngle = function(la, ra)
{
	var llx = -Math.sin(la), llz = Math.cos(la);
	var rlx = -Math.sin(ra), rlz = Math.cos(ra);
	var lnx = llz, lnz = -llx;
	var rnx = rlz, rnz = -rlx;
	
	this.setCoverVertex(this.inner, this.topLeftEdge, -this.coverDepth/2+this.coverLength*llx, .5*this.coverHeight, this.spineWidth+this.coverLength*llz);
	this.setCoverVertex(this.inner, this.bottomLeftEdge, -this.coverDepth/2+this.coverLength*llx, -.5*this.coverHeight, this.spineWidth+this.coverLength*llz);
	this.setCoverVertex(this.outer, this.topLeftEdge, -this.coverDepth/2+this.coverLength*llx-this.bindingWidth*lnx, .5*this.coverHeight, this.spineWidth+this.coverLength*llz-this.bindingWidth*lnz);
	this.setCoverVertex(this.outer, this.bottomLeftEdge, -this.coverDepth/2+this.coverLength*llx-this.bindingWidth*lnx, -.5*this.coverHeight, this.spineWidth+this.coverLength*llz-this.bindingWidth*lnz);
	
	this.setCoverVertex(this.inner, this.topRightEdge, this.coverDepth/2-this.coverLength*rlx, .5*this.coverHeight, this.spineWidth+this.coverLength*rlz);
	this.setCoverVertex(this.inner, this.bottomRightEdge, this.coverDepth/2-this.coverLength*rlx, -.5*this.coverHeight, this.spineWidth+this.coverLength*rlz);
	this.setCoverVertex(this.outer, this.topRightEdge, this.coverDepth/2-this.coverLength*rlx+this.bindingWidth*rnx, .5*this.coverHeight, this.spineWidth+this.coverLength*rlz-this.bindingWidth*rnz);
	this.setCoverVertex(this.outer, this.bottomRightEdge, this.coverDepth/2-this.coverLength*rlx+this.bindingWidth*rnx, -.5*this.coverHeight, this.spineWidth+this.coverLength*rlz-this.bindingWidth*rnz);
	
	for (var i=0;i<this.geoms.length;i++)
	{
		this.geoms[i].computeFaceNormals();
		this.geoms[i].computeVertexNormals();
		this.geoms[i].verticesNeedUpdate = true;
	}
}
