package org.interreg.docexplore.stitcher;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class FragmentViewInputListener extends NavViewInputListener implements KeyListener
{
	public static final double knobRay = 5;
	
	FragmentView view;
	
	public FragmentViewInputListener(FragmentView view)
	{
		super(view);
		this.view = view;
	}
	
	double offsetx = 0, offsety = 0;
	@Override public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		if (((e.getModifiers() & (InputEvent.BUTTON1_MASK+InputEvent.BUTTON3_MASK)) != 0) && view.knobFragment != null)
		{
			view.knob.onGrab(view.knobFragment, mx, my);
			view.knobFragment.alpha = .6f;
			view.repaint();
		}
		else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && view.highlighted != null)
		{
			view.selected = view.highlighted;
			view.stitcher.fragmentSet.moveToLast(view.selected);
			offsetx = view.selected.uix-view.toViewX(e.getX());
			offsety = view.selected.uiy-view.toViewY(e.getY());
			view.selected.alpha = .6f;
			view.repaint();
		}
	}
	@Override public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && view.highlighted == null && view.knobFragment == null)
			view.selected = null;
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0 
			&& view.highlighted != null && view.selected != null && view.highlighted != view.selected && view.knobFragment == null)
				view.stitcher.editStitches(view.selected, view.highlighted);
//		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0 
//			&& view.highlighted != null && view.selected != null && view.highlighted != view.selected && view.knobFragment == null)
//		{
//			FragmentAssociation fa = null;
//			List<FragmentAssociation> list = view.stitcher.fragmentSet.associationsByFragment.get(view.selected);
//			if (list != null)
//				for (int i=0;i<list.size();i++)
//					if (list.get(i).other(view.selected) == view.highlighted)
//						{fa = list.get(i); break;}
//			if (fa != null && fa.transform != null)
//			{
//				if (view.selected == fa.d1.fragment)
//					fa.transform.transform(view.selected, view.highlighted);
//				else fa.transform.itransform(view.selected, view.highlighted);
//			}
//		}
		if (view.selected != null)
			view.selected.alpha = 1;
		if (view.knobFragment != null)
			view.knobFragment.alpha = 1;
		view.repaint();
	}

	List<Fragment> near = new ArrayList<Fragment>();
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		
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
		super.mouseDragged(e);
		
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
	
	@Override public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE && view.selected != null)
			view.toggleFull(view.selected);
		else if (e.getKeyCode() == KeyEvent.VK_DELETE && view.selected != null)
		{
			view.stitcher.remove(view.selected);
			view.selected = null;
			view.repaint();
		}
	}
	
	@Override public void keyTyped(KeyEvent e) {}
	@Override public void keyPressed(KeyEvent e) {}
}
