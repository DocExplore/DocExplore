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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class FreeShapeROIOperation implements ImageView.Operation<PageEditor>
{
	List<Point> current;
	Point next;
	boolean completed;
	
	public FreeShapeROIOperation()
	{
		this.current = new LinkedList<Point>();
		this.completed = false;
		this.next = new Point(-1, -1);
	}
	
	double distance2(PageEditor view, double vx, double vy, Point point)
	{
		double dx = (vx-point.x)*view.getScale();
		double dy = (vy-point.y)*view.getScale();
		return dx*dx+dy*dy;
	}
	
	static int completeRay = 5;
	
	public void pointClicked(PageEditor view, int cx, int cy, double vx, double vy, int modifiers, int clickCount)
	{
		if (current.size() > 2 && distance2(view, vx, vy, current.get(0)) < completeRay*completeRay)
		{
			view.getHost().getActionListener().onAddRegionRequest(view.page, current.toArray(new Point [0]));
			this.current = new LinkedList<Point>();
			next.setLocation(-1, -1);
		}
		else current.add(new Point(Math.max(0, Math.min(view.getImage().getWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImage().getHeight()-1, (int)(vy+.5)))));
		view.repaint();
	}
	
	public void pointDropped(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers) {pointClicked(view, cx, cy, vx, vy, modifiers, 1);}

	public boolean completed() {return completed;}
	
	public void pointHovered(PageEditor view, int cx, int cy, double vx, double vy, int modifiers)
	{
		next.setLocation(Math.max(0, Math.min(view.getImage().getWidth()-1, (int)(vx+.5))), Math.max(0, Math.min(view.getImage().getHeight()-1, (int)(vy+.5))));
		if (!current.isEmpty())
		{
			Point first = current.get(0);
			if (distance2(view, vx, vy, first) < completeRay*completeRay)
				{next.x = first.x; next.y = first.y;}
		}
		view.repaint();
	}
	
	final static Stroke crosshairStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);
	public void render(PageEditor view, Graphics2D g, double pixelSize)
	{
		if (next.x < 0)
			return;
		
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.blue);
		g.drawLine((int)bounds.getMinX(), next.y, (int)bounds.getMaxX(), next.y);
		g.drawLine(next.x, (int)bounds.getMinY(), next.x, (int)bounds.getMaxY());
		
		g.setColor(!current.isEmpty() && next != null && next.equals(current.get(0)) ? 
			RegionOverlay.regionFocusedOutlineColor : RegionOverlay.regionOutlineColor);
		
		Point last = null;
		for (Point point : current)
		{
			if (last != null)
				g.drawLine(last.x, last.y, point.x, point.y);
			last = point;
		}
		
		if (next.x >= 0 && last != null)
			g.drawLine(last.x, last.y, next.x, next.y);
	}
	
	public String getMessage() {return XMLResourceBundle.getBundledString("statusFreeMessage");}

	public void pointDragged(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers) {pointHovered(view, cx, cy, vx, vy, modifiers);}
	public void pointGrabbed(PageEditor view, int cx, int cy, double vx, double vy, int modifiers) {}
	public void contextMenuRequested(PageEditor view, int cx, int cy, double vx, double vy, int modifiers) {}
}
