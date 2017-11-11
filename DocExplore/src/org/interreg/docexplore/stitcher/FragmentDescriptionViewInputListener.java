package org.interreg.docexplore.stitcher;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class FragmentDescriptionViewInputListener extends NavViewInputListener
{
	public static final double poiRay = 2;
	
	FragmentDescriptionView view;
	
	
	public FragmentDescriptionViewInputListener(FragmentDescriptionView view)
	{
		super(view);
		
		this.view = view;
	}
	
	@Override public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
		{
			view.setSelected(view.highlighted);
		}
	}
	
	@Override public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
		
//		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0 && view.highlighted != null && view.highlighted == view.selected)
//		{
//			double x = view.toViewX(e.getX()), y = view.toViewY(e.getY());
//			view.desc.move(view.selected, x, y);
//			view.repaint();
//		}
	}
	
	@Override public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
		
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
		{
			if (view.highlighted != null && view.highlighted == view.selected)
			{
				view.desc.remove(view.selected);
				view.setHighlighted(null);
				view.setSelected(null);
			}
			else
			{
				double x = view.toViewX(e.getX()), y = view.toViewY(e.getY());
				if (x >= 0 && x <= view.desc.fragment.imagew && y >= 0 && y <= view.desc.fragment.imageh)
				{
					view.desc.add(x, y);
					view.repaint();
				}
			}
		}
	}
	
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		if (view.desc == null)
			return;
		
		double x = view.toViewX(e.getX()), y = view.toViewY(e.getY());
		double x0 = view.toViewX(e.getX()-poiRay)-1, y0 = view.toViewY(e.getY()-poiRay)-1;
		double x1 = view.toViewX(e.getX()+poiRay)+1, y1 = view.toViewY(e.getY()+poiRay)+1;
		int i0 = view.desc.binx(x0), j0 = view.desc.biny(y0), i1 = view.desc.binx(x1), j1 = view.desc.biny(y1);
		POI min = null;
		double minDist = 0;
		for (int i=i0;i<=i1;i++)
			for (int j=j0;j<=j1;j++)
				if (view.desc.bins[i][j] != null)
					for (int k=0;k<view.desc.bins[i][j].size();k++)
					{
						POI poi = view.desc.bins[i][j].get(k);
						double d = (x-poi.x)*(x-poi.x)+(y-poi.y)*(y-poi.y);
						if (min == null || d < minDist)
						{
							min = poi;
							minDist = d;
						}
					}
		if (min != null && minDist <= (poiRay/view.scale+1)*(poiRay/view.scale+1))
			view.setHighlighted(min);
		else view.setHighlighted(null);
	}
}
