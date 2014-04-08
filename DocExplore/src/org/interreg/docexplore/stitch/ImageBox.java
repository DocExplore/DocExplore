/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitch;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Math2D;


public class ImageBox
{
	int w, h;
	double [] p, u, v;
	double [] spine;
	
	public ImageBox(int w, int h, double [] spine)
	{
		this.w = w;
		this.h = h;
		p = new double [] {0, 0};
		u = new double [] {1, 0};
		v = new double [] {0, 1};
		this.spine = spine;
	}
	
	public void rotate(double a)
	{
		double ca = Math.cos(a), sa = Math.sin(a);
		double nux = u[0]*ca-u[1]*sa, nuy = u[0]*sa+u[1]*ca;
		double nvx = v[0]*ca-v[1]*sa, nvy = v[0]*sa+v[1]*ca;
		u[0] = nux; u[1] = nuy;
		v[0] = nvx; v[1] = nvy;
	}
	
	double [] buf1 = {0, 0}, buf2 = {0, 0}, buf3 = {0, 0};
	public double [] toWorld(double x, double y, double [] res)
	{
		Math2D.add(p, Math2D.scale(u, x, buf1), res);
		return Math2D.add(res, Math2D.scale(v, y, buf1), res);
	}
	public double [] toWorld(double [] q, double [] res) {return toWorld(q[0], q[1], res);}
	
	public double [] fromWorld(double x, double y, double [] res)
	{
		res[0] = ((x-p[0])*u[0]+(y-p[1])*u[1])/Math2D.dotProduct(u, u);
		res[1] = ((x-p[0])*v[0]+(y-p[1])*v[1])/Math2D.dotProduct(v, v);
		return res;
	}
	public double [] fromWorld(double [] q, double [] res) {return fromWorld(q[0], q[1], res);}
	
	public void align(double [] p1, double [] p2, ImageBox to, double [] to1, double [] to2)
	{
		double [] pw1 = toWorld(p1, new double [2]);
		double [] pw2 = toWorld(p2, new double [2]);
		double [] tow1 = to.toWorld(to1, new double [2]);
		double [] tow2 = to.toWorld(to2, new double [2]);
		double [] dp = Math2D.diff(pw1, pw2, buf2);
		double [] dto = Math2D.diff(tow1, tow2, buf3);
		double ldp = Math2D.length(dp), ldto = Math2D.length(dto);
		double a = Math.acos(Math2D.dotProduct(dp, dto)/(ldp*ldto));
		if (dto[0]*dp[1]-dto[1]*dp[0] > 0)
			a = -a;
		double ca = Math.cos(a), sa = Math.sin(a);
		
		double npx = -pw1[0]*ca+pw1[1]*sa, npy = -pw1[0]*sa-pw1[1]*ca;
		npx *= ldto/ldp; npy *= ldto/ldp;
		npx += tow1[0]; npy += tow1[1];
		
		double nux = u[0]*ca-u[1]*sa, nuy = u[0]*sa+u[1]*ca;
		nux *= ldto/ldp; nuy *= ldto/ldp;
		
		double nvx = v[0]*ca-v[1]*sa, nvy = v[0]*sa+v[1]*ca;
		nvx *= ldto/ldp; nvy *= ldto/ldp;
		//System.out.println(npx+","+npy);
		
		p[0] = npx; p[1] = npy;
		u[0] = nux; u[1] = nuy;
		v[0] = nvx; v[1] = nvy;
		
//		System.out.println("tow: "+tow1[0]+","+tow1[1]+" "+tow2[0]+","+tow2[1]);
//		double [] res1 = toWorld(p1, new double [2]);
//		double [] res2 = toWorld(p2, new double [2]);
//		System.out.println("res: "+res1[0]+","+res1[1]+" "+res2[0]+","+res2[1]);
	}
	public double [] bounds()
	{
		double minx = p[0], maxx = p[0], miny = p[1], maxy = p[1];
		
		if (p[0]+w*u[0] < minx) minx = p[0]+w*u[0];
		if (p[0]+w*u[0] > maxx) maxx = p[0]+w*u[0];
		if (p[1]+w*u[1] < miny) miny = p[1]+w*u[1];
		if (p[1]+w*u[1] > maxy) maxy = p[1]+w*u[1];
		
		if (p[0]+w*u[0]+h*v[0] < minx) minx = p[0]+w*u[0]+h*v[0];
		if (p[0]+w*u[0]+h*v[0] > maxx) maxx = p[0]+w*u[0]+h*v[0];
		if (p[1]+w*u[1]+h*v[1] < miny) miny = p[1]+w*u[1]+h*v[1];
		if (p[1]+w*u[1]+h*v[1] > maxy) maxy = p[1]+w*u[1]+h*v[1];
		
		if (p[0]+h*v[0] < minx) minx = p[0]+h*v[0];
		if (p[0]+h*v[0] > maxx) maxx = p[0]+h*v[0];
		if (p[1]+h*v[1] < miny) miny = p[1]+h*v[1];
		if (p[1]+h*v[1] > maxy) maxy = p[1]+h*v[1];
		
		return new double [] {minx, miny, maxx, maxy};
	}
	
