/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
