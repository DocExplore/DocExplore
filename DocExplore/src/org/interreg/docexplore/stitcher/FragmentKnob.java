/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher;

public enum FragmentKnob
{
	TopLeft(0, 0),
	TopRight(1, 0),
	BottomRight(1, 1),
	BottomLeft(0, 1);
	
	double ax, ay;
	
	private FragmentKnob(double ax, double ay)
	{
		this.ax = ax;
		this.ay = ay;
	}
	
	public double dist2(double x, double y, Fragment fragment)
	{
		double kx = fragment.fromLocalX(ax, ay);
		double ky = fragment.fromLocalY(ax, ay);
		return (x-kx)*(x-kx)+(y-ky)*(y-ky);
	}
	
	double cx, cy, fa0, ma0;
	public void onGrab(Fragment fragment, double x, double y)
	{
		FragmentKnob opp = values()[(ordinal()+2)%values().length];
		cx = fragment.fromLocalX(opp.ax, opp.ay);
		cy = fragment.fromLocalY(opp.ax, opp.ay);
		fa0 = fragment.uiang;
		ma0 = angle(cx, cy, x, y);
	}
	
	private double lengthTo(double [] c, double [] p) {return lengthTo(c[0]-cx, c[1]-cy, p);}
	private double lengthTo(double vx, double vy, double [] p)
	{
		double k = ((p[0]-cx)*vx+(p[1]-cy)*vy)/(vx*vx+vy*vy);
		double ix = cx+k*vx, iy = cy+k*vy;
		return Math.sqrt((ix-cx)*(ix-cx)+(iy-cy)*(iy-cy));
	}
	private void firstCorner(Fragment fragment, double [] res)
	{
		res[0] = fragment.fromLocalX(1-ax, ay);
		res[1] = fragment.fromLocalY(1-ax, ay);
	}
	private void secondCorner(Fragment fragment, double [] res)
	{
		res[0] = fragment.fromLocalX(ax, 1-ay);
		res[1] = fragment.fromLocalY(ax, 1-ay);
	}
	private double dist2(double [] p1, double [] p2) {return (p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1]);}
	double [] firstCorner = {0, 0}, secondCorner = {0, 0}, nearCorner = {0, 0};
	public void onDrag(Fragment fragment, double x, double y, boolean rightClick, boolean shift, boolean ctrl, FragmentView view, double magnetRay)
	{
		boolean resize = !rightClick || ctrl;
		boolean rotate = rightClick || ctrl;
		boolean coarse = shift;
		if (resize)
		{
			double l = Math.sqrt((x-cx)*(x-cx)+(y-cy)*(y-cy));
			double vl = Math.sqrt(fragment.imagew*fragment.imagew+fragment.imageh*fragment.imageh);
			double w = l*fragment.imagew/vl;
			fragment.setSize(w);
		}
		if (rotate)
		{
			double ma = angle(cx, cy, x, y);
			while (ma-ma0 > Math.PI)
				ma -= 2*Math.PI;
			while (ma0-ma > Math.PI)
				ma += 2*Math.PI;
			double a = fa0+ma-ma0;
			while (a < 0)
				a += 2*Math.PI;
			while (a >= 2*Math.PI)
				a -= 2*Math.PI;
			if (coarse)
			{
				int step = (int)Math.round(a/(.125*Math.PI));
				a = step*.125*Math.PI;
			}
			fragment.setAngle(a);
		}
		FragmentKnob opp = values()[(ordinal()+2)%values().length];
		double tx = fragment.fromLocalX(opp.ax, opp.ay), ty = fragment.fromLocalY(opp.ax, opp.ay);
		fragment.setPos(fragment.uix+cx-tx, fragment.uiy+cy-ty);
		
		if (resize && !rotate)
		{
			firstCorner(fragment, firstCorner);
			secondCorner(fragment, secondCorner);
			boolean hor = true;
			double minDist2 = -1, dist2 = 0, scale = -1;
			for (int i=0;i<view.set.fragments.size();i++)
			{
				Fragment near = view.set.fragments.get(i);
				if (near == fragment)
					continue;
				for (int j=0;j<4;j++)
				{
					near.farCornerPoint(j, nearCorner);
					if ((dist2 = dist2(firstCorner, nearCorner)) < magnetRay*magnetRay && (minDist2 < 0 || dist2 < minDist2))
						{minDist2 = dist2; scale = lengthTo(firstCorner, nearCorner); hor = true;}
					if ((dist2 = dist2(secondCorner, nearCorner)) < magnetRay*magnetRay && (minDist2 < 0 || dist2 < minDist2))
						{minDist2 = dist2; scale = lengthTo(secondCorner, nearCorner); hor = false;}
				}
			}
			if (scale >= 0)
			{
				scale = hor ? fragment.uiw*scale/fragment.uih : scale;
				fragment.setSize(scale);
				opp = values()[(ordinal()+2)%values().length];
				tx = fragment.fromLocalX(opp.ax, opp.ay); ty = fragment.fromLocalY(opp.ax, opp.ay);
				fragment.setPos(fragment.uix+cx-tx, fragment.uiy+cy-ty);
			}
		}
	}
	
	public double angle(double cx, double cy, double x, double y)
	{
		double vx = x-cx, vy = y-cy;
		return (vy > 0 ? 1 : -1)*Math.acos(vx/Math.sqrt(vx*vx+vy*vy));
	}
}
