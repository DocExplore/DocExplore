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

import org.interreg.docexplore.internationalization.Lang;

public class BeaconOperation implements PageEditor.Operation<PageEditor>
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

	public void pointClicked(PageEditor view, int cx, int cy, double vx, double vy, int modifiers, int clickCount) {}
	public void pointDragged(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers) {}
	public void pointDropped(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers) {}
	public void pointGrabbed(PageEditor view, int cx, int cy, double vx, double vy, int modifiers) {}
	public void pointHovered(PageEditor view, int cx, int cy, double vx, double vy, int modifiers) {}
	public void contextMenuRequested(PageEditor view, int cx, int cy, double vx, double vy, int modifiers) {}
	
	public void render(final PageEditor view, Graphics2D g, double pixelSize)
	{
		if (start < 0)
		{
			start = System.currentTimeMillis();
			view.setView(point.x, point.y, view.getScale());
		}
		else elapsed = System.currentTimeMillis()-start;
		if ((elapsed/period)%2 == 0)
		{
			Rectangle bounds = g.getClipBounds();
			g.setColor(Color.blue);
			g.drawLine((int)bounds.getMinX(), point.y, (int)bounds.getMaxX(), point.y);
			g.drawLine(point.x, (int)bounds.getMinY(), point.x, (int)bounds.getMaxY());
		}
		
		if (!completed())
			new Thread() {public void run() {
				try {Thread.sleep(period/2);}
				catch (Exception e) {}
				view.repaint();
			}}.start();
		else
		{
			view.cancelOperation();
			view.repaint();
		}
	}
	
	public String getMessage() {return Lang.s("statusBeaconMessage");}
}
