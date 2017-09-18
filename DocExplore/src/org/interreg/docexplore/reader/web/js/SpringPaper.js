var SpringPaperConstants = {};
SpringPaperConstants.nodeSpringSpread = 3;
SpringPaperConstants.cork = .16;
SpringPaperConstants.gravity = -.0006;
SpringPaperConstants.vlim = .1;
SpringPaperConstants.damping = .7;

function Spring(a, b)
{
	this.a = a;
	this.b = b;
	this.desired = a.node.distanceTo(b.node);
	this.buf = new THREE.Vector3();
	this.buf2 = new THREE.Vector3();
}

Spring.prototype.update = function()
{
	this.buf.copy(this.b.node); this.buf.sub(this.a.node);
	var dist = this.buf.length();
	this.buf.setLength(1);
	//console.log(dist+" "+this.desired);
	var cor = SpringPaperConstants.cork*(dist-this.desired);
	this.buf2.copy(this.buf); this.buf2.multiplyScalar(cor);
	this.a.v.add(this.buf2);
	this.buf2.copy(this.buf); this.buf2.multiplyScalar(-cor);
	this.b.v.add(this.buf2);
}

function SpringNode(node, i, j)
{
	this.isStatic = false;
	this.node = node;
	this.springs = [];
	this.v = new THREE.Vector3(0, 0, 0);
	this.buf = new THREE.Vector3(0, 0, 0);
	this.iindex = i;
	this.jindex = j;
}

SpringNode.prototype.attract = function(x, y, z, cor)
{
	var dx = x-this.node.x, dy = y-this.node.y, dz = z-this.node.z;
	var dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
	dx /= dist; dy /= dist; dz /= dist;
	this.v.x += cor*dx; this.v.y += cor*dy; this.v.z += cor*dz; 
}

SpringNode.prototype.addSpring = function(snode)
{
	var spring;
	this.springs.push(spring = new Spring(this, snode));
	return spring;
}

SpringNode.prototype.adjust = function()
{
	if (!this.isStatic)
	{
		if (this.v.lengthSq() > SpringPaperConstants.vlim*SpringPaperConstants.vlim)
			this.v.setLength(SpringPaperConstants.vlim);
	}
	else this.v.set(0, 0, 0);
}
SpringNode.prototype.update = function()
{
	if (!this.isStatic)
		this.node.add(this.v);
	this.v.multiplyScalar(SpringPaperConstants.damping);
}

SpringNode.prototype.dist2FromLine = function(a, u)
{
	var k = (this.node.dot(u)-a.dot(u))/u.dot(u);
	this.buf.copy(u);
	this.buf.multiplyScalar(k);
	this.buf.add(a);
	this.buf.sub(this.node);
	return this.buf.lengthSq();
}

function Grid(p, u, v, w, h)
{
	this.nodes = [];
	for (var i=0;i<w;i++)
	{
		this.nodes[i] = [];
		var ki = i*1./(w-1);
		for (var j=0;j<h;j++)
		{
			var kj = j*1./(h-1);
			this.nodes[i][j] = new THREE.Vector3(p.x+ki*u.x+kj*v.x, p.y+ki*u.y+kj*v.y, p.z+ki*u.z+kj*v.z);
		}
	}
}

Grid.prototype.write = function(frontGeom, backGeom)
{
	var w = this.nodes.length;
	var h = this.nodes[0].length;
	
	for (var i=0;i<w;i++)
		for (var j=0;j<h;j++)
	{
		var index = j*w+i;
		frontGeom.vertices[index].copy(this.nodes[i][j]);
		backGeom.vertices[index].copy(this.nodes[i][j]);
	}
}

function SpringPaper(p, u, v, w, h)
{
	this.grid = new Grid(p, u, v, w, h);
	this.snodes = [];
	
	for (var i=0;i<w;i++)
	{
		this.snodes[i] = [];
		for (var j=0;j<h;j++)
			this.snodes[i][j] = new SpringNode(this.grid.nodes[i][j], i, j);
	}
	
	this.springs = [];
	
	var spread = SpringPaperConstants.nodeSpringSpread;
	for (var k=1;k<spread+1;k++)
		for (var i=0;i<w;i++)
			for (var j=0;j<h;j++)
	{
		var snode = this.snodes[i][j];
		
		if (i < w-k)
			this.springs.push(snode.addSpring(this.snodes[i+k][j]));
		if (j < h-k)
			this.springs.push(snode.addSpring(this.snodes[i][j+k]));
		if (i < w-k && j < h-k)
		{
			this.springs.push(snode.addSpring(this.snodes[i+k][j+k]));
			this.springs.push(this.snodes[i][j+k].addSpring(this.snodes[i+k][j]));
		}
	}
//	var spread = 1;
//	for (var k=1;k<spread+1;k++)
//		for (var i=0;i<w;i++)
//			for (var j=0;j<h;j++)
//				for (var si=i-spread;si<=i+spread;si++)
//					for (var sj=j-spread;sj<=j+spread;sj++)
//						if (si*si+sj*sj > 0 && si >= 0 && si < this.snodes.length && sj >= 0 && sj < this.snodes[0].length)
//	{
//		var snode = this.snodes[i][j];
//		this.springs.push(snode.addSpring(this.snodes[si][sj]));
//	}
	console.log("springs : "+this.springs.length);
	
	this.buf = new THREE.Vector3();
}

SpringPaper.prototype.update = function()
{
	for (spring of this.springs)
		spring.update();
	for (snodeList of this.snodes)
		for (snode of snodeList)
	{
		if (!snode.isStatic)
			snode.v[1] += SpringPaperConstants.gravity;
		snode.adjust();
	}
	
	for (snodeList of this.snodes)
		for (snode of snodeList)
			snode.update();
	//this.grid.computeNormals();
}

SpringPaper.prototype.closestToLine = function(a, v)
{
	var w = this.snodes.length;
	var h = this.snodes[0].length;
	
	var min = null;
	var minDist = 0;
	
	for (var i=0;i<w;i++)
		for (var j=0;j<h;j++)
	{
		var snode = this.snodes[i][j];
		var d2 = snode.dist2FromLine(a, v);
		if (min === null || d2 < minDist)
		{
			min = snode;
			minDist = d2;
		}
	}
	return min;
}
