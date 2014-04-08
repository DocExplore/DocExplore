/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.interreg.docexplore.reader.book.page.PaperCurve;


public class TestPageStack
{
	public static void main(String [] args)
	{
		JFrame win = new JFrame("Hey!");
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final float coverLength = 400, coverDepth = 40;
		final float [] sep = {.75f};
		final float [] le = {0, -20}, lh = {coverLength, 0};
		final float [] rh = {coverLength+coverDepth, 0}, re = {2*coverLength+coverDepth, 0};
		@SuppressWarnings("serial")
		final JLabel canvas = new JLabel()
		{
			
			public synchronized void paintComponent(Graphics g)
			{
				Graphics2D g2d = (Graphics2D)g;
				g2d.scale(1, -1);
				g2d.translate(0, -getHeight());
				
				int x0 = (int)(getWidth()-2*coverLength-coverDepth)/2;
				int y0 = 400;
				
				g.setColor(Color.blue);
				g.drawLine((int)(x0+le[0]), (int)(y0+le[1]), (int)(x0+lh[0]), (int)(y0+lh[1]));
				g.setColor(Color.red);
				g.drawLine((int)(x0+coverLength), y0, (int)(x0+coverLength+coverDepth), y0);
				g.setColor(Color.blue);
				g.drawLine((int)(x0+re[0]), (int)(y0+re[1]), (int)(x0+rh[0]), (int)(y0+rh[1]));
				
				g.setColor(Color.black);
				float [][] path = new float [30][2];
				PaperCurve.compute(x0+coverLength+coverDepth, y0, 
					1f, .75f, 
					re[0]-rh[0], re[1]-rh[1], 
					coverLength, path.length-1, .5f+sep[0], path);
				for (int i=0;i<path.length-1;i++)
					g.drawLine((int)path[i][0], (int)path[i][1], (int)path[i+1][0], (int)path[i+1][1]);
				
				float [][] path2 = new float [path.length][2];
				PaperCurve.project(path, -sep[0]*coverDepth, 0, coverLength, path2);
				for (int i=0;i<path.length-1;i++)
					g.drawLine((int)path2[i][0], (int)path2[i][1], (int)path2[i+1][0], (int)path2[i+1][1]);
				
				g.drawLine((int)(path[path.length-1][0]), (int)(path[path.length-1][1]), 
					(int)(path2[path2.length-1][0]), (int)(path2[path2.length-1][1]));
				
				PaperCurve.compute(x0+coverLength, y0, 
					-1f, .75f, 
					le[0]-lh[0], le[1]-lh[1],  
					coverLength, path.length-1, .5f+(1-sep[0]), path);
				for (int i=0;i<path.length-1;i++)
					g.drawLine((int)path[i][0], (int)path[i][1], (int)path[i+1][0], (int)path[i+1][1]);
				
				PaperCurve.project(path, (1-sep[0])*coverDepth, 0, coverLength, path2);
				for (int i=0;i<path.length-1;i++)
					g.drawLine((int)path2[i][0], (int)path2[i][1], (int)path2[i+1][0], (int)path2[i+1][1]);
				for (int i=0;i<path.length;i++)
					if (path[i][0]*path[i][0]+path[i][1]*path[i][1] < 25)
						System.out.println("!");
				g.drawLine((int)(path[path.length-1][0]), (int)(path[path.length-1][1]), 
					(int)(path2[path2.length-1][0]), (int)(path2[path2.length-1][1]));
			}
		};
		canvas.setPreferredSize(new Dimension(1200, 800));
		win.add(canvas);
		win.pack();
		win.setVisible(true);
		
		new Thread()
		{
			float t = 0;//(float)(5*Math.PI-.05);
			public void run()
			{
				while (true)
				{
					try {Thread.sleep(33);}
					catch (Exception e) {}
					
					synchronized (canvas)
					{
						t += .05;
						sep[0] = .025f+.95f*(float)(.5*(Math.sin(t/2)+1));
						
						float a = (float)(0*Math.PI+.5*Math.PI*Math.sin(t/3));
						//le[0] = (float)(lh[0]-coverLength*Math.cos(a));
						//le[1] = (float)(lh[1]+coverLength*Math.sin(a));
						
						a = -(float)(0+.5*Math.PI*Math.sin(t/3+.7));
						re[0] = (float)(rh[0]+coverLength*Math.cos(a));
						re[1] = (float)(rh[1]-coverLength*Math.sin(a));
					}
					
					canvas.repaint();
				}
			}
		}.start();
	}
}
