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

public class TestPathCollision
{
	static float projectOnPath(float x, float y, float [][] path, float [][] normals, float [] proj, float [] normal)
	{
		float minDist = (x-path[0][0])*(x-path[0][0])+(y-path[0][1])*(y-path[0][1]);
		
		for (int i=0;i<path.length-1;i++)
		{
			float k = dist(x, y, path[i][0], path[i][1], path[i+1][0]-path[i][0], path[i+1][1]-path[i][1]);
			float dist = Float.MAX_VALUE;
			if (k > 0 && k < 1)
			{
				float ix = path[i][0]+k*(path[i+1][0]-path[i][0]), iy = path[i][1]+k*(path[i+1][1]-path[i][1]);
				dist = (x-ix)*(x-ix)+(y-iy)*(y-iy);
			}
			if (dist < minDist)
			{
				minDist = dist;
				
				proj[0] = path[i][0]+k*(path[i+1][0]-path[i][0]);
				proj[1] = path[i][1]+k*(path[i+1][1]-path[i][1]);
				normal[0] = normals[i][0]+normals[i+1][0];
				normal[1] = normals[i][1]+normals[i+1][1];
				float nl = normal[0]*normal[0]+normal[1]*normal[1];
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
	
	static float dist(float x, float y, float px, float py, float vx, float vy) {return ((x*vx+y*vy)-(px*vx+py*vy))/(vx*vx+vy*vy);}
	
	public static void main(String [] args)
	{
		float [][] path = {{0, 0}, {.2f, .1f}, {.4f, .15f}, {.6f, .1f}, {.8f, 0}, {1, 0}};
		float [][] normals = new float [path.length][2];
		for (int i=0;i<path.length;i++)
		{
			float nx1 = 0, ny1 = 0, nx2 = 0, ny2 = 0;
			if (i > 0) {nx1 = path[i-1][1]-path[i][1]; ny1 = path[i][0]-path[i-1][0];}
			if (i < path.length-1) {nx2 = path[i][1]-path[i+1][1]; ny2 = path[i+1][0]-path[i][0];}
			float l1 = nx1*nx1+ny1*ny1, l2 = nx2*nx2+ny2*ny2;
			if (l1 > 0) {l1 = (float)Math.sqrt(l1); nx1 /= l1; ny1 /= l1;}
			if (l2 > 0) {l2 = (float)Math.sqrt(l2); nx2 /= l2; ny2 /= l2;}
			float nx = nx1+nx2, ny = ny1+ny2;
			float l = (float)Math.sqrt(nx*nx+ny*ny);
			nx /= l; ny /= l;
			
			normals[i][0] = nx; normals[i][1] = ny;
		}
		
		
	}
}
