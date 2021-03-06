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
