/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.book;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import org.interreg.docexplore.internationalization.LocalizedContent;
import org.interreg.docexplore.reader.book.roi.Shape;

public class ROISpecification
{
	public static interface InfoElement {}
	public static interface TextInfo extends InfoElement
	{
		public String getText();
	}
	public static interface ImageInfo extends InfoElement
	{
		public String getUri();
		public int getWidth();
		public int getHeight();
	}
	
	public Shape shape;
	public LocalizedContent<List<InfoElement>> contents;
	public PageSpecification page = null;

	public ROISpecification(Shape shape)
	{
		this.shape = shape;
		this.contents = new LocalizedContent<List<InfoElement>>();
	}
	
	public void addText(String lang, final String text)
	{
		List<InfoElement> langContents = contents.getContent(lang);
		if (langContents == null)
		{
			langContents = new Vector<InfoElement>();
			contents.addContent(lang, langContents);
		}
		langContents.add(new TextInfo() {public String getText() {return text;}});
	}
	public void addImage(String lang, final String uri, final int width, final int height)
	{
		List<InfoElement> langContents = contents.getContent(lang);
		if (langContents == null)
		{
			langContents = new Vector<InfoElement>();
			contents.addContent(lang, langContents);
		}
		langContents.add(new ImageInfo() 
		{
			public String getUri() {return uri;}
			public int getWidth() {return width;}
			public int getHeight() {return height;}
		});
	}
	public void addCustom(String lang, InfoElement element)
	{
		List<InfoElement> langContents = contents.getContent(lang);
		if (langContents == null)
		{
			langContents = new Vector<InfoElement>();
			contents.addContent(lang, langContents);
		}
		langContents.add(element);
	}
	
	public boolean isLeft() {return page.pageNum%2 == 1;}
	public float [] center()
	{
		return new float [] {.5f*(shape.minx+shape.maxx), .5f*(shape.miny+shape.maxy)};
	}
	
	public void drawReverseMask(BufferedImage image)
	{
//		for (int i=0;i<image.getWidth();i++)
//			for (int j=0;j<image.getHeight();j++)
//		{
//			float x = i*1f/image.getWidth();
//			float y = j*1f/image.getHeight();
//			
//			image.setRGB(i, j, shape.contains(x, y) ? inRGB : outRGB);
//		}
		
		int w = image.getWidth(), h = image.getHeight();
		Graphics2D g = image.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.setColor(Color.black);
		g.fillRect(0, 0, w, h);
		
		g.setComposite(AlphaComposite.Src);
		//g.setComposite(AlphaComposite.Clear);
		float [][][] triangles = shape.outerTriangulation;
		for (int i=0;i<triangles.length;i++)
			g.fillPolygon(
				new int [] {(int)(w*triangles[i][0][0]), (int)(w*triangles[i][1][0]), (int)(w*triangles[i][2][0])}, 
				new int [] {(int)(h*triangles[i][0][1]), (int)(h*triangles[i][1][1]), (int)(h*triangles[i][2][1])}, 
				3);
	}
}
