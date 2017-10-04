package org.interreg.docexplore.stitcher;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

public class FragmentViewMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener
{
	public static final double knobRay = 5;
	
	FragmentView view;
	
	public FragmentViewMouseListener(FragmentView view)
	{
		this.view = view;
	}
	
	boolean panning = false;
	double offsetx = 0, offsety = 0;
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
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		if (((e.getModifiers() & (InputEvent.BUTTON1_MASK+InputEvent.BUTTON3_MASK)) != 0) && view.knobFragment != null)
		{
			view.knob.onGrab(view.knobFragment, mx, my);
			view.repaint();
		}
		else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && view.highlighted != null)
		{
			view.selected = view.highlighted;
			offsetx = view.selected.uix-view.toViewX(e.getX());
			offsety = view.selected.uiy-view.toViewY(e.getY());
			view.repaint();
		}
	}
	@Override public void mouseReleased(MouseEvent e)
	{
		if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
			panning = false;
		else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && view.highlighted == null && view.knobFragment == null)
		{
			view.selected = null;
			view.repaint();
		}
	}

	List<Fragment> near = new ArrayList<Fragment>();
	@Override public void mouseMoved(MouseEvent e)
	{
		double viewRay = knobRay/view.scale;
		near.clear();
		view.nearFragments(view.toViewX(e.getX()), view.toViewY(e.getY()), viewRay, near);
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		
		Fragment closestKnobFragment = null;
		FragmentKnob closestKnob = null;
		double minDist2 = 0;
		for (int i=0;i<near.size();i++)
			for (int j=0;j<FragmentKnob.values().length;j++)
		{
			double dist2 = FragmentKnob.values()[j].dist2(mx, my, near.get(i));
			if (dist2 <= viewRay*viewRay && (closestKnobFragment == null || dist2 < minDist2))
			{
				closestKnobFragment = near.get(i);
				closestKnob = FragmentKnob.values()[j];
				minDist2 = dist2;
			}
		}
		if (closestKnobFragment != view.knobFragment || closestKnob != view.knob)
		{
			view.knobFragment = closestKnobFragment;
			view.knob = closestKnob;
			view.repaint();
		}
		
		Fragment closest = null;
		for (int i=0;i<near.size();i++)
			if (near.get(i).contains(mx, my))
		{
			double dist2 = near.get(i).dist2ToCenter(mx, my);
			if (closest == null || dist2 < minDist2)
			{
				closest = near.get(i);
				minDist2 = dist2;
			}
		}
		if (closest != view.highlighted)
		{
			view.highlighted = closest;
			view.repaint();
		}
	}
	@Override public void mouseDragged(MouseEvent e)
	{
		if (panning)
		{
			int dx = e.getX()-panDownX;
			int dy = e.getY()-panDownY;
			panDownX = e.getX();
			panDownY = e.getY();
			view.scrollPixels(dx, dy);
			view.repaint();
			return;
		}
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		if (view.knobFragment != null && (e.getModifiersEx() & (MouseEvent.BUTTON1_DOWN_MASK+MouseEvent.BUTTON3_DOWN_MASK)) != 0)
		{
			view.knob.onDrag(view.knobFragment, mx, my, (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0, e.isShiftDown(), e.isControlDown());
			view.repaint();
		}
		else if (view.highlighted != null && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
		{
			view.selected.setPos(mx+offsetx, my+offsety);
			view.repaint();
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
		view.repaint();
	}
	
	@Override public void mouseClicked(MouseEvent e)
	{
		
	}
	
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}
