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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Icon;
import javax.swing.JButton;

public class GrowingButton extends JButton
{
	private static final long serialVersionUID = -5554185250176668540L;

	Icon icon;
	float max; 
	float min; 
	float step;
	float goal;
	float current;
	LinkedBlockingQueue<Float> queue;
	
	boolean glowing;
	MaskIcon mask;
	float glowLevel;
	float glowStep;
	
	public GrowingButton(Icon icon, float max, float min, float step, 
		ActionListener listener)
	{
		this.current = min;
		this.min = min;
		this.goal = min;
		this.max = max;
		this.step = step;
		this.icon = icon;
		this.queue = new LinkedBlockingQueue<Float>();
		this.glowing = false;
		this.mask = null;
		
		setPreferredSize(new Dimension((int)(max*icon.getIconWidth()), 
			(int)(max*icon.getIconHeight())));
		setBorder(null);
		setOpaque(false);
		addActionListener(listener);
		
		addMouseListener(new MouseAdapter()
		{
			public void mouseEntered(MouseEvent e)
			{
				queue.add(GrowingButton.this.max);
			}
			public void mouseExited(MouseEvent e)
			{
				queue.add(GrowingButton.this.min);
			}
		});
		
		if (isVisible())
			runAnimationThread();
	}
	
	public void setVisible(boolean visible)
	{
		if (visible == isVisible())
			return;
		super.setVisible(visible);
		
		if (visible)
			runAnimationThread();
		else queue.add(min);
	}
	
	void runAnimationThread()
	{
		queue.clear();
		new Thread()
		{
			public void run()
			{
				while (true)
				{
					goal = current;
					try {synchronized (queue) {goal = queue.take();}}
					catch (InterruptedException e) {}
					if (!isVisible())
						break;
					
					while (current != goal || glowing)
					{
						try {while (queue.size() > 0)
							goal = queue.take();}
						catch (InterruptedException e) {}
						
						float nextCurrent = current;
						if (goal > current)
						{
							nextCurrent += step;
							if (nextCurrent > goal)
								nextCurrent = goal;
						}
						else
						{
							nextCurrent -= step;
							if (nextCurrent < goal)
								nextCurrent = goal;
						}
						current = nextCurrent;
						
						if (glowing)
						{
							glowLevel += glowStep;
							if (glowStep > 0 && glowLevel >= 1)
							{
								glowLevel = 1;
								glowStep = -glowStep;
							}
							else if (glowStep < 0 && glowLevel <= 0)
							{
								glowLevel = 0;
								glowStep = -glowStep;
							}
							mask.setColor(new Color(1f, 1f, 1f, .5f*glowLevel));
						}
						
						repaint();
						
						try {Thread.sleep(50);}
						catch (Exception e) {}
					}
				}
			}
		}.start();
	}
	
	public void setGlowing(boolean glowing)
	{
		this.glowLevel = 0;
		this.glowStep = .05f;
		if (glowing && mask == null)
			this.mask = new MaskIcon(icon, 1f);
		this.glowing = glowing;
		if (glowing && queue.isEmpty())
			queue.add(goal);
	}
	
	protected void paintComponent(Graphics _g)
	{
		Graphics2D g = (Graphics2D)_g;
		if (isOpaque())
		{
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
		g.scale(current, current);
		g.translate((getWidth()-icon.getIconWidth()*current)/2, 
			(getHeight()-icon.getIconHeight()*current)/2);
		icon.paintIcon(this, g, 0, 0);
		if (glowing)
			mask.paintIcon(this, g, (icon.getIconWidth()-mask.getIconWidth())/2, 
				(icon.getIconHeight()-mask.getIconHeight())/2);
	}
}
