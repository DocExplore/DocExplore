package org.interreg.docexplore.reader.book;

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
	
	public void drawROIMask(BufferedImage image, int inRGB, int outRGB)
	{
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
		{
			float x = i*1f/image.getWidth();
			float y = j*1f/image.getHeight();
			
			image.setRGB(i, j, shape.contains(x, y) ? inRGB : outRGB);
		}
	}
}
