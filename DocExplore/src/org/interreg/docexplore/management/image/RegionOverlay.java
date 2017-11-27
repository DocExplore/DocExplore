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
import java.awt.Polygon;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.Pair;

public class RegionOverlay
{
	public static class RegionObject
	{
		public Region region;
		public Polygon polygon;
		
		public RegionObject(Region region)
		{
			this.region = region;
			Point [] points = region.getOutline();
			
			int [] xs = new int [points.length];
			int [] ys = new int [points.length];
			for (int i=0;i<points.length;i++)
			{xs[i] = points[i].x; ys[i] = points[i].y;}
			
			this.polygon = new Polygon(xs, ys, points.length);
		}
		
		public void synchronizeOutline()
		{
			Point [] outline = new Point [polygon.npoints];
			for (int i=0;i<polygon.npoints;i++)
				outline[i] = new Point(polygon.xpoints[i], polygon.ypoints[i]);
			
			try
			{
				region.setOutline(outline);
				polygon.invalidate();
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}
	}
	
	PageEditor editor;
	Page page;
	Set<RegionObject> objects;
	int focused, highlighted;
	
	public RegionOverlay(PageEditor editor)
	{
		this.editor = editor;
		this.objects = new HashSet<RegionOverlay.RegionObject>();
		this.focused = -1;
		this.highlighted = -1;
	}
	
	public void setPage(Page page, Region focused) throws DataLinkException {setPage(page, focused == null ? -1 : focused.getId(), false);}
	public void setPage(Page page, boolean refresh) throws DataLinkException {setPage(page, focused, refresh);}
	public void setPage(Page page, int focused, boolean refresh) throws DataLinkException
	{
		if (page == this.page && !refresh)
		{
			setFocusedRegion(focused);
			return;
		}
		
		objects.clear();
		this.highlighted = -1;
		boolean focusFound = false;
		for (Region region : page.getRegions())
		{
			objects.add(new RegionObject(region));
			focusFound |= region.getId() == focused;
		}
		this.focused = !focusFound ? -1 : focused;
		editor.repaint();
	}
	public void setFocusedRegion(int regionId)
	{
		if (this.focused == regionId)
			return;
		this.focused = regionId;
		editor.repaint();
	}
	public void setHighlightedRegion(Region region)
	{
		int id = region == null ? -1 : region.getId();
		if (this.highlighted == id)
			return;
		this.highlighted = id;
		editor.repaint();
	}
	
	final static Color regionOutlineColor = new Color(255, 127, 127, 192);
	final static Color regionFocusedOutlineColor = new Color(255, 255, 64, 255);
	final static Color regionHighlightedOutlineColor = new Color(255, 160, 64, 255);
	public void render(Graphics2D g, double pixelSize)
	{
		if (pixelSize == 0)
			return;
		for (RegionObject object : objects)
		{
			g.setColor(object.region.getId() == focused ? regionFocusedOutlineColor : 
				object.region.getId() == highlighted ? regionHighlightedOutlineColor :
				regionOutlineColor);
			g.drawPolygon(object.polygon);
		}
	}
	
	public Region regionAt(double x, double y)
	{
		for (RegionObject object : objects)
			if (object.polygon.contains(x, y))
				return object.region;
		return null;
	}
	public RegionObject regionObjectAt(double x, double y)
	{
		for (RegionObject object : objects)
			if (object.polygon.contains(x, y))
				return object;
		return null;
	}
	
	public List<Region> regionsAt(double x, double y)
	{
		List<Region> regions = new LinkedList<Region>();
		for (RegionObject object : objects)
			if (object.polygon.contains(x, y))
				regions.add(object.region);
		return regions;
	}
	
	public Pair<RegionObject, Integer> pointAt(double x, double y, double ray)
	{
		for (RegionObject object : objects)
			for (int i=0;i<object.polygon.npoints;i++)
			{
				int px = object.polygon.xpoints[i], py = object.polygon.ypoints[i];
				if ((x-px)*(x-px)+(y-py)*(y-py) <= ray*ray)
					return new Pair<RegionObject, Integer>(object, i);
			}
		return null;
	}
}
