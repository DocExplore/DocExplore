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
