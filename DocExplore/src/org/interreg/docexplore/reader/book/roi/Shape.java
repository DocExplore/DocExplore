/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book.roi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Shape
{
	public float [][] points;
	public float minx, miny, maxx, maxy;
	static float lim = .001f;
	public float [][][] innerTriangulation, outerTriangulation;
	
	public Shape(float [][] points)
	{
		this.points = Arrays.copyOf(points, points.length);
		
		if (isClockwise(points))
			reverse(this.points);
		
		minx = maxx = points[0][0];
		miny = maxy = points[0][1];
		for (int i=1;i<points.length;i++)
		{
			if (points[i][0] < lim) points[i][0] = lim;
			else if (points[i][0] > 1-lim) points[i][0] = 1-lim;
			if (points[i][1] < lim) points[i][1] = lim;
			else if (points[i][1] > 1-lim) points[i][1] = 1-lim;
			
			if (points[i][0] < minx) minx = points[i][0];
			else if (points[i][0] > maxx) maxx = points[i][0];
			if (points[i][1] < miny) miny = points[i][1];
			else if (points[i][1] > maxy) maxy = points[i][1];
		}
		
		innerTriangulation = triangulate(this.points, true);
		outerTriangulation = triangulate(this.points, false);
	}
	
	private static void reverse(float [][] points)
	{
		for (int i=0;i<points.length/2;i++)
		{
			float [] tmp = points[i];
			points[i] = points[points.length-1-i];
			points[points.length-1-i] = tmp;
		}
	}
	
	public boolean contains(float qx, float qy)
	{
		float vx = maxx+1-qx;
		float vy = maxy+1-qy;
		
		int nInters = 0;
		for (int i=0;i<points.length;i++)
		{
			float px = points[i][0];
			float py = points[i][1];
			
			float ux = points[(i+1)%points.length][0]-px;
			float uy = points[(i+1)%points.length][1]-py;
			
			float k = ((qx-px)*vy-(qy-py)*vx)/(ux*vy-uy*vx);
			if (k < 0 || k >= 1)
				continue;
			
			float kp = vx*vx > vy*vy ? (px+k*ux-qx)/vx : (py+k*uy-qy)/vy;
			if (kp >= 0 && kp < 1)
				nInters++;
		}
		
		return nInters%2 == 1;
	}
	
	public static boolean intersects(float [][] points, int i1, int i2, int j1, int j2)
	{
		if (i1 == i2 || j1 == j2)
			throw new RuntimeException("!!!");
		if (i1 > i2) {int tmp = i1; i1 = i2; i2 = tmp;}
		if (j1 > j2) {int tmp = j1; j1 = j2; j2 = tmp;}
		if (i1 == j1 || i1 == j2 || i2 == j1 || i2 == j2)
			return false;
		return intersects(points[i1][0], points[i1][1], points[i2][0]-points[i1][0], points[i2][1]-points[i1][1],
			points[j1][0], points[j1][1], points[j2][0]-points[j1][0], points[j2][1]-points[j1][1]);
	}
	public static boolean intersects(float x1, float y1, float vx1, float vy1, 
		float x2, float y2, float vx2, float vy2)
	{
		if (vx1*vy2-vy1*vx2 == 0)
			return false;
		float k = (x2*vy2-y2*vx2-x1*vy2+y1*vx2)/(vx1*vy2-vy1*vx2);
		if (k <= 0 || k >= 1)
			return false;
		float dx = x1+k*vx1-x2, dy = y1+k*vy1-y2;
		float kp = vx2*vx2 > vy2*vy2 ? dx/vx2 : dy/vy2;
		return kp > 0 && kp < 1;
	}
	
	public static float [][][] triangulate(float [][] outline, boolean inside)
	{
		float [][] points;
		
		if (!inside)
		{
			points = Arrays.copyOf(outline, outline.length+4);
			points[points.length-4] = new float [] {0, 0};
			points[points.length-3] = new float [] {1, 0};
			points[points.length-2] = new float [] {1, 1};
			points[points.length-1] = new float [] {0, 1};
		}
		else
		{
			points = Arrays.copyOf(outline, outline.length);
			reverse(points);
		}
		
		int [][] lines = new int [points.length][];
		
		for (int i=0;i<outline.length-1;i++)
			addLine(lines, i, i+1);
		addLine(lines, 0, outline.length-1);
		if (!inside)
		{
			addLine(lines, points.length-4, points.length-3);
			addLine(lines, points.length-3, points.length-2);
			addLine(lines, points.length-2, points.length-1);
			addLine(lines, points.length-4, points.length-1);
		}
		
		for (int i=0;i<points.length-1;i++)
			for (int j=i+1;j<points.length;j++)
			{
				int ip = (i+outline.length-1)%outline.length, in = (i+1)%outline.length;
				if (!isIn(points[j][0]-points[i][0], points[j][1]-points[i][1],
					points[i][0]-points[ip][0], points[i][1]-points[ip][1],
					points[in][0]-points[i][0], points[in][1]-points[i][1]))
						continue;
				
				boolean intersects = false;
				
				for (int k=0;k<lines.length && !intersects;k++)
					if (lines[k] != null)
					{
						for (int li=0;li<lines[k].length && !intersects;li++)
						{
							int l = lines[k][li];
							if (k != i && k != j && l != i && l != j)
								intersects = intersects(points, i, j, k, l);
						}
					}
				if (!intersects)
					addLine(lines, i, j);
			}
		
		Vector<float [][]> triangles = new Vector<float [][]>();
		for (int i=0;i<lines.length;i++)
			if (lines[i] != null)
				for (int j=0;j<lines[i].length;j++)
					if (i < lines[i][j])
					{
						int [] ks = trianglesFor(lines, i, lines[i][j]);
						for (int k=0;k<ks.length;k++)
							triangles.add(buildTriangle(points, i, lines[i][j], ks[k]));
					}
		return triangles.toArray(new float [][][] {});
	}
	private static int [] trianglesFor(int [][] lines, int i, int j)
	{
		int [] res = new int [0];
		for (int k=0;k<lines[i].length;k++)
			for (int l=0;l<lines[j].length;l++)
				if (lines[i][k] == lines[j][l] && j < lines[i][k])
				{
					res = Arrays.copyOf(res, res.length+1);
					res[res.length-1] = lines[i][k];
					break;
				}
		return res;
	}
	private static float [][] buildTriangle(float [][] points, int i, int j, int k)
	{
		return new float [][]
		{
			{points[i][0], points[i][1]},
			{points[j][0], points[j][1]},
			{points[k][0], points[k][1]},
		};
	}
	private static void addLine(int [][] lines, int i, int j)
	{
		if (lines[i] == null)
			lines[i] = new int [1];
		else lines[i] = Arrays.copyOf(lines[i], lines[i].length+1);
		lines[i][lines[i].length-1] = j;
		
		if (lines[j] == null)
			lines[j] = new int [1];
		else lines[j] = Arrays.copyOf(lines[j], lines[j].length+1);
		lines[j][lines[j].length-1] = i;
	}
	
	//checks if u extends "to the left" of v
	private static boolean isLeft(float ux, float uy, float vx, float vy) {return ux*vy-uy*vx > 0;}
	//checks if u extends "inside" of <v1, v2>
	private static boolean isIn(float ux, float uy, float vx1, float vy1, float vx2, float vy2)
	{
		boolean intersection = isLeft(vx2, vy2, vx1, vy1);
		if (intersection)
			return isLeft(ux, uy, vx1, vy1) && isLeft(ux, uy, vx2, vy2);
		else return isLeft(ux, uy, vx1, vy1) || isLeft(ux, uy, vx2, vy2);
	}
	public static boolean isClockwise(float [][] points)
	{
		int mini = 0;
		for (int i=1;i<points.length;i++)
			if (points[i][0] < points[mini][0] || (points[i][0] == points[mini][0] && points[i][1] < points[mini][1]))
				mini = i;
		int ip = (mini+points.length-1)%points.length, in = (mini+1)%points.length;
		return !isLeft(points[mini][0]-points[ip][0], points[mini][1]-points[ip][1],
			points[in][0]-points[mini][0], points[in][1]-points[mini][1]);
	}
	
//	public static Mesh toMesh(List<float [][][]> triangles)
//	{
//		int nVertices = 0;
//		for (float [][][] triangle : triangles)
//			nVertices += 3*triangle.length;
//		Mesh mesh = new Mesh(true, nVertices, nVertices, new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 3, "p")));
//		FloatBuffer data = mesh.getVerticesBuffer();
//		data.clear();
//		int vcnt = 0;
//		for (float [][][] triangle : triangles)
//			for (int i=0;i<triangle.length;i++)
//				for (int j=0;j<3;j++)
//				{
//					//data.put(vcnt)
//				}
//	}
	
	@SuppressWarnings("serial")
	public static void main(String [] args)
	{
		final int w = 640, h = 480;
		float [][] outline = new float [][]
		{
			{74f/w, 41f/h},
			{183f/w, 178f/h},
			{69f/w, 294f/h},
			{335f/w, 182f/h},
			{281f/w, 333f/h},
			{200f/w, 281f/h},
			{192f/w, 433f/h},
			{417f/w, 355f/h},
			{436f/w, 449f/h},
			{613f/w, 354f/h},
			{614f/w, 207f/h},
			{446f/w, 212f/h},
			{528f/w, 77f/h},
			{616f/w, 65f/h},
			{555f/w, 21f/h},
			{365f/w, 52f/h},
			{418f/w, 84f/h},
			{325f/w, 125f/h},
			{320f/w, 171f/h},
			{296f/w, 37f/h},
			{204f/w, 76f/h},
			{151f/w, 6f/h}
		};
		//reverse(outline);
		final Shape shape = new Shape(outline);
		final float [][][] triangles = shape.outerTriangulation;
		System.out.println(triangles.length+" triangles");
		
		JFrame win = new JFrame("Test");
		JLabel canvas = new JLabel() {protected void paintComponent(Graphics g)
		{
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.setColor(new Color(255, 0, 0, 127));
			for (int i=0;i<triangles.length;i++)
				g.fillPolygon(
					new int [] {(int)(w*triangles[i][0][0]), (int)(w*triangles[i][1][0]), (int)(w*triangles[i][2][0])}, 
					new int [] {(int)(h*triangles[i][0][1]), (int)(h*triangles[i][1][1]), (int)(h*triangles[i][2][1])}, 
					3);
			
			g.setColor(new Color(0, 0, 255, 127));
			for (int i=0;i<shape.points.length;i++)
				g.drawLine((int)(w*shape.points[i][0]), (int)(h*shape.points[i][1]), 
					(int)(w*shape.points[(i+1)%shape.points.length][0]), (int)(h*shape.points[(i+1)%shape.points.length][1]));
		}};
		canvas.setPreferredSize(new Dimension(w, h));
		canvas.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				System.out.println("{"+e.getX()+", "+e.getY()+"}");
			}
		});
		win.add(canvas);
		win.pack();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setVisible(true);
	}
}
