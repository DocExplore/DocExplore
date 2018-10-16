/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.gui.image;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.interreg.docexplore.gui.ErrorHandler;

public class NavViewInputListener implements MouseListener, MouseMotionListener, MouseWheelListener
{
	NavView view;
	int panningButton;
	
	public NavViewInputListener(NavView view)
	{
		this(view, InputEvent.BUTTON3_MASK);
	}
	public NavViewInputListener(NavView view, int panningButton)
	{
		if (panningButton == InputEvent.BUTTON1_MASK)
		{
			ErrorHandler.defaultHandler.submit(new Exception("Can't use left click as panning button, switching to right"), false);
			panningButton = InputEvent.BUTTON3_MASK;
		}
		this.view = view;
		this.panningButton = panningButton;
	}
	
	boolean panning = false;
	int panDownX = 0, panDownY = 0, panCurX = 0, panCurY = 0;
	@Override public void mousePressed(MouseEvent e)
	{
		if ((e.getModifiers() & panningButton) != 0)
		{
			panning = true;
			panDownX = panCurX = e.getX();
			panDownY = panCurY = e.getY();
			return;
		}
	}
	@Override public void mouseReleased(MouseEvent e)
	{
		if ((e.getModifiers() & panningButton) != 0)
			panning = false;
	}

	@Override public void mouseMoved(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e)
	{
		if (panning)
		{
			int dx = e.getX()-panCurX;
			int dy = e.getY()-panCurY;
			panCurX = e.getX();
			panCurY = e.getY();
			view.scrollPixels(dx, dy);
		}
	}

	double k = 1.2;
	@Override public void mouseWheelMoved(MouseWheelEvent e)
	{
		int r = e.getWheelRotation();
		double xc = view.toViewX(e.getX()), yc = view.toViewY(e.getY());
		if (r == 0)
			return;
		view.scale *= Math.pow(k, -r);
		double xp = view.fromViewX(xc), yp = view.fromViewY(yc);
		view.scrollPixels(e.getX()-xp, e.getY()-yp);
	}
	
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}
