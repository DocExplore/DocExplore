/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.roi;

public class Shape
{
	public float [][] points;
	public float minx, miny, maxx, maxy;
	
	public Shape(float [][] points)
	{
		this.points = points;
		minx = maxx = points[0][0];
		miny = maxy = points[0][1];
		
		for (int i=1;i<points.length;i++)
		{
			if (points[i][0] < minx) minx = points[i][0];
			else if (points[i][0] > maxx) maxx = points[i][0];
			if (points[i][1] < miny) miny = points[i][1];
			else if (points[i][1] > maxy) maxy = points[i][1];
		}
	}
	
	public boolean contains(float qx, float qy)
	{
		float vx = maxx+1-qx;
		float vy = maxy+1-qy;
		
		int nInters = 0;
		for (int i=0;i<points.length;i++)
		{
			float px = points[i][0];
			float py = points[i][1];
			
			float ux = points[(i+1)%points.length][0]-px;
			float uy = points[(i+1)%points.length][1]-py;
			
			float k = ((qx-px)*vy-(qy-py)*vx)/(ux*vy-uy*vx);
			if (k < 0 || k >= 1)
				continue;
			
			float kp = vx*vx > vy*vy ? (px+k*ux-qx)/vx : (py+k*uy-qy)/vy;
			if (kp >= 0 && kp < 1)
				nInters++;
		}
		
		return nInters%2 == 1;
	}
}
