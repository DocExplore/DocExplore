package org.interreg.docexplore.stitcher;

import java.io.Serializable;

import Jama.Matrix;

public class FragmentDistortion implements Serializable
{
	private static final long serialVersionUID = 2759131475653577221L;
	
	static class Stitch
	{
		double x, y;
		double dx, dy;
		
		public Stitch(double fx, double fy, double tx, double ty)
		{
			this.x = fx;
			this.y = fy;
			this.dx = .5*(tx-fx);
			this.dy = .5*(ty-fy);
		}
	}
	
	public final static int [][] constant = {{0, 0}};
	public final static int [][] biLinear = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
	public final static int [][] biQuadratic = {{0, 0}, {1, 0}, {0, 1}, {1, 1}, {2, 0}, {0, 2}, {2, 1}, {1, 2}, {2, 2}};
	public final static int [][] biCubic = {{0, 0}, {1, 0}, {0, 1}, {1, 1}, {2, 0}, {0, 2}, {2, 1}, {1, 2}, {2, 2}, {3, 0}, {0, 3}, {3, 1}, {1, 3}, {3, 2}, {2, 3}, {3, 3}};
	public final static int [][][] regressions = {constant, biLinear, biQuadratic, biCubic};
	public final static int [][] orderFor(int n)
	{
		int order = 0;
		while ((order+1)*(order+1) < n && order < 3)
			order++;
		if (order < 1)
			return null;
		int [][] orders = new int [order*order][2];
		for (int i=0;i<order;i++)
			for (int j=0;j<order;j++)
			{
				orders[i*order+j][0] = i;
				orders[i*order+j][1] = j;
			}
		return orders;
	}
	
	int [][] orders;
	double [][] coefs;
	
	public FragmentDistortion(FragmentAssociation fa)
	{
		this(fa, orderFor(fa.associations.size()));
	}
	public FragmentDistortion(FragmentAssociation fa, int [][] orders)
	{
		//System.out.println("regression order: "+orders.length);
		this.orders = orders;
		this.coefs = new double [4][orders.length];
		
		Fragment f1 = fa.d1.fragment, f2 = fa.d2.fragment;
		int nNeutralPoints = 3;
		int n = fa.associations.size()+2*nNeutralPoints;
		
		Stitch [][] stitches = new Stitch [2][n];
		for (int i=0;i<fa.associations.size();i++)
		{
			Association a = fa.associations.get(i);
			double lx1 = f1.fromImageToLocalX(a.p1.x), ly1 = f1.fromImageToLocalY(a.p1.y);
			double lx2 = f2.fromImageToLocalX(a.p2.x), ly2 = f2.fromImageToLocalY(a.p2.y);
			double uix1 = f1.fromLocalX(lx1, ly1), uiy1 = f1.fromLocalY(lx1, ly1);
			double uix2 = f2.fromLocalX(lx2, ly2), uiy2 = f2.fromLocalY(lx2, ly2);
			double uix2i1 = f1.fromLocalToImageX(f1.toLocalX(uix2, uiy2)), uiy2i1 = f1.fromLocalToImageY(f1.toLocalY(uix2, uiy2));
			double uix1i2 = f2.fromLocalToImageX(f2.toLocalX(uix1, uiy1)), uiy1i2 = f2.fromLocalToImageY(f2.toLocalY(uix1, uiy1));
			stitches[0][i] = new Stitch(a.p1.x, a.p1.y, uix2i1, uiy2i1);
			stitches[1][i] = new Stitch(a.p2.x, a.p2.y, uix1i2, uiy1i2);
		}
		for (int i=0;i<nNeutralPoints;i++)
			for (int j=0;j<2;j++)
		{
			int i0 = n-2*nNeutralPoints+2*i;
			double k = i*1./(nNeutralPoints-1);
			closeNeutralPoint(fa, j == 0 ? f1 : f2, k, stitches[j][i0] = new Stitch(0, 0, 0, 0));
			farNeutralPoint(fa, j == 0 ? f1 : f2, k, stitches[j][i0+1] = new Stitch(0, 0, 0, 0));
		}
		
		Matrix A = new Matrix(n, orders.length), B = new Matrix(n, 1);
		for (int axis=0;axis<2;axis++)
			for (int inv=0;inv<2;inv++)
		{
			Matrix C = solve(A, B, stitches[inv], axis);
			set(C, axis, inv == 1);
		}
	}
	
	public double getDist(double x, double y, int axis, boolean inverse)
	{
		int coefIndex = (inverse ? 2 : 0)+axis;
		double res = 0;
		for (int i=0;i<orders.length;i++)
			res += coefs[coefIndex][i]*Math.pow(x, orders[i][0])*Math.pow(y, orders[i][1]);
		return res;
	}
	
