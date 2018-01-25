/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.interreg.docexplore.internationalization.Lang;

@SuppressWarnings("serial")
public class ResourceMonitor extends JDialog
{
	final int historySize = 200;
	long [][] history;
	int cursor;
	
	public ResourceMonitor()
	{
		super(JOptionPane.getRootFrame(), false);
		
		this.history = new long [historySize][2];
		this.cursor = 0;
		for (long [] frame : history)
			{frame[0] = 0; frame[1] = 0;}
		
		setAlwaysOnTop(true);
		setLayout(new BorderLayout(10, 10));
		add(new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				long top = 1024*1024*1024;
				
				for (int i=0;i<history.length;i++)
				{
					int index = (cursor+i)%history.length;
					
					int x0 = i*getWidth()/history.length;
					int w = (i+1)*getWidth()/history.length-x0;
					
					g.setColor(Color.green);
					int h2 = (int)(history[index][1]*getHeight()/top);
					g.fillRect(x0, getHeight()-1-h2, w, h2);
					
					g.setColor(Color.red);
					int h1 = (int)(history[index][0]*getHeight()/top);
					g.fillRect(x0, getHeight()-1-h1, w, h1);
					
					long [] last = history[(cursor+history.length-1)%history.length];
					g.setColor(Color.white);
					g.drawString(last[0]/(1024*1024)+"/"+last[1]/(1024*1024), 0, 20);
				}
			}
		}, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction("GC") {public void actionPerformed(ActionEvent e) 
			{System.gc();}}));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("dialogCloseLabel")) {public void actionPerformed(ActionEvent e) 
			{setVisible(false);}}));
		add(buttonPanel, BorderLayout.SOUTH);
		
		new Thread()
		{
			public void run()
			{
				while (true)
				{
					long tm = Runtime.getRuntime().totalMemory();
					history[cursor][0] = tm-Runtime.getRuntime().freeMemory();
					history[cursor][1] = tm;
					//System.out.println("\r"+history[cursor][0]/(1024*1024)+"/"+tm/(1024*1024));
					cursor = (cursor+1)%history.length;
					repaint();
					
					try {Thread.sleep(333);}
					catch (InterruptedException e) {}
				}
			}
		}.start();
		
		setPreferredSize(new Dimension(640, 480));
		pack();
	}
}
