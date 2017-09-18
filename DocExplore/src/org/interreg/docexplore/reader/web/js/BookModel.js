var BookModelConstants = {};
BookModelConstants.gridSize = 15;

function BookModel(length, height)
{
	this.length = length;
	this.height = height;
	
	var coverDepth = Math.min(.08, Spec.pages.length*.00125);
	this.cover = new BookCover(length, height, coverDepth, .005, .05, 10);
	this.nPages = Spec.pages.length/2;
	this.leftStack = new BookPageStack(this.cover, this.nPages, .95, .95, 2*BookModelConstants.gridSize, true);
	this.rightStack = new BookPageStack(this.cover, this.nPages, .95, .95, 2*BookModelConstants.gridSize, false);
	this.maxCoverAngle = 1.*Math.PI/2;
	this.minCoverAngle = .001*Math.PI/2;
	this.modelRotation = 0;
	this.leftCoverAng = -.01;
	this.rightCoverAng = -.01;
	this.openCoverAng = .9;
	this.inc = 1./30.;
	this.isAnimating = false;
	this.page = new BookPage(this);
	this.page.model.visible = false;
	
	this.model = new THREE.Group();
	this.model.add(this.cover.model);
	this.model.add(this.leftStack.model);
	this.model.add(this.rightStack.model);
	this.model.frustumCulled = false;
	
	this.buf1 = new THREE.Vector3();
	this.buf2 = new THREE.Vector3();
	this.bufv = [0, 0];
	
	this.update();
}

BookModel.prototype.setLeftTex = function(tex) {this.leftStack.setTex(tex);}
BookModel.prototype.setRightTex = function(tex) {this.rightStack.setTex(tex);}

function smooth(x) {return 3*x*x-2*x*x*x;}
function smoothCover(x)
{
	if (x <= this.openCoverAng)
	{
		x /= this.openCoverAng;
		return smooth(x)*this.openCoverAng;
	}
	return x;
}
function sudden(x) {return x*x*x;}

BookModel.prototype.getCoverAngle = function(v)
{
	return this.minCoverAngle+v*(this.maxCoverAngle-this.minCoverAngle);
}

BookModel.prototype.update = function()
{
	var coverNeedsUpdate = false, leftStackNeedsUpdate = false, rightStackNeedsUpdate = false;
	
	var leftPagesTarget = Math.max(0, Math.min(this.nPages, (Reader.currentPage+1)/2));
	var rightPagesTarget = Math.max(0, this.nPages-leftPagesTarget-(Hand.active ? 1 : 0));
	stackNeedsUpdate = leftPagesTarget != this.leftStack.nStackPages || rightPagesTarget != this.rightStack.nStackPages;
	
	var leftCoverClosed = Reader.currentPage == -3, rightCoverClosed = Reader.currentPage == Spec.pages.length+1;
	var leftCoverAngTarget = leftCoverClosed || rightCoverClosed ? 0 : Reader.zoomed ? 1 : this.openCoverAng;
	var rightCoverAngTarget = leftCoverClosed || rightCoverClosed ? 0 : Reader.zoomed ? 1 : this.openCoverAng;
	var modelRotationTarget = leftCoverClosed ? 1 : rightCoverClosed ? -1 : 0;
	var coverNeedsUpdate = leftCoverAngTarget != this.leftCoverAng || rightCoverAngTarget != this.rightCoverAng;
	var modelTransformNeedsUpdate = modelRotationTarget != this.modelRotation;
	//console.log(leftCoverClosed+" "+rightCoverClosed);
	if (coverNeedsUpdate)
	{
		if (Math.abs(this.leftCoverAng-leftCoverAngTarget) < this.inc)
			this.leftCoverAng = leftCoverAngTarget;
		else this.leftCoverAng = Math.max(0, Math.min(1, this.leftCoverAng+this.inc*(this.leftCoverAng < leftCoverAngTarget ? 1 : -1)));
		if (Math.abs(this.rightCoverAng-rightCoverAngTarget) < this.inc)
			this.rightCoverAng = rightCoverAngTarget;
		else this.rightCoverAng = Math.max(0, Math.min(1, this.rightCoverAng+this.inc*(this.rightCoverAng < rightCoverAngTarget ? 1 : -1)));
		this.cover.setAngle(this.getCoverAngle(smoothCover(this.leftCoverAng)), this.getCoverAngle(smoothCover(this.rightCoverAng)));
	}
	if (stackNeedsUpdate || coverNeedsUpdate)
	{
		this.leftStack.nStackPages = leftPagesTarget;
		this.rightStack.nStackPages = rightPagesTarget;
		this.leftStack.updateStack();
		this.rightStack.updateStack();
	}
	if (modelTransformNeedsUpdate)
	{
		if (Math.abs(this.modelRotation-modelRotationTarget) < this.inc)
			this.modelRotation = modelRotationTarget;
		else this.modelRotation = Math.max(-1, Math.min(1, this.modelRotation+this.inc*(this.modelRotation < modelRotationTarget ? 1 : -1)));
		this.model.rotation.set(.05*Math.PI*(sudden(1-Math.abs(this.modelRotation))), .5*Math.PI*sudden(this.modelRotation), 0);
		this.model.position.set(-.5*this.length*this.modelRotation, 0, Reader.heightBound ? 0 : Math.abs(this.modelRotation));
	}
	this.page.model.rotation.copy(this.model.rotation);
	this.page.model.position.copy(this.model.position);
	
	this.isAnimating = stackNeedsUpdate || coverNeedsUpdate || modelTransformNeedsUpdate;
	
	this.page.update();
}

