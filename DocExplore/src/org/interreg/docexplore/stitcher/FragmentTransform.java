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
		
		if (fa.associations.isEmpty())
		{
			double xi = fa.d1.fragment.toLocalX(fa.d2.fragment.uix, fa.d2.fragment.uiy);
			double yi = fa.d1.fragment.toLocalY(fa.d2.fragment.uix, fa.d2.fragment.uiy);
			return new FragmentTransform(xi, yi, fa.d2.fragment.uiw/fa.d1.fragment.uiw, fa.d2.fragment.uiang-fa.d1.fragment.uiang);
		}
		
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
	
	public double overlap(Fragment f, Fragment res)
	{
		double uixt = f.fromLocalX(dx, dy);
		double uiyt = f.fromLocalY(dx, dy);
		double uiwt = res.imagew*f.uiw*dw/f.imagew;
		double uiangt = f.uiang+dang;
		double uxt = uiwt*Math.cos(uiangt);
		double uyt = uiwt*Math.sin(uiangt);
		double vxt = -res.imageh*uyt/res.imagew;
		double vyt = res.imageh*uxt/res.imagew;
		return computeOverlap(f.uix, f.uiy, f.ux, f.uy, f.vx, f.vy, uixt, uiyt, uxt, uyt, vxt, vyt);
	}
	
	static double [][] output = new double [12][2];
	static double [][] input = new double [12][2];
	private static void copy(double [][] from, double [][] to, int n)
	{
		for (int i=0;i<n;i++)
			for (int j=0;j<from[i].length;j++)
				to[i][j] = from[i][j];
	}
	private static boolean inside(double [] p, double [] e)
	{
		double ux = p[0]-e[0], uy = p[1]-e[1];
		return ux*e[3]-uy*e[2] < 0;
	}
	private static double triangleArea(double ux, double uy, double vx, double vy)
	{
		double k = (ux*vx+uy*vy)/(vx*vx+vy*vy);
		double ix = k*vx, iy = k*vy;
		return .5*Math.sqrt((ux-ix)*(ux-ix)+(uy-iy)*(uy-iy))*Math.sqrt(vx*vx+vy*vy);
	}
	private static double computeOverlap(double x1, double y1, double ux1, double uy1, double vx1, double vy1,
		double x2, double y2, double ux2, double uy2, double vx2, double vy2)
	{
		double [][] clip = {{x1, y1, ux1, uy1}, {x1+ux1, y1+uy1, vx1, vy1}, {x1+ux1+vx1, y1+uy1+vy1, -ux1, -uy1}, {x1+vx1, y1+vy1, -vx1, -vy1}};
		double [][] subject = {{x2, y2}, {x2+ux2, y2+uy2}, {x2+ux2+vx2, y2+uy2+vy2}, {x2+vx2, y2+vy2}};
		double [][][] segs = {
			{{x1, y1, ux1, uy1}, {x1+ux1, y1+uy1, vx1, vy1}, {x1+ux1+vx1, y1+uy1+vy1, -ux1, -uy1}, {x1+vx1, y1+vy1, -vx1, -vy1}},
			{{x2, y2, ux2, uy2}, {x2+ux2, y2+uy2, vx2, vy2}, {x2+ux2+vx2, y2+uy2+vy2, -ux2, -uy2}, {x2+vx2, y2+vy2, -vx2, -vy2}}};
		copy(subject, output, 4);
		int nOutput = 4;
		for (int c=0;c<4;c++)
		{
			int nInput = nOutput;
			copy(output, input, nOutput);
			nOutput = 0;
			int s = nInput-1;
			for (int e=0;e<nInput;e++)
			{
				if (inside(input[e], clip[c]))
				{
					if (!inside(input[s], clip[c]))
					{
						double ux = input[e][0]-input[s][0], uy = input[e][1]-input[s][1];
						double k = intersect(input[s][0], input[s][1], ux, uy, clip[c][0], clip[c][1], clip[c][2], clip[c][3]);
						output[nOutput][0] = input[s][0]+k*ux;
						output[nOutput][1] = input[s][1]+k*uy;
						nOutput++;
					}
					output[nOutput][0] = input[e][0];
					output[nOutput][1] = input[e][1];
					nOutput++;
				}
				else if (inside(input[s], clip[c]))
				{
					double ux = input[e][0]-input[s][0], uy = input[e][1]-input[s][1];
					double k = intersect(input[s][0], input[s][1], ux, uy, clip[c][0], clip[c][1], clip[c][2], clip[c][3]);
					output[nOutput][0] = input[s][0]+k*ux;
					output[nOutput][1] = input[s][1]+k*uy;
					nOutput++;
				}
				s = e;
			}
		}
		
		double xm = 0, ym = 0;
		for (int i=0;i<nOutput;i++)
		{
			xm += output[i][0];
			ym += output[i][1];
		}
		xm /= nOutput;
		ym /= nOutput;
		
		double area = 0;
		for (int i=0;i<nOutput;i++)
			area += triangleArea(output[i][0]-xm, output[i][1]-ym, output[(i+1)%nOutput][0]-xm, output[(i+1)%nOutput][1]-ym);
		return area/(Math.sqrt(ux1*ux1+uy1*uy1)*Math.sqrt(vx1*vx1+vy1*vy1)+Math.sqrt(ux2*ux2+uy2*uy2)*Math.sqrt(vx2*vx2+vy2*vy2));
	}
	private static double intersect(double [] s1, double [] s2)
	{
		return intersect(s1[0], s1[1], s1[2], s1[3], s2[0], s2[1], s2[2], s2[3]);
	}
	private static double intersect(double x1, double y1, double vx1, double vy1, double x2, double y2, double vx2, double vy2)
	{
		return ((x2-x1)*vy2-(y2-y1)*vx2)/(vx1*vy2-vy1*vx2);
	}
	
	public static void main(String [] args)
	{
		//System.out.println(computeOverlap(0, 0, 4, 2, -3, 6, 1, 1, 3, 3, -3, 3));
		System.out.println(computeOverlap(0, 0, 4, 0, 0, 4, 1, 1, 2.9, 0, 0, 2.9));
	}
}
