/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.SwingUtilities;

import org.interreg.docexplore.gui.image.NavView;
import org.interreg.docexplore.gui.image.NavViewInputListener;

@SuppressWarnings("serial")
public class FragmentDescriptionView extends NavView
{
	StitchEditor editor;
	FragmentDescription desc = null;
	POI highlighted = null, selected = null;
	FragmentDistortion distortion = null;
	
	public FragmentDescriptionView(StitchEditor editor)
	{
		super();
		this.editor = editor;
		this.defaultStrokeWidth = 2;
		addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {if (scale == 0) fit();}});
	}
	@Override protected NavViewInputListener createInputListener() {return new FragmentDescriptionViewInputListener(this);}
	
	@Override protected void onViewChange()
	{
		editor.repaint();
	}
	
	public void fit()
	{
		if (desc != null) 
			fitView(0, 0, desc.image.getWidth(), desc.image.getHeight(), .1);
	}
	
	void setSelected(POI poi)
	{
		if (selected != poi)
		{
			if (poi != null)
			{
				List<Association> associations = desc.fa.associationsByPOI.get(poi);
				if (associations != null && associations.size() == 1)
					editor.otherView(this).selected = associations.get(0).other(poi);
			}
			else if (selected != null)
			{
				List<Association> associations = desc.fa.associationsByPOI.get(selected);
				if (associations != null && associations.size() == 1 && editor.otherView(this).selected == associations.get(0).other(selected))
					editor.otherView(this).selected = null;
			}
			selected = poi;
			editor.repaint();
		}
	}
	void setHighlighted(POI poi)
	{
		if (highlighted != poi)
		{
			highlighted = poi;
			editor.repaint();
		}
	}
	
	public void setImageDescription(FragmentDescription desc)
	{
		this.desc = desc;
		this.highlighted = null;
		this.selected = null;
		this.distortion = null;
		if (desc != null)
		{
			distortion = new FragmentDistortion(editor.view.set, desc.fragment, null);
			System.out.println(distortion.stitches);
			fit();
		}
		else repaint();
	}
	
	Rectangle2D.Double rect = new Rectangle2D.Double();
	Ellipse2D.Double oval = new Ellipse2D.Double();
	double ovalr = 1.5;
	Line2D.Double line = new Line2D.Double();
	Point point = new Point();
	Color loneSurfCol = new Color(.5f, 0, 0, 1f), rectCol = new Color(1f, 0, 0, .25f), associationCol = new Color(1f, 0, 1f, .5f), distortionCol = new Color(0f, 0f, .7f, .5f);
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		if (desc != null)
		{
			Font font = g.getFont();
			g.setFont(font.deriveFont((float)(16f/pixelSize)));
			String name = Fragment.getName(desc.fragment.file);
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(name, g);
			double sh = g.getFont().getSize2D();
			g.setColor(new Color(0f, 0f, 0f, .5f));
			double x0 = toViewX(0), y0 = toViewY(0)+sh;
			g.fill(new Rectangle2D.Double(x0, y0-sh, bounds.getWidth(), 1.5*sh));
			g.setColor(Color.white);
			g.drawString(name, (float)x0, (float)y0);
			
			g.drawImage(desc.image, 0, 0, null);
			
			for (int i=0;i<desc.features.size();i++)
			{
				POI poi = desc.features.get(i);
				g.setColor(poi.descriptor == null ? Color.blue : desc.fa.associationsByPOI.get(poi) == null ? loneSurfCol : Color.red);
				rect.setRect(poi.x-1, poi.y-1, 2, 2);
				g.draw(rect);
				if (poi == highlighted || poi == selected)
				{
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					oval.setFrame(poi.x-ovalr, poi.y-ovalr, 2*ovalr, 2*ovalr);
					g.setColor(poi == selected ? Color.yellow : Color.orange);
					g.draw(oval);
				}
			}
			
			if (editor.showAssociations)
			{
				FragmentDescriptionView opp = editor.left == this ? editor.right : editor.left;
				for (int i=0;i<desc.features.size();i++)
				{
					POI poi = desc.features.get(i);
					double sx = fromViewX(poi.x), sy = fromViewY(poi.y);
					if (sx < 0 || sy < 0 || sx > getWidth() || sy > getHeight())
						continue;
					List<Association> list = editor.map.associationsByPOI.get(poi);
					if (list != null)
						for (int j=0;j<list.size();j++)
					{
						Association a = list.get(j);
						POI end = a.other(poi);
						g.setColor(poi == selected || end == opp.selected ? Color.yellow : poi == highlighted || end == opp.highlighted ? Color.orange : associationCol);
						point.x = (int)opp.fromViewX(end.x);
						point.y = (int)opp.fromViewY(end.y);
						Point conv = SwingUtilities.convertPoint(opp, point, this);
						line.setLine(poi.x, poi.y, toViewX(conv.getX()), toViewY(conv.getY()));
						g.draw(line);
					}
				}
			}
			else
			{
				for (int i=0;i<desc.features.size();i++)
				{
					POI poi = desc.features.get(i);
					double sx = fromViewX(poi.x), sy = fromViewY(poi.y);
					if (sx < 0 || sy < 0 || sx > getWidth() || sy > getHeight())
						continue;
					List<Association> list = editor.map.associationsByPOI.get(poi);
					if (list != null)
						for (int j=0;j<list.size();j++)
					{
						Association a = list.get(j);
						POI end = a.other(poi);
						double lx = end.fragment.fromImageToLocalX(end.x), ly = end.fragment.fromImageToLocalY(end.y);
						double wx = end.fragment.fromLocalX(lx, ly), wy = end.fragment.fromLocalY(lx, ly);
						lx = poi.fragment.toLocalX(wx, wy); ly = poi.fragment.toLocalY(wx, wy);
						double dx = poi.fragment.fromLocalToImageX(lx)-poi.x, dy = poi.fragment.fromLocalToImageY(ly)-poi.y;
						g.setColor(associationCol);
						line.setLine(poi.x, poi.y, poi.x+.5*dx, poi.y+.5*dy);
						g.draw(line);
					}
				}
			}
			
			if (distortion != null)
			{
				double step = Math.min(desc.fragment.imagew, desc.fragment.imageh)/(getScale()*40);
				for (double x=toViewX(0);x<toViewX(getWidth());x+=step)
					for (double y=toViewY(0);y<toViewY(getHeight());y+=step)
						if (x >= 0 && y >= 0 && x < desc.fragment.imagew && y < desc.fragment.imageh)
				{
					if (!editor.showAlpha)
					{
						g.setColor(distortionCol);
						line.setLine(x, y, x+distortion.getDist(x, y, 0), y+distortion.getDist(x, y, 1));
						g.draw(line);
						g.setColor(associationCol);
						line.setLine(x, y, x, y);
						g.draw(line);
					}
					else
					{
//						double a = desc.distortionAlpha(x, y);
//						line.setLine(x-a, y-a, x+a, y+a);
//						g.draw(line);
					}
				}
				if (distortion.stitches != null)
				{
					g.setColor(Color.green);
					for (FragmentDistortion.Stitch s : distortion.stitches)
					{
						line.setLine(s.x, s.y, s.x+s.dx, s.y+s.dy);
						g.draw(line);
					}
				}
			}
		}
	}
}
