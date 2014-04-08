/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.gfx;


import java.util.LinkedList;

public final class Math3D
{
	public static final double epsilon = .000001;
	public static final double sqrt2 = Math.sqrt(2);
	public static final double sqrt3 = Math.sqrt(3);
	
	public static final double [] origin = {0, 0, 0};
	public static final double [] basei = {1, 0, 0};
	public static final double [] basej = {0, 1, 0};
	public static final double [] basek = {0, 0, 1};
	
	//converts euclidian distance between two points on the surface of the sphere to an angle
	public static double arcLength(double d)
	{
		return Math.PI-2*Math.acos(d/2);
	}
	public static double lineLength(double a)
	{
		return Math.sqrt(2-2*Math.cos(a));
	}
	
	public static double [] crossProduct(double [] a, double [] b, double [] res)
	{
		res[0] = a[1]*b[2]-a[2]*b[1];
		res[1] = a[2]*b[0]-a[0]*b[2];
		res[2] = a[0]*b[1]-a[1]*b[0];
		return res;
	}
	public static double [] crossProduct(double [] a, double [] b) {
		return crossProduct(a, b, new double [3]);}
	public static float [] crossProduct(float [] a, float [] b, float [] res)
	{
		res[0] = a[1]*b[2]-a[2]*b[1];
		res[1] = a[2]*b[0]-a[0]*b[2];
		res[2] = a[0]*b[1]-a[1]*b[0];
		return res;
	}
	
	public static double dotProduct(double [] a, double [] b)
	{
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}
	
	public static double norm(double [] a)
	{
		return Math.sqrt(norm2(a));
	}
	public static float norm(float [] a)
	{
		return (float)Math.sqrt(norm2(a));
	}
	public static double norm2(double [] a)
	{
		return a[0]*a[0]+a[1]*a[1]+a[2]*a[2];
	}
	public static float norm2(float [] a)
	{
		return a[0]*a[0]+a[1]*a[1]+a[2]*a[2];
	}
	
	public static double distance(double [] a, double [] b)
	{
		double dx = a[0]-b[0];
		double dy = a[1]-b[1];
		double dz = a[2]-b[2];
		return Math.sqrt(dx*dx+dy*dy+dz*dz);
	}
	public static double distance2(double [] a, double [] b)
	{
		double dx = a[0]-b[0];
		double dy = a[1]-b[1];
		double dz = a[2]-b[2];
		return dx*dx+dy*dy+dz*dz;
	}
	
	public static double fastDist(double [] v)
	{
		double a = Math.abs(v[0]), b = Math.abs(v[1]), c = Math.abs(v[2]), tmp;
		if (a > b) {tmp = a; a = b; b = tmp;}
		if (b > c)
		{
			{tmp = b; b = c; c = tmp;}
			if (a > b) {tmp = a; a = b; b = tmp;}
		}
		return c+11*b/32+a/4;
	}
	
	public static double [] normalize(double [] a, double [] res)
	{
		double n = norm(a);
		res[0] = a[0]/n;
		res[1] = a[1]/n;
		res[2] = a[2]/n;
		return res;
	}
	public static double [] normalize(double [] a) {return normalize(a, new double [3]);}
	public static float [] normalize(float [] a, float [] res)
	{
		float n = norm(a);
		res[0] = a[0]/n;
		res[1] = a[1]/n;
		res[2] = a[2]/n;
		return res;
	}
	
	public static double [] safeNormal(double [] v)
	{
		double [] res = new double [3];
		
		if (v[0] != 0) {res[0] = -v[1]; res[1] = v[0]; res[2] = 0;}
		else if (v[1] != 0) {res[0] = 0; res[1] = -v[2]; res[2] = v[1];}
		else {res[0] = v[2]; res[1] = 0; res[2] = -v[0];}
		
		return res;
	}
	public static double [] safeNormal(double [] v, double [] res)
	{
		if (v[0] != 0) {res[0] = -v[1]; res[1] = v[0]; res[2] = 0;}
		else if (v[1] != 0) {res[0] = 0; res[1] = -v[2]; res[2] = v[1];}
		else {res[0] = v[2]; res[1] = 0; res[2] = -v[0];}
		
		return res;
	}
	
	public static double [] origin()
	{
		double [] o = {0, 0, 0};
		return o;
	}
	
	public static double [] scale(double [] v, double k, double [] res)
	{
		res[0] = k*v[0]; res[1] = k*v[1]; res[2] = k*v[2];
		return res;
	}
	public static double [] scale(double [] v, double k) {return scale(v, k, getVector3d());}
	
	public static double [][] scale(double [][] v, double k)
	{
		double [][] kv = new double [v.length][];
		for (int i=0;i<v.length;i++) kv[i] = scale(v[i], k);
		return kv;
	}
	
	public static double [][] createBase(double [] normal)
	{
		double [][] base = new double [2][];
		base[0] = normalize(safeNormal(normal));
		base[1] = normalize(Math3D.crossProduct(normal, base[0]));
		return base;
	}
	
