/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.util;


public final class Math2D
{
	public static double epsilon = .000001;
	
	public static final double [] origin = {0, 0};
	public static final double [] basei = {1, 0};
	public static final double [] basej = {0, 1};
	
	public static double [] set(double [] v, double x, double y) {v[0] = x; v[1] = y; return v;}
	public static double [] set(double x, double y) {return set(new double [2], x, y);}
	public static double [] set(double [] res, double [] v) {res[0] = v[0]; res[1] = v[1]; return res;}
	public static double [] set(double [] v) {return set(new double [2], v);}
	
	public static double [] scale(double [] v, double k, double [] res)
	{
		res[0] = k*v[0];
		res[1] = k*v[1];
		return res;
	}
	public static double [] scale(double [] v, double k) {return scale(v, k, new double [2]);}
	
	public static double [] add(double [] v1, double [] v2, double [] res)
	{
		res[0] = v1[0]+v2[0];
		res[1] = v1[1]+v2[1];
		return res;
	}
	public static double [] add(double [] v1, double [] v2) {return add(v1, v2, new double [2]);}
	
	public static double [] diff(double [] v1, double [] v2, double [] res)
	{
		res[0] = v2[0]-v1[0];
		res[1] = v2[1]-v1[1];
		return res;
	}
	public static double [] diff(double [] v1, double [] v2) {return diff(v1, v2, new double [2]);}
	
	public static double [] opposite(double [] v, double [] res)
	{
		res[0] = -v[0];
		res[1] = -v[1];
		return res;
	}
	public static double [] opposite(double [] v) {return opposite(v, new double [2]);}
	
	public static double dotProduct(double [] v1, double [] v2)
	{
		return v1[0]*v2[0]+v1[1]*v2[1];
	}
	
	//left : <0
	public static double crossProduct(double [] v1, double [] v2)
	{
		return v1[1]*v2[0]-v1[0]*v2[1];
	}
	
	public static double [] normalC(double [] v, double [] res)
	{
		res[0] = v[1];
		res[1] = -v[0];
		return res;
	}
	public static double [] normalC(double [] v) {return normalC(v, new double [2]);}
	
	public static double [] normalCC(double [] v, double [] res)
	{
		res[0] = -v[1];
		res[1] = v[0];
		return res;
	}
	public static double [] normalCC(double [] v) {return normalCC(v, new double [2]);}
	
	public static boolean colinear(double [] v1, double [] v2, double epsilon)
	{
		double dp = dotProduct(v1, normalC(v2));
		return dp*dp < epsilon*epsilon;
	}
	public static boolean colinear(double [] v1, double [] v2) {return colinear(v1, v2, epsilon);}
	
	public static double projection(double [] u, double [] v)
	{
		return dotProduct(u, v)/dotProduct(u, u);
	}
	
	public static boolean aligned(double [] p1, double [] v1, 
		double [] p2, double [] v2, double epsilon)
	{
		return Math2D.colinear(v1, v2, epsilon) && Math2D.colinear(v1, Math2D.diff(p1, p2), epsilon);
	}
	public static boolean aligned(double [] p1, double [] v1, 
		double [] p2, double [] v2)
	{
		return aligned(p1, v1, p2, v2, epsilon);
	}
	
	public static double intersection(double [] p1, double [] v1, 
		double [] p2, double [] v2)
	{
		double [] v2n = normalC(v2);
		return (dotProduct(p2, v2n)-dotProduct(p1, v2n))/dotProduct(v1, v2n);
	}
	
	public static double length(double [] v)
	{
		return Math.sqrt(length2(v));
	}
	public static double length2(double [] v)
	{
		return v[0]*v[0]+v[1]*v[1];
	}
	
	public static double distance(double [] a, double [] b)
	{
		return Math.sqrt(distance2(a, b));
	}
	public static double distance2(double [] a, double [] b)
	{
		return (a[0]-b[0])*(a[0]-b[0])+(a[1]-b[1])*(a[1]-b[1]);
	}
	
	public static double [] normalize(double [] v, double [] res)
	{
		return scale(v, 1/length(v), res);
	}
	public static double [] normalize(double [] v) {return normalize(v, new double [2]);}
	
	public static double [] reverse(double [] v, double [] res)
	{
		res[0] = -v[0];
		res[1] = -v[1];
		return res;
	}
	public static double [] reverse(double [] v) {return reverse(v, new double [2]);}
}
