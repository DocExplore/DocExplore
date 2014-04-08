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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.interreg.docexplore.util.Math2D;

public class TriangulatedBox<T>
{
	static class Node<T>
	{
		double [] p;
		T t;
		Node(double [] p, T t) {this.p = p; this.t = t;}
	}
	
	Node<T> [] nodes;
	List<Node<T> []> triangles = new Vector<Node<T> []>();
	
	@SuppressWarnings("unchecked")
	public TriangulatedBox(double [][] points, T [] ts)
	{
		nodes = new Node [points.length];
		for (int i=0;i<points.length;i++)
			nodes[i] = new Node<T>(points[i], ts[i]);
		
		for (int i=0;i<nodes.length-2;i++)
			for (int j=i+1;j<nodes.length-1;j++)
				for (int k=j+1;k<nodes.length;k++)
					if (isDelaunayTriangle(nodes[i], nodes[j], nodes[k]))
						triangles.add(new Node [] {nodes[i], nodes[j], nodes[k]});
	}
	
	boolean isDelaunayTriangle(Node<T> a, Node<T> b, Node<T> c)
	{
		double [] o = circleCenter(a.p, b.p, c.p);
		double d2 = Math2D.distance2(a.p, o);
		for (Node<T> p : nodes)
			if (p != a && p != b && p != c && Math2D.distance2(p.p, o) < d2)
				return false;
		return true;
	}
	
	double [] circleCenter(double [] a, double [] b, double [] c)
	{
		double [] ab = Math2D.diff(a, b, new double [2]), ac = Math2D.diff(a, c, new double [2]);
		double [] abo = {ab[1], -ab[0]};
		double k = (.5*Math2D.dotProduct(ac, ac)-.5*Math2D.dotProduct(ab, ac))/(Math2D.dotProduct(abo, ac));
		
		double [] res = {0, 0};
		Math2D.add(a, Math2D.scale(ab, .5, res), res);
		return Math2D.add(res, Math2D.scale(abo, k, new double [2]), res);
	}
	
	double [] buf1 = {0, 0}, buf2 = {0, 0}, buf3 = {0, 0};
	double [] coords(double [] p, double [] a, double [] b, double [] c, double [] res)
	{
		double [] ap = Math2D.diff(a, p, buf1);
		double [] ab = Math2D.diff(a, b, buf2);
		double [] aco = Math2D.set(buf3, c[1]-a[1], a[0]-c[0]);
		res[0] = Math2D.dotProduct(ap, aco)/Math2D.dotProduct(ab, aco);
		
		double [] ac = Math2D.diff(a, c, buf2);
		double [] abo = Math2D.set(buf3, b[1]-a[1], a[0]-b[0]);
		res[1] = Math2D.dotProduct(ap, abo)/Math2D.dotProduct(ac, abo);
		
		return res;
	}
	
	double [] buf4 = {0, 0}, buf5 = {0, 0};
	Node<T> [] getTriangle(double x, double y, double [] res)
	{
		for (Node<T> [] tri : triangles)
		{
			Math2D.set(buf4, x, y);
			coords(buf4, tri[0].p, tri[1].p, tri[2].p, buf5);
			if (buf5[0] > 0 && buf5[1] > 0 && buf5[0]+buf5[1] <= 1)
				{Math2D.set(res, buf5); return tri;}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static void main(String [] args)
	{
		double w = 600, h = 800;
		double [][] points = new double [][] {
			{0, 0}, {w, 0}, {w, h}, {0, h}, {.5*w+.001, .5*h+.001}, {127, 13}, {46, 99}, {351, 121}, {297, 78}, {413, 211}, {546, 375}};
		final TriangulatedBox<Void> box = new TriangulatedBox<Void>(points, new Void [points.length]);
		
		final Node [][] selectedTri = {null};
		
		JFrame frame = new JFrame("Triangulated Box");
		@SuppressWarnings("serial")
		final JLabel canvas = new JLabel()
		{
			@SuppressWarnings("unchecked")
			public void paintComponent(Graphics g)
			{
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				
				g.setColor(Color.red);
				for (Node<Void> [] tri : box.triangles)
				{
					g.drawLine((int)tri[0].p[0], (int)tri[0].p[1], (int)tri[1].p[0], (int)tri[1].p[1]);
					g.drawLine((int)tri[1].p[0], (int)tri[1].p[1], (int)tri[2].p[0], (int)tri[2].p[1]);
					g.drawLine((int)tri[2].p[0], (int)tri[2].p[1], (int)tri[0].p[0], (int)tri[0].p[1]);
				}
				if (selectedTri[0] != null)
				{
					Node<Void> [] tri = selectedTri[0];
					g.setColor(Color.green);
					g.drawLine((int)tri[0].p[0], (int)tri[0].p[1], (int)tri[1].p[0], (int)tri[1].p[1]);
					g.drawLine((int)tri[1].p[0], (int)tri[1].p[1], (int)tri[2].p[0], (int)tri[2].p[1]);
					g.drawLine((int)tri[2].p[0], (int)tri[2].p[1], (int)tri[0].p[0], (int)tri[0].p[1]);
				}
			}
		};
		canvas.addMouseMotionListener(new MouseMotionAdapter()
		{
			double [] buf1 = {0, 0}, buf2 = {0, 0};
			public void mouseMoved(MouseEvent e)
			{
				for (Node<Void> [] tri : box.triangles)
				{
					Math2D.set(buf1, e.getX(), e.getY());
					box.coords(buf1, tri[0].p, tri[1].p, tri[2].p, buf2);
					if (buf2[0] > 0 && buf2[1] > 0 && buf2[0]+buf2[1] <= 1)
						{selectedTri[0] = tri; canvas.repaint(); return;}
				}
				selectedTri[0] = null;
				canvas.repaint();
			}
		});
		canvas.setPreferredSize(new Dimension((int)w+1, (int)h+1));
		frame.add(canvas);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