	public static double [] add(double [] a, double [] b)
	{
		double [] v = {a[0]+b[0], a[1]+b[1], a[2]+b[2]};
		return v;
	}
	public static double [] add(double [] a, double [] b, double [] res)
	{
		res[0] = a[0]+b[0];
		res[1] = a[1]+b[1];
		res[2] = a[2]+b[2];
		return res;
	}
	
	public static double [] diff(double [] a, double [] b, double [] res)
	{
		res[0] = b[0]-a[0]; res[1] = b[1]-a[1]; res[2] = b[2]-a[2];
		return res;
	}
	public static double [] diff(double [] a, double [] b) {return diff(a, b, getVector3d());}
	public static float [] diff(float [] a, float [] b, float [] res)
	{
		res[0] = b[0]-a[0]; res[1] = b[1]-a[1]; res[2] = b[2]-a[2];
		return res;
	}
	
	//point of an origin centered ellipse along axis i,j with angle a
	public static double [] ellipsePoint(double [] i, double [] j, double a)
	{
		double ca = Math.cos(a);
		double sa = Math.sin(a);
		double [] res = {ca*i[0]+sa*j[0], ca*i[1]+sa*j[1], ca*i[2]+sa*j[2]};
		return res;
	}
	
	//expresses p in 2d origin centered base (i,j)
	public static double [] changeBase(double [] p, double [] i, double [] j)
	{
		double [] res = {0, 0};
		
		int k;
		for (k=0;k<3;k++) if (j[k]==0 && i[k]!=0)
		{
			res[0] = p[k]/i[k];
			break;
		}
		if (k == 3)
		{
			boolean ok = false;
			for (k=0;k<3 && !ok;k++) for (int l=k+1;l<3;l++)
			{
				if (j[k]!=0 && j[l]!=0)
				{
					double d = i[k]/j[k]-i[l]/j[l];
					if (d != 0)
					{
						res[0] = (p[k]/j[k]-p[l]/j[l])/d;
						ok = true;
						break;
					}
				}
			}
		}
		
		for (k=0;k<3;k++) if (j[k] != 0)
		{
			res[1] = (p[k]-res[0]*i[k])/j[k];
			break;
		}
		
		return res;
	}
	
	public static double [] invert(double [] v, double [] res)
	{
		res[0] = -v[0]; res[1] = -v[1]; res[2] = -v[2];
		return res;
	}
	public static double [] invert(double [] v) {return invert(v, new double [3]);}
	
	public static double [] transform3x3(double [] v, double [] m, double [] res)
	{
		res[0] = v[0]*m[0]+v[1]*m[3]+v[2]*m[6];
		res[1] = v[0]*m[1]+v[1]*m[4]+v[2]*m[7];
		res[2] = v[0]*m[2]+v[1]*m[5]+v[2]*m[8];
		return res;
	}
	
	/* Returns p expressed in (o, i, j, k)
	 */
	public static double [] toBase(double [] o, double [] i, double [] j, double [] k, double [] _p, double [] res)
	{
		double [] p = diff(o, _p, getVector3d()), buf = getVector3d();
		
		res[0] = -intersect2x1(crossProduct(j, k, buf), p, i);
		res[1] = -intersect2x1(crossProduct(k, i, buf), p, j);
		res[2] = -intersect2x1(crossProduct(i, j, buf), p, k);
		
		freeVector3d(p); freeVector3d(buf);
		return res;
	}
	
	/* Returns p expressed in canonical base
	 */
	public static double [] fromBase(double [] o, double [] i, double [] j, double [] k, double [] p, double [] res)
	{
		double [] buf = getVector3d();
		Math3D.set(res, o);
		
		Math3D.add(res, Math3D.scale(i, p[0], buf), res);
		Math3D.add(res, Math3D.scale(j, p[1], buf), res);
		Math3D.add(res, Math3D.scale(k, p[2], buf), res);
		
		freeVector3d(buf);
		return res;
	}
	
	/* Projects p on line (q, v)
	 */
	public static double project(double [] p, double [] q, double [] v)
	{
		return (dotProduct(p, v)-dotProduct(q, v))/dotProduct(v, v);
	}
	
	/* Intersection of a plane (n) and a line
	 */
	public static double intersect2x1(double [] n, double [] p, double [] v)
	{
		return -dotProduct(p, n)/dotProduct(v, n);
	}
	
	public static double [] set(double [] v1, double x, double y, double z) {v1[0] = x; v1[1] = y; v1[2] = z; return v1;}
	public static double [] set(double [] v1, double [] v2) {v1[0] = v2[0]; v1[1] = v2[1]; v1[2] = v2[2]; return v1;}
	public static float [] set(float [] v1, float x, float y, float z) {v1[0] = x; v1[1] = y; v1[2] = z; return v1;}
	
	static LinkedList<double []> vector3dPool = new LinkedList<double[]>();
	public static double [] getVector3d()
	{
		synchronized (vector3dPool)
		{
			if (vector3dPool.isEmpty())
				return new double [3];
			return vector3dPool.pop();
		}
	}
	public static void freeVector3d(double [] v) {synchronized (vector3dPool) {vector3dPool.add(v);}}
}
