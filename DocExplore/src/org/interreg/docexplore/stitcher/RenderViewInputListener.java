package org.interreg.docexplore.stitcher;

import java.awt.event.MouseEvent;

import org.interreg.docexplore.gui.image.NavViewInputListener;

public class RenderViewInputListener extends NavViewInputListener
{
	RenderView view;
	
	public RenderViewInputListener(RenderView view)
	{
		super(view);
		this.view = view;
	}
	
	int ray = 10;
	@Override public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		double l = ray/view.getScale();
		int hightlighted = 0;
		if (!view.editor.rendering)
		{
			if ((view.minx-mx)*(view.minx-mx) < l*l) hightlighted += 1;
			if ((view.miny-my)*(view.miny-my) < l*l) hightlighted += 2;
			if ((view.maxx-mx)*(view.maxx-mx) < l*l) hightlighted += 4;
			if ((view.maxy-my)*(view.maxy-my) < l*l) hightlighted += 8;
		}
		
		if (hightlighted != view.highlighted)
		{
			view.highlighted = hightlighted;
			view.repaint();
		}
	}
	
	@Override public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
		if (view.highlighted == -1 || view.editor.rendering)
			return;
		
		double mx = view.toViewX(e.getX()), my = view.toViewY(e.getY());
		if ((view.highlighted & 1) > 0) view.minx = Math.min(mx, view.maxx);
		else if ((view.highlighted & 4) > 0) view.maxx = Math.max(mx, view.minx);
		if ((view.highlighted & 2) > 0) view.miny = Math.min(my, view.maxy);
		else if ((view.highlighted & 8) > 0) view.maxy = Math.max(my, view.miny);
		view.editor.updateFields();
	}
}