	private void set(Matrix C, int axis, boolean inverse)
	{
		int coefIndex = (inverse ? 2 : 0)+axis;
		for (int i=0;i<orders.length;i++)
			coefs[coefIndex][i] = C.get(i, 0);
	}
	
	private Matrix solve(Matrix A, Matrix B, Stitch [] stitches, int axis)
	{
		for (int i=0;i<stitches.length;i++)
		{
			double x = stitches[i].x, y = stitches[i].y;
			for (int j=0;j<orders.length;j++)
				A.set(i, j, Math.pow(x, orders[j][0])*Math.pow(y, orders[j][1]));
			B.set(i, 0, axis == 0 ? stitches[i].dx : stitches[i].dy);
		}
		return A.solve(B);
	}
	
	public void closeNeutralPoint(FragmentAssociation fa, Fragment f, double l, Stitch res)
	{
		FragmentDescription d = fa.d1.fragment == f ? fa.d1 : fa.d2;
		boolean hor = d.rect.getWidth() < d.rect.getHeight();
		boolean lo = hor ? d.rect.getX()+.5*d.rect.getWidth() < .5 : d.rect.getY()+.5*d.rect.getHeight() < .5;
		double k1 = hor ? d.rect.getX() : d.rect.getY(), k2 = hor ? d.rect.getX()+d.rect.getWidth() : d.rect.getY()+d.rect.getHeight();
		if (lo) {double tmp = k1; k1 = 1-k2; k2 = 1-tmp;}
		if (hor) {res.x = f.fromLocalToImageX(k1+(k1-k2)); res.y = f.fromLocalToImageY(l);}
		else {res.x = f.fromLocalToImageX(l); res.y = f.fromLocalToImageY(k1+(k1-k2));}
	}
	public void farNeutralPoint(FragmentAssociation fa, Fragment f, double l, Stitch res)
	{
		FragmentDescription d = fa.d1.fragment == f ? fa.d1 : fa.d2;
		boolean hor = d.rect.getWidth() < d.rect.getHeight();
		boolean lo = hor ? d.rect.getX()+.5*d.rect.getWidth() < .5 : d.rect.getY()+.5*d.rect.getHeight() < .5;
		double k1 = hor ? d.rect.getX() : d.rect.getY(), k2 = hor ? d.rect.getX()+d.rect.getWidth() : d.rect.getY()+d.rect.getHeight();
		if (lo) {double tmp = k1; k1 = 1-k2; k2 = 1-tmp;}
		if (hor) {res.x = f.fromLocalToImageX(k2+(k2-k1)); res.y = f.fromLocalToImageY(l);}
		else {res.x = f.fromLocalToImageX(l); res.y = f.fromLocalToImageY(k2+(k2-k1));}
	}
	
	public double distortion(FragmentAssociation fa, Fragment f, double lx, double ly)
	{
		FragmentDescription d = fa.d1.fragment == f ? fa.d1 : fa.d2;
		boolean hor = d.rect.getWidth() < d.rect.getHeight();
		boolean lo = hor ? d.rect.getX()+.5*d.rect.getWidth() < .5 : d.rect.getY()+.5*d.rect.getHeight() < .5;
		double k = hor ? lx : ly, k1 = hor ? d.rect.getX() : d.rect.getY(), k2 = hor ? d.rect.getX()+d.rect.getWidth() : d.rect.getY()+d.rect.getHeight();
		if (lo) {k = 1-k; double tmp = k1; k1 = 1-k2; k2 = 1-tmp;}
		double k0 = k1+.5*(k1-k2);
		if (k > k2) return 0;
		if (k < k0) return -1;
		if (k > k1) return 1;
		return (k-k0)/(k1-k0);
	}
	public double alpha(FragmentAssociation fa, Fragment f, double lx, double ly)
	{
		FragmentDescription d = fa.d1.fragment == f ? fa.d1 : fa.d2;
		boolean hor = d.rect.getWidth() < d.rect.getHeight();
		boolean lo = hor ? d.rect.getX()+.5*d.rect.getWidth() < .5 : d.rect.getY()+.5*d.rect.getHeight() < .5;
		double k = hor ? lx : ly, k1 = hor ? d.rect.getX() : d.rect.getY(), k2 = hor ? d.rect.getX()+d.rect.getWidth() : d.rect.getY()+d.rect.getHeight();
		if (lo) {k = 1-k; double tmp = k1; k1 = 1-k2; k2 = 1-tmp;}
		if (k > k2) return 0;
		if (k < k1) return 1;
		return 1-(k-k1)/(k2-k1);
	}
}
