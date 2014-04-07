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

import org.interreg.docexplore.internationalization.XMLResourceBundle;

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
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("dialogCloseLabel")) {public void actionPerformed(ActionEvent e) 
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
