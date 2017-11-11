package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class FragmentDescriptionView extends NavView
{
	StitchEditor editor;
	FragmentDescription desc = null;
	POI highlighted = null, selected = null;
	
	public FragmentDescriptionView(StitchEditor editor)
	{
		super();
		this.editor = editor;
		this.scale = 1;
		this.defaultStrokeWidth = 2;
	}
	@Override protected NavViewInputListener createInputListener() {return new FragmentDescriptionViewInputListener(this);}
	
	@Override protected void onViewChange()
	{
		editor.repaint();
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
		if (desc != null)
			fitView(0, 0, desc.image.getWidth(), desc.image.getHeight(), .1);
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
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(desc.fragment.file.getName(), g);
			double sh = g.getFont().getSize2D();
			g.setColor(new Color(0f, 0f, 0f, .5f));
			double x0 = toViewX(0), y0 = toViewY(0)+sh;
			g.fill(new Rectangle2D.Double(x0, y0-sh, bounds.getWidth(), 1.5*sh));
			g.setColor(Color.white);
			g.drawString(desc.fragment.file.getName(), (float)x0, (float)y0);
			
			g.drawImage(desc.image, 0, 0, null);
			
			for (int i=0;i<desc.features.size();i++)
			{
				POI poi = desc.features.get(i);
				g.setColor(poi.descriptor.length == 0 ? Color.blue : desc.fa.associationsByPOI.get(poi) == null ? loneSurfCol : Color.red);
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
			
			g.setColor(rectCol);
			double rx = desc.fragment.fromLocalToImageX(desc.rect.getX());
			double ry = desc.fragment.fromLocalToImageY(desc.rect.getY());
			rect.setRect(rx, ry, desc.fragment.fromLocalToImageX(desc.rect.getX()+desc.rect.getWidth())-rx, desc.fragment.fromLocalToImageY(desc.rect.getY()+desc.rect.getHeight())-ry);
			g.draw(rect);
			
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
			
			if (desc.fa.distortion != null)
			{
				g.setColor(distortionCol);
				double step = Math.min(desc.fragment.imagew, desc.fragment.imageh)/(scale*40);
				for (double x=toViewX(0);x<toViewX(getWidth());x+=step)
					for (double y=toViewY(0);y<toViewY(getHeight());y+=step)
						if (x >= 0 && y >= 0 && x < desc.fragment.imagew && y < desc.fragment.imageh && desc.distortionFactor(x, y) >= 0)
				{
					if (!editor.showAlpha)
					{
						line.setLine(x, y, desc.getDistortedImageX(x, y), desc.getDistortedImageY(x, y));
						g.draw(line);
					}
					else
					{
						double a = desc.distortionAlpha(x, y);
						line.setLine(x-a, y-a, x+a, y+a);
						g.draw(line);
					}
				}
			}
		}
	}
}
