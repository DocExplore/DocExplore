var BookPageConstants = {};
BookPageConstants.springGridSize = 12;
BookPageConstants.collideCor = .0000001;
BookPageConstants.collideLim = .00001;
BookPageConstants.springUpdateLoops = 10;
BookPageConstants.pinchLength = .95;

function BookPage(book)
{
	this.book = book;
	
	var a = -Math.PI/10.;
	this.paper = new SpringPaper(
		new THREE.Vector3(book.leftStack.projection[0][0], .5*book.cover.coverHeight-book.leftStack.margin, book.leftStack.projection[0][1]), //p
		new THREE.Vector3(Math.sin(a)*book.leftStack.pageWidth*book.cover.coverLength, 0, Math.cos(a)*book.leftStack.pageWidth*book.cover.coverLength), //u
		new THREE.Vector3(0, -(book.cover.coverHeight-2*book.leftStack.margin), 0), //v
		BookModelConstants.gridSize, BookModelConstants.gridSize);
		//3, 3);
	for (var i=0;i<this.paper.snodes[0].length;i++)
		this.paper.snodes[0][i].isStatic = true;
	
	this.frontPageGeom = new THREE.Geometry();
	this.backPageGeom = new THREE.Geometry();
	
	var w = this.paper.snodes.length;
	var h = this.paper.snodes[0].length;
	
	for (var i=0;i<this.paper.snodes.length;i++)
		for (var j=0;j<this.paper.snodes[0].length;j++)
	{
		this.frontPageGeom.vertices.push(new THREE.Vector3());
		this.backPageGeom.vertices.push(new THREE.Vector3());
	}
	
	for (var i=0;i<w-1;i++)
		for (var j=0;j<h-1;j++)
	{
		this.frontPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+i, (j+1)*w+(i+1)));
		this.frontPageGeom.faceVertexUvs[0].push([new THREE.Vector2(i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2(i*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2((i+1)*1./(w-1), 1-(j+1)*1./(w-1))]);
		this.frontPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+(i+1), j*w+(i+1)));
		this.frontPageGeom.faceVertexUvs[0].push([new THREE.Vector2(i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2((i+1)*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2((i+1)*1./(w-1), 1-j*1./(w-1))]);
		this.backPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+i, (j+1)*w+(i+1)));
		this.backPageGeom.faceVertexUvs[0].push([new THREE.Vector2(1-i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2(1-i*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2(1-(i+1)*1./(w-1), 1-(j+1)*1./(w-1))]);
		this.backPageGeom.faces.push(new THREE.Face3(j*w+i, (j+1)*w+(i+1), j*w+(i+1)));
		this.backPageGeom.faceVertexUvs[0].push([new THREE.Vector2(1-i*1./(w-1), 1-j*1./(w-1)), new THREE.Vector2(1-(i+1)*1./(w-1), 1-(j+1)*1./(w-1)), new THREE.Vector2(1-(i+1)*1./(w-1), 1-j*1./(w-1))]);
	}
	
	this.frontPageMesh = new THREE.Mesh(this.frontPageGeom, new THREE.MeshLambertMaterial({map: Spec.loadingTex}));
	this.frontPageMesh.material.side = THREE.FrontSide;
	this.frontPageMesh.material.shading = THREE.SmoothShading;
	this.frontPageMesh.renderOrder = 1;
	this.frontPageMesh.onBeforeRender = function(renderer) {renderer.clearDepth();};
	this.backPageMesh = new THREE.Mesh(this.backPageGeom, new THREE.MeshLambertMaterial({map: Spec.loadingTex}));
	this.backPageMesh.material.side = THREE.BackSide;
	this.backPageMesh.material.shading = THREE.SmoothShading;
	this.backPageMesh.renderOrder = 2;
	
	this.model = new THREE.Group();
	this.model.add(this.frontPageMesh);
	this.model.add(this.backPageMesh);
	this.model.visible = false;
	this.model.renderOrder = 99;
	
	this.opacity = 0;
	
	this.buf1 = [0, 0];
	this.buf2 = [0, 0];
	this.bufv = new THREE.Vector3();
}

BookPage.prototype.setPageOpacity = function(val)
{
	
	if (val == 0)
		this.model.visible = false;
	else
	{
		this.model.visible = true;
		this.frontPageMesh.material.transparent = true;
		this.backPageMesh.material.transparent = true;
		this.frontPageMesh.material.opacity = val;
		this.backPageMesh.material.opacity = val;
	}
	this.opacity = val;
}

BookPage.prototype.writeGeoms = function()
{
	this.paper.grid.write(this.frontPageGeom, this.backPageGeom);
	this.frontPageGeom.computeFaceNormals();
	this.frontPageGeom.computeVertexNormals();
	this.frontPageGeom.verticesNeedUpdate = true;
	this.backPageGeom.computeFaceNormals();
	this.backPageGeom.computeVertexNormals();
	this.backPageGeom.verticesNeedUpdate = true;
}

BookPage.prototype.setFrontTex = function(front) {this.frontPageMesh.material.map = front;}
BookPage.prototype.setBackTex = function(back) {this.backPageMesh.material.map = back;}

BookPage.prototype.setTo = function(stack)
{
	for (var i=0;i<this.paper.snodes.length;i++)
	{
		var l = i*this.book.leftStack.pageWidth*this.book.cover.coverLength/(this.paper.snodes.length-1);
		stack.projectionAt(l, this.buf1);
		for (var j=0;j<this.paper.snodes[0].length;j++)
			this.paper.snodes[i][j].node.set(this.buf1[0], this.paper.snodes[0][j].node.y, this.buf1[1]);
	}
	this.writeGeoms();
}

