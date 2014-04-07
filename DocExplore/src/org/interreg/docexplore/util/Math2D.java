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
