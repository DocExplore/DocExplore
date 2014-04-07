package org.interreg.docexplore.management.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class FreeShapeOperation implements PageViewer.ImageOperation
{
	List<Point> current;
	Point next;
	boolean completed;
	
	public FreeShapeOperation()
	{
		this.current = new LinkedList<Point>();
		this.completed = false;
		this.next = null;
	}
	
	static int completeRay = 5;
	
	public void pointClicked(PageViewer ic, Point point, int modifiers, int clickCount)
	{
		if (current.size() > 2 && ic.displayDistance2(point, current.get(0)) < completeRay*completeRay)
		{
			ic.addRegion(current.toArray(new Point [0]));
			this.current = new LinkedList<Point>();
			this.next = null;
		}
		else current.add(point);
	}
	
	public void pointDropped(PageViewer ic, Point point, int modifiers) {pointClicked(ic, point, modifiers, 1);}

	public boolean completed() {return completed;}
	
	public void pointHovered(PageViewer ic, Point point, int modifiers)
	{
		ic.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		this.next = point;
		
		if (!current.isEmpty())
		{
			Point first = current.get(0);
			if (ic.displayDistance2(first, next) < completeRay*completeRay)
				{next.x = first.x; next.y = first.y;}
		}
	}
	
	final static Stroke crosshairStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);
	public void render(PageViewer ic, Graphics2D g)
	{
		if (next == null)
			return;
		
		g.setStroke(crosshairStroke);
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.blue);
		g.drawLine((int)bounds.getMinX(), next.y, (int)bounds.getMaxX(), next.y);
		g.drawLine(next.x, (int)bounds.getMinY(), next.x, (int)bounds.getMaxY());
		
		g.setStroke(new BasicStroke((float)(3./g.getTransform().getScaleX()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(!current.isEmpty() && next != null && next.equals(current.get(0)) ? 
			RegionOverlay.regionFocusedOutlineColor : RegionOverlay.regionOutlineColor);
		
		Point last = null;
		for (Point point : current)
		{
			if (last != null)
				g.drawLine(last.x, last.y, point.x, point.y);
			last = point;
		}
		
		if (next != null && last != null)
			g.drawLine(last.x, last.y, next.x, next.y);
	}

	public void pointDragged(PageViewer ic, Point point, int modifiers) {pointHovered(ic, point, modifiers);}
	public void pointGrabbed(PageViewer ic, Point point, int modifiers) {}
	
	public String getMessage() {return XMLResourceBundle.getBundledString("statusFreeMessage");}
}
