package org.interreg.docexplore.stitcher;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class NavViewInputListener implements MouseListener, MouseMotionListener, MouseWheelListener
{
	NavView view;
	
	public NavViewInputListener(NavView view)
	{
		this.view = view;
	}
	
	boolean panning = false;
	int panDownX = 0, panDownY = 0;
	@Override public void mousePressed(MouseEvent e)
	{
		if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
		{
			panning = true;
			panDownX = e.getX();
			panDownY = e.getY();
			return;
		}
	}
	@Override public void mouseReleased(MouseEvent e)
	{
		if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
			panning = false;
	}

	@Override public void mouseMoved(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e)
	{
		if (panning)
		{
			int dx = e.getX()-panDownX;
			int dy = e.getY()-panDownY;
			panDownX = e.getX();
			panDownY = e.getY();
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
