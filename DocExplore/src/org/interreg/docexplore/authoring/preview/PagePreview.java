/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.preview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

@SuppressWarnings("serial")
public class PagePreview extends ImagePreview
{
	static Stroke stroke = new BasicStroke(2);
	public BufferedImage buildPreview(Object object) throws Exception
	{
		Page page = (Page)object;
		BufferedImage image = page.getImage().getImage();
		BufferedImage scaled = super.buildPreview(image);
		
		Graphics2D g = scaled.createGraphics();
		int w = scaled.getWidth(), h = scaled.getHeight();
		g.drawImage(image, 0, 0, w, h, null);
		
		try
		{
			g.setStroke(stroke);
			Set<Region> regions = page.getRegions();
			for (Region region : regions)
				drawRegion(region, g, w, h, image.getWidth(), image.getHeight());
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		page.unloadImage();
		
		MetaDataKey key = page.getLink().getOrCreateKey("imported-from", "");
		List<MetaData> annotations = page.getMetaDataListForKey(key);
		if (annotations != null && !annotations.isEmpty())
		{
			g.setFont(g.getFont().deriveFont(16).deriveFont(Font.BOLD));
			String importedFrom = annotations.get(0).getString();
			Rectangle2D rec = g.getFontMetrics().getStringBounds(importedFrom, g);
			g.setColor(Color.gray);
			g.fillRect(0, 0, (int)rec.getWidth(), (int)rec.getHeight());
			g.setColor(Color.white);
			g.drawString(importedFrom, 0, (int)(-rec.getY()));
			page.unloadMetaData();
		}
		
		return scaled;
	}
	
	void drawRegion(Region region, Graphics2D g, int w, int h, int w0, int h0)
	{
		Point [] outline = region.getOutline();
		g.setColor(Color.red);
		Point last = outline[outline.length-1];
		for (int i=0;i<outline.length;i++)
		{
			g.drawLine(last.x*w/w0, last.y*h/h0, outline[i].x*w/w0, outline[i].y*h/h0);
			last = outline[i];
		}
	}
}
