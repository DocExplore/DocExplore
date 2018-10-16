/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher.springy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.imgscalr.Scalr;
import org.interreg.docexplore.util.ImageUtils;

public class ImageGrid
{
	static double neighborCor = .07;
	static double similarityCor = .04;
	static int searchRay = 5;
	
	class Node
	{
		int gi, gj;
		double x, y;
		int ix, iy;
		int r, g, b;
		Color col;
		
		Node [] neighbors = null;
		double [] neighborDist = null;
		
		boolean active = true;
		double vx, vy;
		double complacency = 0;
		
		double pvx, pvy;
		
		Node(BufferedImage image, int i, int j)
		{
			this.gi = i;
			this.gj = j;
			this.x = i;
			this.y = j;
			this.ix = i;
			this.iy = j;
			int rgb = image.getRGB(ix, iy);
			this.r = ImageUtils.red(rgb);
			this.g = ImageUtils.green(rgb);
			this.b = ImageUtils.blue(rgb);
			this.col = new Color(2*r/3, 2*g/3, 2*b/3);
		}
		void buildNeighbors()
		{
			int n = 0;
			for (int di=-1;di<2;di++)
				if (gi+di >= 0 && gi+di < nodes.length)
					for (int dj=-1;dj<2;dj++)
						if (gj+dj >= 0 && gj+dj < nodes[0].length)
							n++;
			neighbors = new Node [n];
			neighborDist = new double [n];
			n = 0;
			for (int di=-1;di<2;di++)
				if (gi+di >= 0 && gi+di < nodes.length)
					for (int dj=-1;dj<2;dj++)
						if (gj+dj >= 0 && gj+dj < nodes[0].length)
			{
				neighbors[n] = nodes[gi+di][gj+dj];
				neighborDist[n] = Math.sqrt((x-neighbors[n].x)*(x-neighbors[n].x)+(y-neighbors[n].y)*(y-neighbors[n].y));
				n++;
			}
		}
		void step()
		{
//			if (vx*vx+vy*vy < .00001)
//				active = false;
			x += vx-.001;
			y += vy;
			pvx = vx;
			pvy = vy;
			vx = 0;
			vy = 0;
		}
		void constrain()
		{
			for (int i=0;i<neighbors.length;i++)
			{
				double l = Math.sqrt((x-neighbors[i].x)*(x-neighbors[i].x)+(y-neighbors[i].y)*(y-neighbors[i].y));
				double cor = neighborCor*(l-neighborDist[i]);
				vx += cor*(neighbors[i].x-x);
				vy += cor*(neighbors[i].y-y);
				if (Math.abs(l-neighborDist[i]) > .0001)
					neighbors[i].active = true;
			}
		}
		double similarity(int r, int g, int b)
		{
			return (1-Math.abs(this.r-r)/255.)*(1-Math.abs(this.g-g)/255.)*(1-Math.abs(this.b-b)/255.);
		}
		double cfact = .5;
		void attract(int r, int g, int b, double dx, double dy)
		{
			double s = similarity(r, g, b);
			this.vx += (1-cfact+cfact*(1-complacency))*similarityCor*dx*s;
			this.vy += (1-cfact+cfact*(1-complacency))*similarityCor*dy*s;
		}
		void attract(BufferedImage image, double x0, double y0, int i, int j, int dx, int dy)
		{
			if (dx*dx+dy*dy == 0)
				return;
			double l = Math.sqrt(dx*dx+dy*dy);
			dx /= l;
			dy /= l;
			int rgb = image.getRGB(i, j);
			attract(ImageUtils.red(rgb), ImageUtils.green(rgb), ImageUtils.blue(rgb), dx, dy);
		}
		void attract(BufferedImage image, double x0, double y0)
		{
			int i0 = (int)(x-x0);
			int j0 = (int)(y-y0);
			if (i0 >= 0 && j0 >= 0 && i0 < image.getWidth() && j0 < image.getHeight())
			{
				int rgb = image.getRGB(i0, j0);
				complacency = similarity(ImageUtils.red(rgb), ImageUtils.green(rgb), ImageUtils.blue(rgb));
			}
			else return;
			
			for (int di=0;di<=searchRay;di++)
			{
				if (i0-di < 0 || i0+di >= image.getWidth())
					break;
				for (int idir=0;idir<2;idir++)
				{
					int i = i0+(idir == 0 ? -1 : 1)*di;
					for (int dj=0;dj<=searchRay;dj++)
					{
						if (j0-dj < 0 || j0+dj >= image.getHeight())
							break;
						for (int jdir=0;jdir<2;jdir++)
						{
							int j = j0+(jdir == 0 ? -1 : 1)*dj;
							if ((i-i0)*(i-i0)+(j-j0)*(j-j0) > 0 && (i-i0)*(i-i0)+(j-j0)*(j-j0) <= searchRay*searchRay)
								attract(image, x0, y0, i, j, i-i0, j-j0);
						}
					}
				}
			}
		}
	}
	