BookPage.prototype.updateHeldNode = function(snode, x)
{
	var length = BookPageConstants.pinchLength*this.book.cover.coverLength;
	var ca = Math.max(-.95, Math.min(.95, x/this.book.cover.coverLength));
	var sa = Math.sqrt(1-ca*ca);
	this.bufv.set(this.paper.snodes[0][0].node.x+ca*length, this.paper.snodes[0][snode.jindex].node.y, this.paper.snodes[0][0].node.z+sa*length);
	snode.node.copy(this.bufv);
}

BookPage.prototype.update = function()
{
	if (!Hand.active)
	{
		this.setPageOpacity(Math.max(0, this.opacity-.1));
		return;
	}
	this.setPageOpacity(1.);
	
	this.book.leftStack.projectionAt(0, this.buf1);
	this.book.rightStack.projectionAt(0, this.buf2);
		
	for (var j=0;j<this.paper.snodes[0].length;j++)
	{
		this.paper.snodes[0][j].node.x = (this.buf1[0]+this.buf2[0])/2;
		this.paper.snodes[0][j].node.z = (this.buf1[1]+this.buf2[1])/2;
	}
	
	if (Hand.grabbedNode !== null)
		this.updateHeldNode(Hand.grabbedNode, Hand.grabbedX);
	
	for (var k=0;k<BookPageConstants.springUpdateLoops;k++)
	{
		if (Hand.grabbedNode === null)
			this.attractToStack(Hand.goLeft);
		
		this.paper.update();
		
		for (var i=0;i<this.paper.snodes.length;i++)
			for (var j=0;j<this.paper.snodes[0].length;j++)
		{
			var snode = this.paper.snodes[i][j];
			if (snode.node.x < this.book.leftStack.projection[0][0])
				this.collide(snode, this.book.leftStack);
			if (snode.node.x > this.book.rightStack.projection[0][0])
				this.collide(snode, this.book.rightStack);
		}
	}
	this.writeGeoms();
}

BookPage.prototype.collide = function(snode, stack)
{
	if (snode.isStatic)
		return;
	var dist = this.projectOnPath(snode.node.x, snode.node.z, 
		stack.projection, stack.normals, this.buf1, this.buf2);
	if (dist < BookPageConstants.collideLim)
		return;
	if ((snode.node.x-this.buf1[0])*this.buf2[0]+(snode.node.z-this.buf1[1])*this.buf2[1] > 0)
		return;
	
	dist = Math.sqrt(dist);
	var cx = (this.buf1[0]-snode.node.x)/dist, cy = (this.buf1[1]-snode.node.z)/dist;
	snode.node.x += cx*(dist+BookPageConstants.collideCor);
	snode.node.z += cy*(dist+BookPageConstants.collideCor);
}

BookPage.prototype.projectOnPath = function(x, y, path, normals, proj, normal)
{
	var minDist = (x-path[0][0])*(x-path[0][0])+(y-path[0][1])*(y-path[0][1]);
	
	for (var i=0;i<path.length-1;i++)
	{
		var k = this.dist(x, y, path[i][0], path[i][1], path[i+1][0]-path[i][0], path[i+1][1]-path[i][1]);
		var dist = 99999999;
		if (k > 0 && k < 1)
		{
			var ix = path[i][0]+k*(path[i+1][0]-path[i][0]), iy = path[i][1]+k*(path[i+1][1]-path[i][1]);
			dist = (x-ix)*(x-ix)+(y-iy)*(y-iy);
		}
		if (dist < minDist)
		{
			minDist = dist;
			
			proj[0] = path[i][0]+k*(path[i+1][0]-path[i][0]);
			proj[1] = path[i][1]+k*(path[i+1][1]-path[i][1]);
			normal[0] = normals[i][0]+normals[i+1][0];
			normal[1] = normals[i][1]+normals[i+1][1];
			var nl = Math.sqrt(normal[0]*normal[0]+normal[1]*normal[1]);
			normal[0] /= nl; normal[1] /= nl;
		}
		
		dist = (x-path[i+1][0])*(x-path[i+1][0])+(y-path[i+1][1])*(y-path[i+1][1]);
		if (dist < minDist)
		{
			minDist = dist;
			
			proj[0] = path[i+1][0];
			proj[1] = path[i+1][1];
			normal[0] = normals[i+1][0];
			normal[1] = normals[i+1][1];
		}
	}
	return minDist;
}
BookPage.prototype.dist = function(x, y, px, py, vx, vy) {return ((x*vx+y*vy)-(px*vx+py*vy))/(vx*vx+vy*vy);}

BookPage.prototype.attractToStack = function(headedLeft)
{
	var stack = headedLeft ? this.book.leftStack : this.book.rightStack;
	for (var i=1;i<this.paper.snodes.length;i++)
	{
		var k = i*1./(this.paper.snodes.length-1);
		
		stack.projectionAt(.99*k*this.book.cover.coverLength, this.buf1);
		for (var j=0;j<this.paper.snodes[0].length;j++)
		{//console.log(""+.1*(this.buf1[0]-this.paper.snodes[i][j].node.x));
			this.paper.snodes[i][j].v.x += (.7+.3*(1-k))*.01*(this.buf1[0]-this.paper.snodes[i][j].node.x);
			this.paper.snodes[i][j].v.y += (.7+.3*(1-k))*.01*(this.paper.snodes[0][j].node.y-this.paper.snodes[i][j].node.y);
			this.paper.snodes[i][j].v.z += (.7+.3*(1-k))*.01*(this.buf1[1]-this.paper.snodes[i][j].node.z);
		}
	}
}
