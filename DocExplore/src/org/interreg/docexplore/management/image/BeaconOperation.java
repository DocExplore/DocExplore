package org.interreg.docexplore.management.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class BeaconOperation implements PageViewer.ImageOperation
{
	Point point;
	long start, elapsed, duration, period;
	
	public BeaconOperation(Point point, long duration, long period)
	{
		this.point = point;
		this.start = -1;
		this.elapsed = 0;
		this.duration = duration;
		this.period = period;
	}
	
	public boolean completed()
	{
		return elapsed > duration;
	}

	public void pointClicked(PageViewer ic, Point point, int modifiers, int clickCount) {}
	public void pointDragged(PageViewer ic, Point point, int modifiers) {}
	public void pointDropped(PageViewer ic, Point point, int modifiers) {}
	public void pointGrabbed(PageViewer ic, Point point, int modifiers) {}
	public void pointHovered(PageViewer ic, Point point, int modifiers) {}
	
	public void render(final PageViewer ic, Graphics2D g)
	{
		if (start < 0)
		{
			start = System.currentTimeMillis();
			ic.setImageAtDisplay(point, new Point(ic.getWidth()/2, ic.getHeight()/2));
		}
		else elapsed = System.currentTimeMillis()-start;
		if ((elapsed/period)%2 == 0)
		{
			g.setStroke(SquareOperation.crosshairStroke);
			Rectangle bounds = g.getClipBounds();
			g.setColor(Color.blue);
			g.drawLine((int)bounds.getMinX(), point.y, (int)bounds.getMaxX(), point.y);
			g.drawLine(point.x, (int)bounds.getMinY(), point.x, (int)bounds.getMaxY());
		}
		
		if (!completed())
			new Thread() {public void run() {
				try {Thread.sleep(period/2);}
				catch (Exception e) {}
				ic.repaint();
			}}.start();
		else ic.setOperation(PageViewer.defaultOperation);
	}
	
	public String getMessage() {return XMLResourceBundle.getBundledString("statusBeaconMessage");}
}
