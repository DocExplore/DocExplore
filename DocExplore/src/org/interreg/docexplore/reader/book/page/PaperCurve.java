/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.page;

public class PaperCurve
{
	public static void compute(float x0, float y0, float vx0, float vy0, 
		float cdx, float cdy, float length, int steps, float weight,
		float [][] res)
	{
		float cl = (float)Math.sqrt(cdx*cdx+cdy*cdy);
		cdx /= cl; cdy /= cl;
		float cpx = -cdy, cpy = cdx;
		if (cpx*vx0+cpy*vy0 < 0)
			{cpx = -cpx; cpy = -cpy;}
		
		float x = res[0][0] = x0, y = res[0][1] = y0;
		
		float vx = vx0, vy = vy0;
		float vl = (float)Math.sqrt(vx*vx+vy*vy);
		vx /= vl; vy /= vl;
		
		boolean flat = vy <= cdy;
		float g = -.015f*(vy-cdy)*weight;
		if (flat)
		{
			vx = cdx;
			vy = cdy;
		}
		
		float stepLength = length/steps;
		for (int i=0;i<steps;i++)
		{
			if (!flat)
			{
				vy += g*stepLength;
				float cd = -(x0*cpx+y0*cpy-x*cpx-y*cpy)/(cpx*cpx+cpy*cpy);
				float cv = (vx*cpx+vy*cpy)/(cpx*cpx+cpy*cpy);
				if (cv >= 0) ;
				else if (cd <= 0)
				{
					vx = cdx;
					vy = cdy;
				}
				else if (cv < 0)
				{
					float k = -cd/cv;
					//float k = (x0*cpx+y0*cpy-(x+proj*length*vx)*cpx-(y+proj*length*vy)*cpy)/(cpx*cpx+cpy*cpy);
					//k = k > 0 ? (k < 1f ? 1f : k) : 0;
					k = 1f*stepLength/k;
					//if (k < 0) System.out.println(k);
					vx += k*(.00f*cpx+cdx);
					vy += k*(.00f*cpy+cdy);
				}
				
			}
			
			vl = (float)Math.sqrt(vx*vx+vy*vy);
			vx /= vl; vy /= vl;
			
			x += stepLength*vx;
			y += stepLength*vy;
			res[i+1][0] = x;
			res[i+1][1] = y;
		}
	}
	
	public static void compute2(float x0, float y0, float vx0, float vy0, 
			float cdx, float cdy, float length, int steps, float projection,
			float [][] res)
		{
			float cl = (float)Math.sqrt(cdx*cdx+cdy*cdy);
			cdx /= cl; cdy /= cl;
			float cpx = -cdy, cpy = cdx;
			
			float x = res[0][0] = x0, y = res[0][1] = y0;
			
			float vx = vx0, vy = vy0;
			float vl = (float)Math.sqrt(vx*vx+vy*vy);
			vx /= vl; vy /= vl;
			
			float stepLength = length/steps;
			for (int i=0;i<steps;i++)
			{
				float cck = .005f;
				float pck = .1f;
				float inertia = .8f;
				
				//close to cover
				float k = 0;
				//if ((x*cdx+y*cdy-cx1*cdx-cy1*cdy)/(cdx*cdx+cdy*cdy) > 0)
				k = cck*(x0*cpx+y0*cpy-(x+projection*stepLength*vx)*cpx-(y+projection*stepLength*vy)*cpy)/(cpx*cpx+cpy*cpy);
				float ccx = k*cpx, ccy = k*cpy;
				
				//parallel to cover
				float pcx = pck*cdx, pcy = pck*cdy;
				
				//average
				vx = (inertia*vx+ccx+pcx)/(inertia+2);
				vy = (inertia*vy+ccy+pcy)/(inertia+2);
				vl = (float)Math.sqrt(vx*vx+vy*vy);
				vx /= vl; vy /= vl;
				
				x += stepLength*vx;
				y += stepLength*vy;
				res[i+1][0] = x;
				res[i+1][1] = y;
			}
		}
	
	static void normalAt(int i, float [][] path, float seglength, float [] n)
	{
		float dx1 = path[i-1][0]-path[i][0], dy1 = path[i-1][1]-path[i][1];
		float dx2, dy2;
		if (i == path.length-1)
			{dx2 = -dx1; dy2 = -dy1;}
		else {dx2 = path[i+1][0]-path[i][0]; dy2 = path[i+1][1]-path[i][1];}
		
		n[0] = dy1-dy2; n[1] = dx2-dx1;
		float nl = (float)Math.sqrt(n[0]*n[0]+n[1]*n[1]); 
		n[0] /= nl; n[1] /= nl;
	}
	
	static float slideToLength(float px, float py, float cx, float cy, float vx, float vy, float length)
	{
		float a = vx*vx+vy*vy;
		float b = 2*(cx*vx+cy*vy-px*vx-py*vy);
		float c = (cx-px)*(cx-px)+(cy-py)*(cy-py)-length*length;
		float d = (float)Math.sqrt(b*b-4*a*c);
		return (d-b)/(2*a);
	}
	
	public static void project(float [][] path, float dx, float dy, float length, float [][] res)
	{
		float dist = (float)Math.sqrt(dx*dx+dy*dy);
		
		float [] normal = {0, 0};
		normal[0] = path[0][1]-path[1][1];
		normal[1] = path[1][0]-path[0][0];
		float nl = (float)Math.sqrt(normal[0]*normal[0]+normal[1]*normal[1]);
		normal[0] /= nl; normal[1] /= nl;
		boolean reverse = dx*normal[0]+dy*normal[1] < 0;
		if (reverse)
			{normal[0] = -normal[0]; normal[1] = -normal[1];}
		
		int nSubPoints = (int)(dist*path.length/length)+1;
		
		float [][] buf = new float [path.length+nSubPoints][2];
		buf[0][0] = path[0][0]+dx;
		buf[0][1] = path[0][1]+dy;
		
		for (int i=0;i<nSubPoints;i++)
		{
			float k = (i+1)*1.f/(nSubPoints+1);
			float nx = (1-k)*dx/dist+k*normal[0];
			float ny = (1-k)*dy/dist+k*normal[1];
			nl = (float)Math.sqrt(nx*nx+ny*ny);
			nx /= nl; ny /= nl;
			
			buf[i+1][0] = path[0][0]+dist*nx;
			buf[i+1][1] = path[0][1]+dist*ny; 
		}
		
		for (int i=1;i<path.length;i++)
		{
			normalAt(i, path, length/(path.length-1), normal);
			if (reverse)
				{normal[0] = -normal[0]; normal[1] = -normal[1];}
			buf[i+nSubPoints][0] = path[i][0]+dist*normal[0];
			buf[i+nSubPoints][1] = path[i][1]+dist*normal[1];
		}
		
		res[0][0] = buf[0][0];
		res[0][1] = buf[0][1];
		int pathIndex = 0;
		for (int i=1;i<path.length;i++)
		{
			float k = slideToLength(res[i-1][0], res[i-1][1], res[i-1][0], res[i-1][1], 
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
					k = slideToLength(res[i-1][0], res[i-1][1], buf[pathIndex][0], buf[pathIndex][1], 
						buf[pathIndex+1][0]-buf[pathIndex][0], buf[pathIndex+1][1]-buf[pathIndex][1], 
						length/(path.length-1));
				}
				res[i][0] = buf[pathIndex][0]+k*(buf[pathIndex+1][0]-buf[pathIndex][0]);
				res[i][1] = buf[pathIndex][1]+k*(buf[pathIndex+1][1]-buf[pathIndex][1]);
			}
		}
	}
}
