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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.interreg.docexplore.reader.gfx.Math3D;
import org.interreg.docexplore.util.Pair;

public class Smooth
{
	public double [][] points, vs;
	public double [] lengths;
	public double length;
	
	public Smooth(double [][] points)
	{
		this.points = points;
		this.vs = new double [points.length][3];
		this.lengths = new double [points.length-1];
		if (points.length == 1)
			return;
		
		length = 0;
		for (int i=0;i<points.length;i++)
		{
			Math3D.normalize(Math3D.diff(i == 0 ? points[i+1] : points[i], i == 0 ? points[i] : points[i-1], buf1), buf1);
			Math3D.normalize(Math3D.diff(i == points.length-1 ? points[i-1] : points[i], i == points.length-1 ? points[i] : points[i+1], buf2), buf2);
			Math3D.normalize(Math3D.diff(buf1, buf2, vs[i]), vs[i]);
			if (i < points.length-1)
			{
				lengths[i] = Math3D.norm(Math3D.diff(points[i], points[i+1], buf1));
				length += lengths[i];
			}
		}
	}
	
	double [] buf1 = {0, 0, 0}, buf2 = {0, 0, 0};
	double [] smooth(double [] p1, double [] v1, double [] p2, double [] v2, double at, double [] res)
	{
		//at = 3*at*at-2*at*at*at;
		//res[0] = (1-at)*(p1[0]+at*v1[0])+at*(p2[0]+v2[0]-at*v2[0]);
		double x = 3*at*at-2*at*at*at;
		Math3D.scale(Math3D.add(p1, Math3D.scale(v1, at, buf1), buf1), 1-x, buf1);
		Math3D.scale(Math3D.add(Math3D.add(p2, Math3D.scale(v2, -at, buf2), buf2), v2, buf2), x, buf2);
		Math3D.add(buf1,  buf2, res);
		return res;
	}
	
	double [] v1 = {0, 0, 0}, v2 = {0, 0, 0};
	public double [] at(int s, double at, double [] res)
	{
		if (lengths.length == 0)
			return Math3D.set(res, points[0]);
		at = at < 0 ? 0 : at > 1 ? 1 : at;
		Math3D.scale(vs[s], .5*lengths[s], v1);
		Math3D.scale(vs[s+1], -.5*lengths[s], v2);
		return smooth(points[s], v1, points[s+1], v2, at, res);
	}
	public double [] at(double at, double [] res) {return at(at, res, null);}
	public double [] at(double at, double [] res, Pair<Integer, Double> where)
	{
		if (lengths.length == 0)
		{
			if (where != null)
				{where.first = 0; where.second = 0.;}
			return Math3D.set(res, points[0]);
		}
		int s = 0;
		double cur = 0;
		while (at*length > cur+lengths[s] && s < lengths.length-1)
			cur += lengths[s++];
		at = (at*length-cur)/lengths[s];
		if (where != null)
			{where.first = s; where.second = at;}
		return at(s, at, res);
	}
	
	@SuppressWarnings("serial")
	public static void main(String [] args)
	{
		double [][] points = {{189, 357, 0}, {133, 402, 0}, {332, 279, 0}, {509, 284, 0}, {147, 286, 0}, {331, 209, 0}};
		final Smooth smooth = new Smooth(points);
		
		JFrame frame = new JFrame("Smooth");
		final JLabel canvas = new JLabel()
		{
			double [] buf = {0, 0, 0};
			double a = 0;
			public void paintComponent(java.awt.Graphics g)
			{
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				
				g.setColor(Color.black);
				int ppx = 0, ppy = 0;
				boolean first = true;
				int steps = 20;
				for (int i=0;i<smooth.points.length-1;i++)
				{
					for (int j=0;j<steps;j++)
					{
						smooth.at(i, j*1./steps, buf);
						int px = (int)buf[0], py = (int)buf[1];
						if (!first)
							g.drawLine(ppx, ppy, px, py);
						first = false;
						ppx = px;
						ppy = py;
					}
				}
				
				int ray = 3;
				for (int i=0;i<smooth.points.length;i++)
				{
					g.setColor(Color.red);
					g.drawRect((int)smooth.points[i][0]-ray, (int)smooth.points[i][1]-ray, 2*ray, 2*ray);
					
					g.setColor(Color.blue);
					g.drawLine((int)(smooth.points[i][0]-10*ray*smooth.vs[i][0]), (int)(smooth.points[i][1]-10*ray*smooth.vs[i][1]), 
						(int)(smooth.points[i][0]+10*ray*smooth.vs[i][0]), (int)(smooth.points[i][1]+10*ray*smooth.vs[i][1]));
				}
				
				smooth.at(.5+.5*Math.sin(a), buf);
				g.setColor(Color.green);
				g.fillRect((int)(buf[0]-ray), (int)(buf[1]-ray), 2*ray, 2*ray);
				a += .01;
			}
		};
		canvas.setPreferredSize(new Dimension(600, 600));
		frame.add(canvas);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		new Thread() {public void run()
		{
			while (true)
			{
				try {Thread.sleep(25);}
				catch (Exception e) {}
				canvas.repaint();
			}
		}}.start();
	}
}
