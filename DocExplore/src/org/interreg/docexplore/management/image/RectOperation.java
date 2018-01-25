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

import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.internationalization.Lang;

public abstract class RectOperation implements ImageView.Operation<ImageView>
{
	public Point first, second, next;
	
	public RectOperation()
	{
		this.first = new Point(-1, -1);
		this.second = new Point(-1, -1);
		this.next = new Point(-1, -1);
	}
	
	public void pointClicked(ImageView view, int cx, int cy, double vx, double vy, int modifiers, int clickCount) {}

	public void pointHovered(ImageView view, int cx, int cy, double vx, double vy, int modifiers)
	{
		next.setLocation(Math.max(0, Math.min(view.getImageWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImageHeight()-1, (int)(vy+.5))));
		view.repaint();
	}
	
	public void pointGrabbed(ImageView view, int cx, int cy, double vx, double vy, int modifiers)
	{
		first.setLocation(Math.max(0, Math.min(view.getImageWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImageHeight()-1, (int)(vy+.5))));
		view.repaint();
	}
	public void pointDragged(ImageView view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers)
	{
		next.setLocation(Math.max(0, Math.min(view.getImageWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImageHeight()-1, (int)(vy+.5))));
		view.repaint();
	}
	public void pointDropped(ImageView view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers)
	{
		second.setLocation(Math.max(0, Math.min(view.getImageWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImageHeight()-1, (int)(vy+.5))));
		rectDrawn(view, first, second, modifiers);
		view.repaint();
	}
	public abstract void rectDrawn(ImageView view, Point first, Point second, int modifiers);
	
	public void render(ImageView ic, Graphics2D g, double pixelSize)
	{
		if (next.x < 0)
			return;
		
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.blue);
		g.drawLine((int)bounds.getMinX(), next.y, (int)bounds.getMaxX(), next.y);
		g.drawLine(next.x, (int)bounds.getMinY(), next.x, (int)bounds.getMaxY());
		
		if (first.x < 0)
			return;
		
		g.setColor(RegionOverlay.regionOutlineColor);
		Point p = second.x < 0 ? next : second;
		int x = Math.min(first.x, p.x), y = Math.min(first.y, p.y);
		int w = Math.abs(first.x-p.x), h = Math.abs(first.y-p.y);
		g.drawRect(x, y, w, h);
	}
	
	public String getMessage() {return Lang.s("statusFreeMessage");}

	public boolean completed() {return second.x >= 0;}

	public void contextMenuRequested(ImageView view, int cx, int cy, double vx, double vy, int modifiers) {}
}
