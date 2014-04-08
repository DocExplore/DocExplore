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
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.image.RegionOverlay.RegionObject;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.Pair;

public class SelectionOperation implements PageViewer.ImageOperation
{
	public SelectionOperation()
	{
	}
	
	final static int pointRay = 10;
	Pair<RegionObject, Integer> near = null;
	Point grabbed = null;
	int [][] axis = null;
	
	int [][] buildAxis(RegionObject region, int from)
	{
		Polygon poly = region.polygon;
		int prev = (from+poly.npoints-1)%poly.npoints;
		int prevprev = (from+poly.npoints-2)%poly.npoints;
		int next = (from+1)%poly.npoints;
		int nextnext = (from+2)%poly.npoints;
		return new int [][] {
			{poly.xpoints[prevprev], poly.ypoints[prevprev]}, 
			{poly.xpoints[prev], poly.ypoints[prev]},
			{poly.xpoints[from], poly.ypoints[from]},
			{poly.xpoints[next], poly.ypoints[next]},
			{poly.xpoints[nextnext], poly.ypoints[nextnext]}};
	}
	void applyAxis(PageViewer ic, RegionObject region, int from, int [][] axis)
	{
		Polygon poly = region.polygon;
		int prev = (from+poly.npoints-1)%poly.npoints;
		int next = (from+1)%poly.npoints;
		
		int v1x = axis[1][0]-axis[0][0], v1y = axis[1][1]-axis[0][1], v2x = axis[2][1]-axis[1][1], v2y = axis[1][0]-axis[2][0];
		float k = (poly.xpoints[from]*v2x+poly.ypoints[from]*v2y-axis[0][0]*v2x-axis[0][1]*v2y)*1f/(v2x*v1x+v2y*v1y);
		poly.xpoints[prev] = (int)(axis[0][0]+k*v1x);
		poly.ypoints[prev] = (int)(axis[0][1]+k*v1y);
		
		v1x = axis[3][0]-axis[4][0]; v1y = axis[3][1]-axis[4][1]; v2x = axis[2][1]-axis[3][1]; v2y = axis[3][0]-axis[2][0];
		k = (poly.xpoints[from]*v2x+poly.ypoints[from]*v2y-axis[4][0]*v2x-axis[4][1]*v2y)*1f/(v2x*v1x+v2y*v1y);
		poly.xpoints[next] = (int)(axis[4][0]+k*v1x);
		poly.ypoints[next] = (int)(axis[4][1]+k*v1y);
		
		constrain(ic, region, prev);
		constrain(ic, region, next);
	}
	void constrain(PageViewer ic, RegionObject region, int from)
	{
		Polygon poly = region.polygon;
		poly.xpoints[from] = Math.max(0, Math.min(ic.image.getWidth()-1, poly.xpoints[from]));
		poly.ypoints[from] = Math.max(0, Math.min(ic.image.getHeight()-1, poly.ypoints[from]));
	}
	
