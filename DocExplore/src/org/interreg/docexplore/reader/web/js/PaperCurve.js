function paperCurveCompute(x0, y0, vx0, vy0, cdx, cdy, length, steps, weight, res)
{
	var cl = Math.sqrt(cdx*cdx+cdy*cdy);
	cdx /= cl; cdy /= cl;
	var cpx = -cdy, cpy = cdx;
	if (cpx*vx0+cpy*vy0 < 0)
		{cpx = -cpx; cpy = -cpy;}
	
	var x = res[0][0] = x0, y = res[0][1] = y0;
	
	var vx = vx0, vy = vy0;
	var vl = Math.sqrt(vx*vx+vy*vy);
	vx /= vl; vy /= vl;
	
	flat = vy <= cdy;
	g = -.015*(vy-cdy)*weight;
	if (flat)
	{
		vx = cdx;
		vy = cdy;
	}
	
	var stepLength = length/steps;
	for (var i=0;i<steps;i++)
	{
		if (!flat)
		{
			vy += g*stepLength;
			var cd = -(x0*cpx+y0*cpy-x*cpx-y*cpy)/(cpx*cpx+cpy*cpy);
			var cv = (vx*cpx+vy*cpy)/(cpx*cpx+cpy*cpy);
			if (cv >= 0) ;
			else if (cd <= 0)
			{
				vx = cdx;
				vy = cdy;
			}
			else if (cv < 0)
			{
				var k = -cd/cv;
				k = stepLength*1./k;
				vx += k*cdx;
				vy += k*cdy;
			}
			
		}
		
		vl = Math.sqrt(vx*vx+vy*vy);
		vx /= vl; vy /= vl;
		
		x += stepLength*vx;
		y += stepLength*vy;
		res[i+1][0] = x;
		res[i+1][1] = y;
	}
}

function paperCurveNormalAt(i, path, seglength, n)
{
	var dx1 = path[i-1][0]-path[i][0], dy1 = path[i-1][1]-path[i][1];
	var dx2, dy2;
	if (i == path.length-1)
		{dx2 = -dx1; dy2 = -dy1;}
	else {dx2 = path[i+1][0]-path[i][0]; dy2 = path[i+1][1]-path[i][1];}
	
	n[0] = dy1-dy2; n[1] = dx2-dx1;
	nl = Math.sqrt(n[0]*n[0]+n[1]*n[1]); 
	n[0] /= nl; n[1] /= nl;
}

function paperCurveSlideToLength(px, py, cx, cy, vx, vy, length)
{
	var a = vx*vx+vy*vy;
	var b = 2*(cx*vx+cy*vy-px*vx-py*vy);
	var c = (cx-px)*(cx-px)+(cy-py)*(cy-py)-length*length;
	var d = Math.sqrt(b*b-4*a*c);
	return (d-b)/(2*a);
}

function paperCurveProject(path, dx, dy, length, res)
{
	var dist = Math.sqrt(dx*dx+dy*dy);
	
	var normal = [0, 0];
	normal[0] = path[0][1]-path[1][1];
	normal[1] = path[1][0]-path[0][0];
	var nl = Math.sqrt(normal[0]*normal[0]+normal[1]*normal[1]);
	normal[0] /= nl; normal[1] /= nl;
	var reverse = dx*normal[0]+dy*normal[1] < 0;
	if (reverse)
		{normal[0] = -normal[0]; normal[1] = -normal[1];}
	
	var nSubPoints = Math.floor(dist*path.length/length)+1;
	
	var buf = [[path[0][0]+dx, path[0][1]+dy]];
	for (var i=0;i<nSubPoints;i++)
	{
		var k = (i+1)*1./(nSubPoints+1);
		var nx = (1-k)*dx/dist+k*normal[0];
		var ny = (1-k)*dy/dist+k*normal[1];
		nl = Math.sqrt(nx*nx+ny*ny);
		nx /= nl; ny /= nl;
		
		buf[i+1] = [path[0][0]+dist*nx, path[0][1]+dist*ny]; 
	}
	
	for (var i=1;i<path.length;i++)
	{
		paperCurveNormalAt(i, path, length/(path.length-1), normal);
		if (reverse)
			{normal[0] = -normal[0]; normal[1] = -normal[1];}
		buf[i+nSubPoints] = [path[i][0]+dist*normal[0], path[i][1]+dist*normal[1]];
	}
	
	res[0][0] = buf[0][0];
	res[0][1] = buf[0][1];
	var pathIndex = 0;
	for (var i=1;i<path.length;i++)
	{
		var k = paperCurveSlideToLength(res[i-1][0], res[i-1][1], res[i-1][0], res[i-1][1], 
			buf[pathIndex+1][0]-res[i-1][0], buf[pathIndex+1][1]-res[i-1][1], length/(path.length-1));
		if (k <= 1)
		{
			res[i][0] = res[i-1][0]+k*(buf[pathIndex+1][0]-res[i-1][0]);
			res[i][1] = res[i-1][1]+k*(buf[pathIndex+1][1]-res[i-1][1]);
		}
		else
		{
			while (k > 1)
			{
				if (pathIndex+1 == buf.length-1)
					break;
				pathIndex++;
				k = paperCurveSlideToLength(res[i-1][0], res[i-1][1], buf[pathIndex][0], buf[pathIndex][1], 
					buf[pathIndex+1][0]-buf[pathIndex][0], buf[pathIndex+1][1]-buf[pathIndex][1], 
					length/(path.length-1));
			}
			res[i][0] = buf[pathIndex][0]+k*(buf[pathIndex+1][0]-buf[pathIndex][0]);
			res[i][1] = buf[pathIndex][1]+k*(buf[pathIndex+1][1]-buf[pathIndex][1]);
		}
	}
}
