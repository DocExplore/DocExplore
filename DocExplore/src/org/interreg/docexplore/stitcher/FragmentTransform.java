package org.interreg.docexplore.stitcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FragmentTransform implements Serializable
{
	private static final long serialVersionUID = -303081516623249171L;
	
	double dx, dy;
	double dang, dw;
	
	public FragmentTransform(double dx, double dy, double dw, double dang)
	{
		this.dx = dx;
		this.dy = dy;
		this.dang = dang;
		this.dw = dw;
	}
	
	public void transform(Fragment f, Fragment res)
	{
		res.uix = f.fromLocalX(dx, dy);
		res.uiy = f.fromLocalY(dx, dy);
		res.uiw = res.imagew*f.uiw*dw/f.imagew;
		res.uiang = f.uiang+dang;
		res.update();
	}
	
	public void itransform(Fragment f, Fragment res)
	{
		double tuix = res.fromLocalX(dx, dy), tuiy = res.fromLocalY(dx, dy);
		double tuiw = f.imagew*res.uiw*dw/res.imagew;
		double tuiang = res.uiang+dang;
		double tux = tuiw*Math.cos(tuiang), tuy = tuiw*Math.sin(tuiang);
		double tvx = -f.imageh*tuy/f.imagew, tvy = f.imageh*tux/f.imagew;
		double idx = ((res.uix-tuix)*tux+(res.uiy-tuiy)*tuy)/(tux*tux+tuy*tuy);
		double idy = ((res.uix-tuix)*tvx+(res.uiy-tuiy)*tvy)/(tvx*tvx+tvy*tvy);
		
		res.uix = f.fromLocalX(idx, idy);
		res.uiy = f.fromLocalY(idx, idy);
		res.uiw = res.imagew*f.uiw/(f.imagew*dw);
		res.uiang = f.uiang-dang;
		res.update();
	}
	
	public static FragmentTransform build(final FragmentAssociation fa, float [] progress)
	{
		return build(fa, progress, 0, 1);
	}
	
	public static FragmentTransform build(final FragmentAssociation fa, float [] progress, float ps, float pe)
	{
		if (progress != null) progress[0] = ps;
		
		final List<POI> features = new ArrayList<POI>(fa.associations.size());
		for (int i=0;i<fa.d2.features.size();i++)
			if (fa.associationsByPOI.containsKey(fa.d2.features.get(i)))
				features.add(fa.d2.features.get(i));
		int n = features.size();
		
		final double [] pos = new double [2*n];
		for (int i=0;i<n;i++)
		{
			POI poi = features.get(i);
			List<Association> list = fa.associationsByPOI.get(poi);
			pos[2*i] = pos[2*i+1] = 0;
			for (int j=0;j<list.size();j++)
			{
				pos[2*i] += list.get(j).other(poi).x;
				pos[2*i+1] += list.get(j).other(poi).y;
			}
			pos[2*i] /= list.size();
			pos[2*i+1] /= list.size();
		}
		if (progress != null) progress[0] = ps+.2f*(pe-ps);
		
		double cx = 0, cy = 0, dcx = 0, dcy = 0;
		for (int i=0;i<n;i++)
		{
			cx += features.get(i).x;
			cy += features.get(i).y;
			dcx += pos[2*i];
			dcy += pos[2*i+1];
		}
		cx /= n; cy /= n; dcx /= n; dcy /= n;
		if (progress != null) progress[0] = ps+.4f*(pe-ps);
		
		double rot = 0;
		int cnt = 0;
		for (int i=0;i<n;i++)
		{
			double a = computeDesiredRot(features.get(i).x-cx, features.get(i).y-cy, pos[2*i]-dcx, pos[2*i+1]-dcy);
			while (rot-a > Math.PI) a += 2*Math.PI;
			while (rot-a < -Math.PI) a -= 2*Math.PI;
			rot += (a-rot)/(cnt+1);
			cnt++;
		}
		if (progress != null) progress[0] = ps+.6f*(pe-ps);
		
		double scale = 0;
		for (int i=0;i<n;i++)
			scale += computeDesiredScale(features.get(i).x-cx, features.get(i).y-cy, pos[2*i]-dcx, pos[2*i+1]-dcy);
		scale /= n;
		if (progress != null) progress[0] = ps+.8f*(pe-ps);
		
		double ca = Math.cos(rot), sa = Math.sin(rot);
		for (int i=0;i<n;i++)
		{
			transform(features.get(i).x-cx, features.get(i).y-cy, ca, sa, scale, pos, 2*i);
			pos[2*i] += dcx;
			pos[2*i+1] += dcy;
		}
		transform(-cx, -cy, ca, sa, scale, pos, 0);
		pos[0] += dcx;
		pos[1] += dcy;
		double xi = fa.d1.fragment.fromImageToLocalX(pos[0]);
		double yi = fa.d1.fragment.fromImageToLocalY(pos[1]);
		if (progress != null) progress[0] = pe;
		
		return new FragmentTransform(xi, yi, scale, rot);
	}
	
	private static void transform(double x, double y, double ca, double sa, double scale, double [] res, int index)
	{
		x *= scale;
		y *= scale;
		res[index] = x*ca-y*sa;
		res[index+1] = x*sa+y*ca;
	}
	//rotation for u to align with v
	private static double computeDesiredRot(double ux, double uy, double vx, double vy)
	{
		double da = Math.acos((ux*vx+uy*vy)/(Math.sqrt(ux*ux+uy*uy)*Math.sqrt(vx*vx+vy*vy)));
		return ux*vy-uy*vx < 0 ? 2*Math.PI-da : da;
	}
	private static double computeDesiredScale(double ux, double uy, double vx, double vy)
	{
		return Math.sqrt(vx*vx+vy*vy)/Math.sqrt(ux*ux+uy*uy);
	}
}