	public void pointClicked(PageViewer ic, Point point, int modifiers, int clickCount)
	{
		if ((modifiers & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK))
		{
			near = ic.overlay.pointAt(point, pointRay/ic.transform.getScaleX());
			if (near != null && near.first.polygon.npoints > 3)
			{
				Polygon poly = near.first.polygon;
				int [] xpoints = new int [poly.npoints-1], ypoints = new int [poly.npoints-1];
				for (int i=0;i<near.second;i++)
					{xpoints[i] = poly.xpoints[i]; ypoints[i] = poly.ypoints[i];}
				for (int i=near.second+1;i<poly.npoints;i++)
					{xpoints[i-1] = poly.xpoints[i]; ypoints[i-1] = poly.ypoints[i];}
				poly.npoints--;
				poly.xpoints = xpoints; poly.ypoints = ypoints;
				poly.invalidate();
				near.first.synchronizeOutline();
			}
			near = null;
		}
		else if ((modifiers & (InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK)) == (InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK))
		{
			near = ic.overlay.pointAt(point, pointRay/ic.transform.getScaleX());
			if (near != null)
			{
				Polygon poly = near.first.polygon;
				int prev = (near.second+poly.npoints-1)%poly.npoints;
				int next = (near.second+1)%poly.npoints;
				int x = poly.xpoints[near.second], y = poly.ypoints[near.second];
				int xt1 = poly.xpoints[prev], yt1 = poly.ypoints[next];
				int xt2 = poly.xpoints[next], yt2 = poly.ypoints[prev];
				if ((x-xt1)*(x-xt1)+(y-yt1)*(y-yt1) < (x-xt2)*(x-xt2)+(y-yt2)*(y-yt2))
					{poly.xpoints[near.second] = xt1; poly.ypoints[near.second] = yt1;}
				else {poly.xpoints[near.second] = xt2; poly.ypoints[near.second] = yt2;}
				constrain(ic, near.first, near.second);
				poly.invalidate();
				near.first.synchronizeOutline();
			}
			near = null;
		}
		else
		{
			List<Region> regions = ic.overlay.regionsAt(point);
			Region region = null;
			try
			{
				if (!regions.isEmpty())
				{
					AnnotatedObject obj = ic.document;
					if (obj instanceof Region && regions.contains(obj))
						region = regions.get((regions.indexOf(obj)+1)%regions.size());
					else region = regions.get(0);
					ic.notifyObjectSelected(region);
				}
				else ic.notifyObjectSelected(ic.getPage());
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			
			if (region != null && clickCount == 2)
				ic.notifyRegionAnnotationRequested(region);
		}
	}
	
	public void pointHovered(PageViewer ic, Point point, int modifiers)
	{
		Region region = ic.overlay.regionAt(point);
		ic.overlay.setHighlightedRegion(region);
		
		near = ic.overlay.pointAt(point, pointRay/ic.transform.getScaleX());
	}

	public void pointGrabbed(PageViewer ic, Point point, int modifiers)
	{
		axis = null;
		near = ic.overlay.pointAt(point, pointRay/ic.transform.getScaleX());
		if (near != null && (modifiers & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == InputEvent.SHIFT_DOWN_MASK)
		{
			Polygon poly = near.first.polygon;
			int [] xpoints = new int [poly.npoints+1], ypoints = new int [poly.npoints+1];
			for (int i=0;i<=near.second;i++)
				{xpoints[i] = poly.xpoints[i]; ypoints[i] = poly.ypoints[i];}
			xpoints[near.second+1] = xpoints[near.second]; ypoints[near.second+1] = ypoints[near.second];
			for (int i=near.second+1;i<poly.npoints;i++)
				{xpoints[i+1] = poly.xpoints[i]; ypoints[i+1] = poly.ypoints[i];}
			poly.npoints++;
			poly.xpoints = xpoints; poly.ypoints = ypoints;
			poly.invalidate();
			near.second = near.second+1;
		}
		else if (near == null)
			grabbed = new Point(point);
	}
	public void pointDragged(PageViewer ic, Point point, int modifiers)
	{
		if (near != null)
		{
			if ((modifiers & InputEvent.CTRL_DOWN_MASK) == 0)
				axis = null;
			else if (axis == null)
				axis = buildAxis(near.first, near.second);
			near.first.polygon.xpoints[near.second] = point.x;
			near.first.polygon.ypoints[near.second] = point.y;
			if (axis != null)
				applyAxis(ic, near.first, near.second, axis);
			near.first.polygon.invalidate();
		}
		else if (grabbed != null)
			ic.setImageAtDisplay(grabbed, ic.toDisplayCoordinates(point));
	}
	public void pointDropped(PageViewer ic, Point point, int modifiers)
	{
		if (near != null)
			near.first.synchronizeOutline();
		grabbed = null;
	}
	
	final static Color pointColor = new Color(64, 255, 64, 255);
	public void render(PageViewer ic, Graphics2D g)
	{
		if (near == null)
			return;
		
		Point point = new Point(near.first.polygon.xpoints[near.second], near.first.polygon.ypoints[near.second]);
		double w = pointRay/ic.transform.getScaleX(),  h = pointRay/ic.transform.getScaleY();
		Rectangle2D.Double rect = new Rectangle2D.Double(point.x-w/2, point.y-h/2, w, h);
		
		Stroke pointStroke = new BasicStroke((float)(1.5/ic.transform.getScaleX()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);
		g.setStroke(pointStroke);
		g.setColor(pointColor);
		g.draw(rect);
	}

	public boolean completed() {return false;}
	public String getMessage() {return "";}
}