	float [] getCol(BufferedImage image, int i, int j, float [] col)
	{
		if (i < 0 || j < 0 || i >= image.getWidth() || j >= image.getHeight())
			col[0] = col[1] = col[2] = 0;
		else
		{
			int rgb = image.getRGB(i, j);
			col[0] = ImageUtils.red(rgb)/255f;
			col[1] = ImageUtils.green(rgb)/255f;
			col[2] = ImageUtils.blue(rgb)/255f;
		}
		return col;
	}
	float [] blc = {0, 0, 0}, brc = {0, 0, 0}, tlc = {0, 0, 0}, trc = {0, 0, 0};
	float [] getSample(double x, double y, BufferedImage image, float [] res)
	{
		//x *= image.getWidth(); y *= image.getHeight();
		x -= .5; y -= .5;
		int i0 = (int)x, j0 = (int)y;
		x -= i0; y -= j0;
		//if (x<0 || y<0 || x>1 || y>1)
		//	System.out.println(x+","+y);
		double bl = (1-x)*(1-y), br = x*(1-y), tl = (1-x)*y, tr = x*y;
		getCol(image, i0, j0, blc);
		getCol(image, i0+1, j0, brc);
		getCol(image, i0, j0+1, tlc);
		getCol(image, i0+1, j0+1, trc);
		for (int i=0;i<3;i++)
			res[i] = (float)((bl*blc[i]+br*brc[i]+tl*tlc[i]+tr*trc[i])/(bl+br+tl+tr));
		return res;
	}
	
	Line2D line(double [] p1, double [] p2, double x0, double y0, double zoom)
	{
		return new Line2D.Double(p1[0]*zoom-x0, p1[1]*zoom-y0, p2[0]*zoom-x0, p2[1]*zoom-y0);
	}
	public void render(Graphics g, double x0, double y0, double zoom)
	{
		g.setColor(Color.red);
		((Graphics2D)g).draw(line(toWorld(0, 0, buf2), toWorld(w, 0, buf3), x0, y0, zoom));
		((Graphics2D)g).draw(line(toWorld(w, 0, buf2), toWorld(w, h, buf3), x0, y0, zoom));
		((Graphics2D)g).draw(line(toWorld(w, h, buf2), toWorld(0, h, buf3), x0, y0, zoom));
		((Graphics2D)g).draw(line(toWorld(0, h, buf2), toWorld(0, 0, buf3), x0, y0, zoom));
	}
	
//	public static void main(String [] args)
//	{
//		ImageBox b1 = new ImageBox(100, 200);
//		b1.p[0] = 50; b1.p[1] = 30;
//		ImageBox b2 = new ImageBox(10, 20);
//		b2.align(new double [] {.5, 0}, new double [] {1, 0}, b1, new double [] {0, 1}, new double [] {1, 1.1});
//	}
}
