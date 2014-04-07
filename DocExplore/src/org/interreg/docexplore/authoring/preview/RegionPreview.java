package org.interreg.docexplore.authoring.preview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.manuscript.Region;

@SuppressWarnings("serial")
public class RegionPreview extends PagePreview
{
	static Color maskColor = new Color(0, 0, 0, .5f);
	public BufferedImage buildPreview(Object object) throws Exception
	{
		Region region = (Region)object;
		BufferedImage image = region.getPage().getImage().getImage();
		BufferedImage res = super.buildPreview(region.getPage());
		
		Point [] outline = region.getOutline();
		Polygon shape = new Polygon();
		for (int i=0;i<outline.length;i++)
			shape.addPoint(outline[i].x*res.getWidth()/image.getWidth(), outline[i].y*res.getHeight()/image.getHeight());
		Area mask = new Area(new Rectangle(0, 0, res.getWidth(), res.getHeight()));
		mask.subtract(new Area(shape));
		
		Graphics2D g = res.createGraphics();
		g.setColor(maskColor);
		g.fill(mask);
		
		return res;
	}
}
