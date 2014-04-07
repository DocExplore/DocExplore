package org.interreg.docexplore.management.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class SquareOperation implements PageViewer.ImageOperation
{
	public Point first, second, next;
	
	public SquareOperation()
	{
		this.first = null;
		this.second = null;
	}
	
	public void pointClicked(PageViewer ic, Point point, int modifiers, int clickCount)
	{
//		if (second != null)
//			return;
//		if (first == null)
//			first = new Point(point);
//		else
//		{
//			second = new Point(point);
//			ic.addRegion(new Point [] {new Point(first), new Point(first.x, second.y), new Point(second), new Point(second.x, first.y)});
//			this.first = null;
//			this.second = null;
//		}
	}

	public void pointHovered(PageViewer ic, Point point, int modifiers)
	{
		ic.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		this.next = point;
	}
	
	public void pointGrabbed(PageViewer ic, Point point, int modifiers)
	{
		first = new Point(point);
	}
	public void pointDragged(PageViewer ic, Point point, int modifiers)
	{
		this.next = point;
	}
	public void pointDropped(PageViewer ic, Point point, int modifiers)
	{
		second = new Point(point);
		ic.addRegion(new Point [] {new Point(first), new Point(first.x, second.y), new Point(second), new Point(second.x, first.y)});
		this.first = null;
		this.second = null;
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
		
		if (first == null || second == null && next == null)
			return;
		
		g.setStroke(new BasicStroke((float)(3./g.getTransform().getScaleX()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(RegionOverlay.regionOutlineColor);
		Point p = second == null ? next : second;
		int x = Math.min(first.x, p.x), y = Math.min(first.y, p.y);
		int w = Math.abs(first.x-p.x), h = Math.abs(first.y-p.y);
		g.drawRect(x, y, w, h);
	}

	public boolean completed() {return second != null;}

	public String getMessage() {return XMLResourceBundle.getBundledString("statusSquareMessage");}
}
