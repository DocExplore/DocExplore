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
import java.util.LinkedList;
import java.util.List;

import Jama.Matrix;

public class FragmentDistortion implements Serializable
{
	private static final long serialVersionUID = 2759131475653577221L;
	
	static class Stitch implements Serializable
	{
		private static final long serialVersionUID = 4785019621018927043L;
		
		double x, y;
		double dx, dy;
		
		public Stitch(double fx, double fy, double tx, double ty)
		{
			this.x = fx;
			this.y = fy;
			this.dx = fx-tx;
			this.dy = fy-ty;
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
	public final static int neutralIterations = 0;
	public final static double [][] neutral = {{0, 0}, {.5, 0}, {1, 0}, {1, .5}, {1, 1}, {.5, 1}, {0, 1}, {0, .5}};
	//public final static double [][] neutral = {};
	
	int [][] orders;
	double [][] coefs;
	
	LinkedList<Stitch> stitches;
	public FragmentDistortion(FragmentSet fragments, Fragment f1, FragmentDistortion [] distortions)
	{
		List<FragmentAssociation> fas = fragments.associationsByFragment.get(f1);
		/*Stitch [] */stitches = new LinkedList<>();
		boolean [][] freePatches = new boolean [5][5];
		for (int i=0;i<freePatches.length;i++)
			for (int j=0;j<freePatches[0].length;j++)
				freePatches[i][j] = true;
		for (int i=0;i<fas.size();i++)
			if (fas.get(i).other(f1).index < f1.index)
		{
			FragmentAssociation fa = fas.get(i);
			Fragment f2 = fa.other(f1);
			for (int j=0;j<fa.associations.size();j++)
			{
				Association a = fa.associations.get(j);
				POI p1 = a.poiFor(f1), p2 = a.poiFor(f2);
				freePatches[(int)(freePatches.length*p1.x/f1.imagew)][(int)(freePatches[0].length*p1.y/f1.imageh)] = false;
				double p2x = p2.x, p2y = p2.y;
				if (distortions != null && distortions[f2.index] != null)
				{
					double dx = distortions[f2.index].getDist(p2x, p2y, 0), dy = distortions[f2.index].getDist(p2x, p2y, 1);
					p2x += dx;
					p2y += dy;
				}
				double lx2 = f2.fromImageToLocalX(p2x), ly2 = f2.fromImageToLocalY(p2y);
				double uix2 = f2.fromLocalX(lx2, ly2), uiy2 = f2.fromLocalY(lx2, ly2);
				double uix2i1 = f1.fromLocalToImageX(f1.toLocalX(uix2, uiy2)), uiy2i1 = f1.fromLocalToImageY(f1.toLocalY(uix2, uiy2));
				stitches.add(new Stitch(p1.x, p1.y, uix2i1, uiy2i1));
			}
		}
		double mx = .5*f1.imagew, my = .5*f1.imageh;
		double nray = 3;
		for (int j=0;j<neutralIterations;j++)
			for (int i=0;i<neutral.length;i++)
			{
				double nx = neutral[i][0]*f1.imagew;
				double ny = neutral[i][1]*f1.imageh;
				double dx = mx+(j+1)*nray*(nx-mx);
				double dy = my+(j+1)*nray*(ny-my);
				stitches.add(new Stitch(dx, dy, dx, dy));
			}
		for (int i=0;i<freePatches.length;i++)
			for (int j=0;j<freePatches[0].length;j++)
				if (freePatches[i][j])
				{
					double px = (i+.5)/freePatches.length*f1.imagew;
					double py = (j+.5)/freePatches[0].length*f1.imageh;
					stitches.add(new Stitch(px, py, px, py));
				}
		
		this.orders = orderFor(stitches.size());
		this.coefs = new double [2][orders.length];
		
		Matrix A = new Matrix(stitches.size(), orders.length), B = new Matrix(stitches.size(), 1);
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
	
	private Matrix solve(Matrix A, Matrix B, List<Stitch> stitches, int axis)
	{
		int i = 0;
		for (Stitch stitch : stitches)
		{
			double x = stitch.x, y = stitch.y;
			for (int j=0;j<orders.length;j++)
				A.set(i, j, Math.pow(x, orders[j][0])*Math.pow(y, orders[j][1]));
			B.set(i, 0, axis == 0 ? stitch.dx : stitch.dy);
			i++;
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
