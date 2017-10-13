package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class Tightener
{
	public static void tighten(final FragmentAssociation fa, float [] progress)
	{
		tighten(fa, progress, 0, 1);
	}
	@SuppressWarnings("serial")
	public static void tighten(final FragmentAssociation fa, float [] progress, float ps, float pe)
	{
		progress[0] = ps;
		
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
		progress[0] = ps+.2f*(pe-ps);
		
		double cx = 0, cy = 0, dcx = 0, dcy = 0;
		for (int i=0;i<n;i++)
		{
			cx += features.get(i).x;
			cy += features.get(i).y;
			dcx += pos[2*i];
			dcy += pos[2*i+1];
		}
		cx /= n; cy /= n; dcx /= n; dcy /= n;
		progress[0] = ps+.4f*(pe-ps);
		
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
		progress[0] = ps+.6f*(pe-ps);
		
		double scale = 0;
		for (int i=0;i<n;i++)
			scale += computeDesiredScale(features.get(i).x-cx, features.get(i).y-cy, pos[2*i]-dcx, pos[2*i+1]-dcy);
		scale /= n;
		progress[0] = ps+.8f*(pe-ps);
		
		double ca = Math.cos(rot), sa = Math.sin(rot);
		for (int i=0;i<n;i++)
		{
			transform(features.get(i).x-cx, features.get(i).y-cy, ca, sa, scale, pos, 2*i);
			pos[2*i] += dcx;
			pos[2*i+1] += dcy;
		}
		progress[0] = pe;
		
//		double err = 0;
//		for (int i=0;i<n;i++)
//			if (pos[2*i] >= 0)
//		{
//			POI poi = fa.d2.features.get(i);
//			List<Association> list = fa.associationsByPOI.get(poi);
//			for (int j=0;j<list.size();j++)
//			{
//				POI poi2 = list.get(j).other(poi);
//				err += (pos[2*i]-poi2.x)*(pos[2*i]-poi2.x)+(pos[2*i+1]-poi2.y)*(pos[2*i+1]-poi2.y);
//			}
//		}
//		err /= n-unmapped;
//		System.out.println("err>"+err);
		
//		JDialog dbg = new JDialog((Frame)null, "Tighten", true);
//		dbg.getContentPane().add(new JPanel()
//		{
//			Color lineCol = new Color(0, 0, .5f, .3f);
//			{setPreferredSize(new Dimension(1024, 1024));}
//			@Override protected void paintComponent(Graphics g)
//			{
//				super.paintComponent(g);
//				for (int i=0;i<features.size();i++)
//				{
//					g.setColor(Color.red);
//					g.drawRect((int)pos[2*i]/2-1, (int)pos[2*i+1]/2-1, 2, 2);
//					
//					POI poi = features.get(i);
//					List<Association> list = fa.associationsByPOI.get(poi);
//					for (int j=0;j<list.size();j++)
//					{
//						g.setColor(Color.blue);
//						g.drawRect((int)list.get(j).other(poi).x/2-1, (int)list.get(j).other(poi).y/2-1, 2, 2);
//						g.setColor(lineCol);
//						g.drawLine((int)pos[2*i]/2, (int)pos[2*i+1]/2, (int)list.get(j).other(poi).x/2, (int)list.get(j).other(poi).y/2);
//					}
//				}
//			}
//		});
//		dbg.pack();
//		dbg.setVisible(true);
		
		transform(-cx, -cy, ca, sa, scale, pos, 0);
		pos[0] += dcx;
		pos[1] += dcy;
		double xi = fa.d1.fragment.fromImageToLocalX(pos[0]);
		double yi = fa.d1.fragment.fromImageToLocalY(pos[1]);
		fa.d2.fragment.uix = fa.d1.fragment.fromLocalX(xi, yi);
		fa.d2.fragment.uiy = fa.d1.fragment.fromLocalY(xi, yi);
		fa.d2.fragment.uiw = fa.d2.fragment.imagew*fa.d1.fragment.uiw*scale/fa.d1.fragment.imagew;
		fa.d2.fragment.uiang = fa.d1.fragment.uiang+rot;
		fa.d2.fragment.update();
		
		progress[0] = pe;
	}
	
	public static void transform(double x, double y, double ca, double sa, double scale, double [] res, int index)
	{
		x *= scale;
		y *= scale;
		res[index] = x*ca-y*sa;
		res[index+1] = x*sa+y*ca;
	}
	
	//rotation for u to align with v
	public static double computeDesiredRot(double ux, double uy, double vx, double vy)
	{
		double da = Math.acos((ux*vx+uy*vy)/(Math.sqrt(ux*ux+uy*uy)*Math.sqrt(vx*vx+vy*vy)));
		return ux*vy-uy*vx < 0 ? 2*Math.PI-da : da;
	}
	public static double computeDesiredScale(double ux, double uy, double vx, double vy)
	{
		return Math.sqrt(vx*vx+vy*vy)/Math.sqrt(ux*ux+uy*uy);
	}
	
	public static void main(String [] args)
	{
//		Fragment f1 = new Fragment(), f2 = new Fragment();
//		FragmentAssociation fa = new FragmentAssociation(f1, f2);
//		fa.d1.features.add(new POI(fa.d1, new double [] {25, 12, 0}, 0));
//		fa.d1.features.add(new POI(fa.d1, new double [] {75, 75, 10}, 1));
//		fa.d1.features.add(new POI(fa.d1, new double [] {65, 20, 20}, 2));
//		fa.d1.resetBins();
//		fa.d2.features.add(new POI(fa.d2, new double [] {12, 62, 10}, 0));
//		fa.d2.features.add(new POI(fa.d2, new double [] {87, 25, 0}, 1));
//		fa.d2.features.add(new POI(fa.d2, new double [] {50, 90, 20}, 2));
//		fa.d2.resetBins();
//		fa.associations.add(new Association(fa, fa.d1.features.get(0), fa.d2.features.get(1), 1, 0));
//		fa.associations.add(new Association(fa, fa.d1.features.get(1), fa.d2.features.get(0), 1, 1));
//		fa.associations.add(new Association(fa, fa.d1.features.get(2), fa.d2.features.get(2), 1, 2));
//		fa.resetAssociationsByPOI();
//		tighten(fa, new float [] {0});
//		System.exit(0);
	}
}
