package org.interreg.docexplore.stitcher;

import java.io.Serializable;
import java.util.List;

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
	public final static int [][][] regressions = {constant, biLinear, biQuadratic};//, biCubic};
	public final static int [][] orderFor(int n)
	{
		int order = 0;
		while ((order+1)*(order+1) < n && order < regressions.length)
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
	public final static double [][] neutral = {{0, 0}, {.5, 0}, {1, 0}, {1, .5}, {1, 1}, {.5, 1}, {0, 1}, {0, .5}};
	//public final static double [][] neutral = {};
	
	int [][] orders;
	double [][] coefs;
	
	public FragmentDistortion(Fragment f1, List<Association> associations)
	{
		this(f1, associations, orderFor(associations.size()));
	}
	public FragmentDistortion(Fragment f1, List<Association> associations, int [][] orders)
	{
		//System.out.println("regression order: "+orders.length);
		this.orders = orders;
		this.coefs = new double [2][orders.length];
		
		int n = associations.size()+neutral.length;
		Stitch [] stitches = new Stitch [n];
		for (int i=0;i<associations.size();i++)
		{
			Association a = associations.get(i);
			Fragment f2 = a.fa.other(f1);
			POI p1 = a.poiFor(f1);
			POI p2 = a.poiFor(f2);
			double lx2 = f2.fromImageToLocalX(p2.x), ly2 = f2.fromImageToLocalY(p2.y);
			double uix2 = f2.fromLocalX(lx2, ly2), uiy2 = f2.fromLocalY(lx2, ly2);
			double uix2i1 = f1.fromLocalToImageX(f1.toLocalX(uix2, uiy2)), uiy2i1 = f1.fromLocalToImageY(f1.toLocalY(uix2, uiy2));
			stitches[i] = new Stitch(p1.x, p1.y, uix2i1, uiy2i1);
		}
		double mx = .5*f1.imagew, my = .5*f1.imageh;
		double nray = 2;
		for (int i=0;i<neutral.length;i++)
		{
			double nx = neutral[i][0]*f1.imagew;
			double ny = neutral[i][1]*f1.imageh;
			double dx = mx+nray*(nx-mx);
			double dy = my+nray*(ny-my);
			stitches[associations.size()+i] = new Stitch(dx, dy, dx, dy);
		}
		
		Matrix A = new Matrix(n, orders.length), B = new Matrix(n, 1);
		for (int axis=0;axis<2;axis++)
		{
			Matrix C = solve(A, B, stitches, axis);
			set(C, axis);
		}
	}
	
	public double getDist(double x, double y, int axis)
	{
		double res = 0;
		for (int i=0;i<orders.length;i++)
			res += coefs[axis][i]*Math.pow(x, orders[i][0])*Math.pow(y, orders[i][1]);
		return res;
	}
	
	private void set(Matrix C, int axis)
	{
		for (int i=0;i<orders.length;i++)
			coefs[axis][i] = C.get(i, 0);
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
	
	public double distortion(FragmentAssociation fa, Fragment f, double lx, double ly)
	{
		return 1;
	}
	public double alpha(FragmentAssociation fa, Fragment f, double lx, double ly)
	{
		return 1;
	}
}
