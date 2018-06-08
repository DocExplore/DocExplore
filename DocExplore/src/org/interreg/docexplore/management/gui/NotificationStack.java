/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.gui;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class NotificationStack extends JPanel
{
	static final int nw = 200, nh = 75;
	 
	static class Notification extends JLabel
	{
		NotificationStack stack;
		int h = 0;
		boolean expanding = true, collapsing = false;
		
		static Color border = new Color(.45f, .45f, .75f);
		static Color background = new Color(.95f, .95f, 1f);
		
		Notification(final NotificationStack stack, String s)
		{
			super("<html><b>"+s+"</b></html>");
			
			this.stack = stack;
			getInsets().set(10, 10, 10, 10);
			setOpaque(true);
			setBackground(background);
			setForeground(Color.black);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
			
			new Thread() {public void run()
			{
				try {Thread.sleep(10000);}
				catch (Exception e) {}
				expanding = false;
				collapsing = true;
				stack.resizingNeeded = true;
			}}.start();
		}
	}
	
	MMTApp win;
	int comph = 0;
	Vector<Notification> notifications = new Vector<Notification>();
	
	public NotificationStack(final MMTApp win)
	{
		super(null);
		
		this.win = win;
		win.addComponentListener(new ComponentAdapter() {public void componentResized(ComponentEvent arg0) {syncWithMainWindow();}});
		
		new Thread() {public void run()
		{
			while (true)
			{
				try {Thread.sleep(33);}
				catch (Exception e) {}
				if (resizingNeeded)
					SwingUtilities.invokeLater(resizingTask);
			}
		}}.start();
		
		addMouseListener(new MouseAdapter() {public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
				win.host.plugins.analysisPluginSetup.setVisible(true);
		}});
	}
	
	private void syncWithMainWindow()
	{
		setBounds(win.getWidth()-nw-1-win.getInsets().right-win.getInsets().left, win.getHeight()-comph-1-win.getInsets().top-win.getInsets().bottom, nw, comph);
	}
	private void redoLayout()
	{
		int h = 0;
		for (int i=notifications.size()-1;i>=0;i--)
		{
			Notification n = notifications.get(i);
			boolean dimensionsChanged = n.getHeight() != n.h;
			n.setBounds(0, h, nw, n.h);
			if (dimensionsChanged)
				n.validate();
			h += n.h;
		}
		comph = h;
		syncWithMainWindow();
		repaint();
	}
	
	Runnable resizingTask = new Runnable() {public void run() {handleResizing();}};
	boolean resizingNeeded = false;
	private void handleResizing()
	{
		resizingNeeded = false;
		for (int i=notifications.size()-1;i>=0;i--)
		{
			Notification n = notifications.get(i);
			if (n.expanding)
			{
				n.h += 6;
				if (n.h >= nh)
					n.expanding = false;
				else resizingNeeded = true;
			}
			else if (n.collapsing)
			{
				n.h -= 6;
				if (n.h <= 0)
				{
					n.collapsing = false;
					notifications.remove(i);
					remove(n);
				}
				else resizingNeeded = true;
			}
		}
		redoLayout();
	}
	
	public void addNotification(String s)
	{
		Notification n = new Notification(this, s);
		add(n);
		notifications.add(n);
		redoLayout();
		resizingNeeded = true;
	}
}
