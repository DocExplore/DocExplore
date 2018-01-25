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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.image.RegionOverlay.RegionObject;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.Pair;

public class SelectionOperation implements ImageView.Operation<PageEditor>
{
	public SelectionOperation()
	{
	}
	
	final static int pointRay = 15;
	Pair<RegionObject, Integer> near = null;
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
	void applyAxis(PageEditor view, RegionObject region, int from, int [][] axis)
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
		
		constrain(view, region, prev);
		constrain(view, region, next);
	}
	void constrain(PageEditor view, RegionObject region, int from)
	{
		Polygon poly = region.polygon;
		poly.xpoints[from] = Math.max(0, Math.min(view.getImageWidth()-1, poly.xpoints[from]));
		poly.ypoints[from] = Math.max(0, Math.min(view.getImageHeight()-1, poly.ypoints[from]));
	}
	
	public void pointClicked(PageEditor view, int cx, int cy, double vx, double vy, int modifiers, int clickCount)
	{
		if ((modifiers & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) == (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK))
		{
			near = view.regions.pointAt(vx, vy, pointRay/view.getScale());
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
				view.repaint();
			}
			near = null;
		}
		else if ((modifiers & (InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK)) == (InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK))
		{
			near = view.regions.pointAt(vx, vy, pointRay/view.getScale());
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
				constrain(view, near.first, near.second);
				poly.invalidate();
				near.first.synchronizeOutline();
				view.repaint();
			}
			near = null;
		}
		else
		{
			List<Region> regions = view.regions.regionsAt(vx, vy);
			Region region = null;
			try
			{
				if (!regions.isEmpty())
				{
					if (view.region != null && regions.contains(view.region))
						region = regions.get((regions.indexOf(view.region)+1)%regions.size());
					else region = regions.get(0);
					view.switchDocument(region);
				}
				else if (view.region != null)
					view.switchDocument(view.page);
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			
			if (region != null && clickCount == 2)
				view.getHost().onAddAnnotationRequest();
		}
	}
	
	public void pointHovered(PageEditor view, int cx, int cy, double vx, double vy, int modifiers)
	{
		Region region = view.regions.regionAt(vx, vy);
		view.regions.setHighlightedRegion(region);
		Pair<RegionObject, Integer> near = view.regions.pointAt(vx, vy, pointRay/view.getScale());
		if (near == null && this.near != null || near != null && this.near == null || near != this.near && (near.first != this.near.first || near.second != this.near.second))
			view.repaint();
		this.near = near;
	}

	public void pointGrabbed(PageEditor view, int cx, int cy, double vx, double vy, int modifiers)
	{
		axis = null;
		near = view.regions.pointAt(vx, vy, pointRay/view.getScale());
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
			view.repaint();
		}
	}
	public void pointDragged(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers)
	{
		if (near != null)
		{
			if ((modifiers & InputEvent.CTRL_DOWN_MASK) == 0)
				axis = null;
			else if (axis == null)
				axis = buildAxis(near.first, near.second);
			near.first.polygon.xpoints[near.second] = (int)(vx+.5);
			near.first.polygon.ypoints[near.second] = (int)(vy+.5);
			if (axis != null)
				applyAxis(view, near.first, near.second, axis);
			near.first.polygon.invalidate();
			view.repaint();
		}
		else
		{
			view.scrollPixels(deltax, deltay);
		}
	}
	public void pointDropped(PageEditor view, int cx, int cy, double vx, double vy, int downw, int downy, int deltax, int deltay, int modifiers)
	{
		if (near != null)
		{
			near.first.synchronizeOutline();
			view.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	public void contextMenuRequested(final PageEditor view, int cx, int cy, final double vx, final double vy, int modifiers)
	{
		JPopupMenu popup = new JPopupMenu();
		popup.add(new JMenuItem(new AbstractAction(Lang.s("imageLocateLabel")) {@Override public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				JDialog dialog = new JDialog(JOptionPane.getRootFrame(), true);
				String code = BookEditor.encode(view.page, (int)vx, (int)vy);
				JTextField field = new JTextField(code);
				field.setFont(Font.decode(null).deriveFont(24f).deriveFont(Font.BOLD));
				field.setEditable(false);
				field.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				dialog.add(field);
				dialog.pack();
				GuiUtils.centerOnComponent(dialog, view);
				dialog.setVisible(true);
			}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		}}));
		popup.show(view, cx, cy);
	}
	
	final static Color pointColor = new Color(64, 255, 64, 255);
	Rectangle2D.Double rect = new Rectangle2D.Double();
	public void render(PageEditor view, Graphics2D g, double pixelSize)
	{
		if (near == null)
			return;
		
		double w = pointRay/view.getScale(),  h = pointRay/view.getScale();
		rect.setFrame(near.first.polygon.xpoints[near.second]-w/2, near.first.polygon.ypoints[near.second]-h/2, w, h);
		g.setColor(pointColor);
		g.draw(rect);
	}

	public boolean completed() {return false;}
	public String getMessage() {return "";}
}
