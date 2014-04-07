package org.interreg.docexplore.reader.book;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;


public class TestCover
{
	public static void main(String [] args)
	{
		JFrame win = new JFrame("Hey!");
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		@SuppressWarnings("serial")
		final JLabel canvas = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				double [] p0 = {240, 240};
				double [] n0 = {0, 80};
				double [] p1 = {320, 240};
				double [] n1 = {0, -80};
				
				double oldx = -1, oldy = 0;
				for (double t=-1;t<2;t+=.001)
				{
					double x, y;
					if (t < 0) {x = p0[0]+n0[0]*t; y = p0[1]+n0[1]*t;}
					else if (t > 1) {x = p1[0]+n1[0]*(t-1); y = p1[1]+n1[1]*(t-1);}
					else {x = (1-t)*(p0[0]+n0[0]*t)+t*(p1[0]-n1[0]+n1[0]*t); y = (1-t)*(p0[1]+n0[1]*t)+t*(p1[1]-n1[1]+n1[1]*t);}
					
					g.setColor(Color.black);
					if (oldx > 0)
						g.drawLine((int)oldx, (int)oldy, (int)x, (int)y);
					oldx = x; oldy = y;
				}
			}
		};
		canvas.setPreferredSize(new Dimension(600, 600));
		win.add(canvas);
		win.pack();
		win.setVisible(true);
	}
}
