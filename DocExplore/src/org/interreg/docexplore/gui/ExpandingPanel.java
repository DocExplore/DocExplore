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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ExpandingPanel extends JPanel
{
	private static final long serialVersionUID = -6973271744530044617L;
	
	public static Icon doubleUpArrow = new Icon()
	{
		public int getIconHeight() {return 12;}
		public int getIconWidth() {return 13;}

		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			g.setColor(Color.white);
			
			g.drawLine(3, 5, 6, 2);
			g.drawLine(6, 2, 9, 5);
			g.drawLine(4, 5, 6, 3);
			g.drawLine(6, 3, 8, 5);
			
			g.drawLine(3, 9, 6, 6);
			g.drawLine(6, 6, 9, 9);
			g.drawLine(4, 9, 6, 7);
			g.drawLine(6, 7, 8, 9);
		}
	};
	
	public static Icon doubleDownArrow = new Icon()
	{
		public int getIconWidth() {return 13;}
		public int getIconHeight() {return 12;}
		
		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			g.setColor(Color.white);
			
			g.drawLine(3, 6, 6, 9);
			g.drawLine(6, 9, 9, 6);
			g.drawLine(4, 6, 6, 8);
			g.drawLine(6, 8, 8, 6);
			
			g.drawLine(3, 2, 6, 5);
			g.drawLine(6, 5, 9, 2);
			g.drawLine(4, 2, 6, 4);
			g.drawLine(6, 4, 8, 2);
		}
	};
	
	static class AnimationMetric
	{
		Rectangle from, to;
		
		AnimationMetric(Rectangle from, Rectangle to)
		{
			this.from = from;
			this.to = to;
		}
		
		void setAt(Component c, double t)
		{
			c.setBounds((int)(from.x+t*(to.x-from.x)), 
				(int)(from.y+t*(to.y-from.y)), 
				(int)(from.width+t*(to.width-from.width)), 
				(int)(from.height+t*(to.height-from.height)));
		}
	}
	
	String title;
	boolean expanded;
	JPanel contentPane;
	JLabel expandButton;
	
	Dimension titleBarDimension;
	
	public ExpandingPanel(String title)
	{
		super(new LooseGridLayout(0, 1, 2, 2, true, false, 
			SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		
		this.title = title;
		this.expanded = false;
		this.contentPane = new JPanel(new BorderLayout());
		
		setBorder(BorderFactory.createLineBorder(Color.black, 2));
		
		JPanel titleBar = new JPanel(new BorderLayout());
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setOpaque(false);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setForeground(Color.white);
		titleBar.add(titleLabel, BorderLayout.WEST);
		
		JPanel filler = new JPanel();
		filler.setOpaque(false);
		titleBar.add(filler, BorderLayout.CENTER);
		
		titleBar.setBackground(new Color(.1f, .2f, .5f));
		titleBar.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		this.expandButton = new JLabel(doubleDownArrow, SwingConstants.CENTER);
		expandButton.setOpaque(false);
		expandButton.setFocusable(false);
		expandButton.setBackground(new Color(.2f, .4f, .8f));
		expandButton.addMouseListener(new MouseAdapter()
		{
			public void mouseEntered(MouseEvent e) {
				expandButton.setOpaque(true); expandButton.repaint();}
			public void mouseExited(MouseEvent e) {
				expandButton.setOpaque(false); expandButton.repaint();}
			
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1) return;
				
				if (expanded) collapse();
				else expand();
			}
		});
		titleBar.add(expandButton, BorderLayout.EAST);
		titleBarDimension = titleBar.getPreferredSize();
		
		super.add(titleBar);
		//add(contentPane, BorderLayout.SOUTH);
	}
	
	public boolean isExpanded() {return expanded;}
	
	public void expand()
	{
		if (expanded) return;
		
		Rectangle from = getBounds();
		add(contentPane);
		getParent().doLayout();
		Rectangle to = getBounds();
		final AnimationMetric am = new AnimationMetric(from, to);
		
		expandButton.setIcon(doubleUpArrow);
		expanded = true;
		
		final double animTime = .5;
		final int nSteps = 20;
		new Thread()
		{
			//expand animation
			public void run()
			{
				for (int i=0;i<nSteps;i++)
				{
					am.setAt(ExpandingPanel.this, (1.*i)/nSteps);
					repaint();
					
					try {Thread.sleep((long)(1000*animTime/nSteps));}
					catch (InterruptedException e) {}
				}
				
				am.setAt(ExpandingPanel.this, 1);
				getParent().validate();
			}
		}.start();
	}
	
	public void collapse()
	{
		if (!expanded) return;
		
		Rectangle from = getBounds();
		Rectangle to = collapsedDimension();
		final AnimationMetric am = new AnimationMetric(from, to);
		
		expandButton.setIcon(doubleDownArrow);
		expanded = false;
		
		final double animTime = .5;
		final int nSteps = 20;
		new Thread()
		{
			//collapse animation
			public void run()
			{
				for (int i=0;i<nSteps;i++)
				{
					am.setAt(ExpandingPanel.this, (1.*i)/nSteps);
					repaint();
					
					try {Thread.sleep((long)(1000*animTime/nSteps));}
					catch (InterruptedException e) {}
				}
				
				remove(contentPane);
				am.setAt(ExpandingPanel.this, 1);
				getParent().validate();
			}
		}.start();
	}
	
	public JPanel getContentPane() {return contentPane;}

	public Rectangle collapsedDimension()
	{
		if (expanded)
		{
			remove(contentPane);
			getParent().validate();
		}
		Rectangle dim = getBounds();
		if (expanded)
		{
			add(contentPane);
			getParent().validate();
		}
		return dim;
	}
}