	BufferedImage image;
	int w, h;
	Node [][] nodes;
	
	public ImageGrid(BufferedImage image, int w, int h)
	{
		this.image = image;
		this.w = w;
		this.h = h;
		this.nodes = new Node [w][h];
		
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
				nodes[i][j] = new Node(image, i, j);
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
				nodes[i][j].buildNeighbors();
	}
	
	public void attract(BufferedImage image)
	{
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
				if (nodes[i][j].active)
					nodes[i][j].attract(image, 0, 0);
	}
	
	public void step()
	{
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
				if (nodes[i][j].active)
					nodes[i][j].constrain();
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
				if (nodes[i][j].active)
					nodes[i][j].step();
	}
	
	void translate(double tx, double ty)
	{
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++)
		{
			nodes[i][j].x += tx;
			nodes[i][j].y += ty;
		}
	}
	
	@SuppressWarnings("serial")
	public static void main(String [] args) throws Exception
	{
		int div = 16;
		BufferedImage fimg1 = ImageUtils.read(new File("C:\\Users\\aburn\\Documents\\pres\\Msu_18bis-140\\Msu_18bis_7.jpg"));
		BufferedImage fimg2 = ImageUtils.read(new File("C:\\Users\\aburn\\Documents\\pres\\Msu_18bis-140\\Msu_18bis_8.jpg"));
		final BufferedImage img1 = Scalr.resize(fimg1, fimg1.getWidth()/div, fimg1.getHeight()/div);
		final BufferedImage img2 = Scalr.resize(fimg2, fimg2.getWidth()/div, fimg2.getHeight()/div);
		final ImageGrid grid = new ImageGrid(img2, img2.getWidth(), img2.getHeight());
		//grid.translate(90*(img1.getWidth()-1)/100, 0);
		grid.translate(105*(img1.getWidth()-1)/100, 0);
		
		final JFrame win = new JFrame("Springy");
		win.add(new JPanel()
		{
			{setPreferredSize(new Dimension(1800, 1400));}
			@Override protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				
				double x0 = getWidth()/12;
				double y0 = getHeight()/12;
				double scale = 670/img1.getWidth();
				int iscale = (int)Math.round(scale);
				g.drawImage(img1, (int)x0, (int)y0, (int)(x0+scale*(img1.getWidth()-1)), (int)(y0+scale*(img1.getHeight()-1)), 0, 0, img1.getWidth(), img1.getHeight(), null);
				
				for (int i=0;i<grid.nodes.length;i++)
					for (int j=0;j<grid.nodes[i].length;j++)
				{
					Node n = grid.nodes[i][j];
					//if (n.active)
					g.setColor(n.col);
					//else g.setColor(Color.gray);
					double x = n.x, y = n.y;
					g.fillRect((int)(x0+scale*x), (int)(y0+scale*y), iscale, iscale);
//					double vx = n.pvx, vy = n.pvy;
//					g.setColor(n.active ? Color.red : Color.blue);
//					g.drawLine((int)(x0+scale*x)+iscale/2, (int)(y0+scale*y)+iscale/2, (int)(x0+scale*(x+10*vx))+iscale/2, (int)(y0+scale*(y+10*vy))+iscale/2);
				}
			}
		});
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setVisible(true);
		
		new Thread() {public void run()
		{
			while (true)
			{
				grid.attract(img1);
				grid.step();
				win.repaint();
//				try {Thread.sleep(30);}
//				catch (Exception e) {}
			}
		}}.start();
	}
}
