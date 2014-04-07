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