BookModel.prototype.closestToLine = function(a, v)
{
	this.buf1.copy(a);
	this.buf2.copy(v);
	this.model.worldToLocal(this.buf1);
	this.model.worldToLocal(this.buf2);
	var snode = this.page.paper.closestToLine(this.buf1, this.buf2);
	snode = this.page.paper.snodes[this.page.paper.snodes.length-1][snode.jindex];
	return snode;
}

BookModel.prototype.fromPageToWorld = function(x, y, left, res)
{
	if (left) this.leftStack.toBookModel(1-x, y, res);
	else this.rightStack.toBookModel(x, y, res);
	this.model.localToWorld(res);
}

BookModel.prototype.proj = function(px1, py1, vx1, vy1, px2, py2, vx2, vy2, res)
{
	res[0] = (px2*vy2-py2*vx2-px1*vy2+py1*vx2)/(vx1*vy2-vy1*vx2);
	res[1] = vx2*vx2 > vy2*vy2 ? (px1+res[0]*vx1-px2)/vx2 : (py1+res[0]*vy1-py2)/vy2;
}
BookModel.prototype.toPage = function(p, d)
{
	var left = p.x-d.z*d.x < 0;
	var stack = left ? this.leftStack : this.rightStack;
	if (stack.nStackPages == 0)
		return [left, null];
	
	var mink = -1, cx = 0;
	for (var i=0;i<stack.projection.length-1;i++)
	{
		var vx = stack.projection[i+1][0]-stack.projection[i][0],
			vy = stack.projection[i+1][1]-stack.projection[i][1];
		this.proj(p.x, p.z, d.x, d.z, stack.projection[i][0], stack.projection[i][1], vx, vy, this.bufv);
		if (this.bufv[1] < 0 || this.bufv[1] > 1)
			continue;
		if (mink < 0 || this.bufv[0] < mink)
			{mink = this.bufv[0]; cx = (i+this.bufv[1])/stack.projection.length;}
	}
	if (mink < 0)
		return null;
	
	//TODO: ugly hack
	cx *= 1.03;
	if (left)
		cx = 1-cx;
	var cy = 1-(p.y+mink*d.y+.5*this.cover.coverHeight-stack.margin)/(this.cover.coverHeight-2*stack.margin);
	return [left, [cx, cy]];
}
