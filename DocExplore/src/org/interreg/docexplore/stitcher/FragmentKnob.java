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
	
	public void onDrag(Fragment fragment, double x, double y, boolean rightClick, boolean shift, boolean ctrl)
	{
		boolean resize = rightClick == shift || ctrl;
		boolean rotate = rightClick != shift || ctrl;
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
			fragment.setAngle(a);
		}
		FragmentKnob opp = values()[(ordinal()+2)%values().length];
		double tx = fragment.fromLocalX(opp.ax, opp.ay), ty = fragment.fromLocalY(opp.ax, opp.ay);
		fragment.setPos(fragment.uix+cx-tx, fragment.uiy+cy-ty);
	}
	
	public double angle(double cx, double cy, double x, double y)
	{
		double vx = x-cx, vy = y-cy;
		return (vy > 0 ? 1 : -1)*Math.acos(vx/Math.sqrt(vx*vx+vy*vy));
	}
}
