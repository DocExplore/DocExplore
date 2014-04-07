package org.interreg.docexplore.reader.book;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import org.interreg.docexplore.internationalization.LocalizedContent;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.reader.gfx.StreamedTexture;

public class PageSpecification
{
	ReaderApp app;
	LocalizedContent<String> imageUri, transImageUri;
	LocalizedContent<String> soundUri;
	StreamedTexture texture, transTexture;
	public List<ROISpecification> regions;
	public int pageNum;
	
	public PageSpecification(ReaderApp app, int pageNum, LocalizedContent<String> imageUri, LocalizedContent<String> transImageUri, LocalizedContent<String> soundUri, 
		List<ROISpecification> regions)
	{
		this.app = app;
		this.pageNum = pageNum;
		this.imageUri = imageUri;
		this.transImageUri = transImageUri == null || transImageUri.content.isEmpty() ? null : transImageUri;
		this.soundUri = soundUri;
		this.regions = regions;
		this.texture = null;
	}
	public PageSpecification(ReaderApp app, int pageNum, LocalizedContent<String> imageUri, LocalizedContent<String> transImageUri, LocalizedContent<String> soundUri)
	{
		this(app, pageNum, imageUri, transImageUri, soundUri, new Vector<ROISpecification>());
	}
	
	public void addRegion(ROISpecification region)
	{
		region.page = this;
		regions.add(region);
	}
	
	public boolean isLoaded()
	{
		StreamedTexture texture = this.texture;
		if (texture == null || !texture.isComplete())
			return false;
		if (transImageUri != null)
		{
			texture = this.transTexture;
			if (texture == null || !texture.isComplete())
				return false;
		}
		return true;
	}
	public Bindable getTexture()
	{
		if (texture == null)
			texture = app.client.getResource(StreamedTexture.class, imageUri.getContent(""));
		if (transTexture == null && transImageUri != null)
			transTexture = app.client.getResource(StreamedTexture.class, transImageUri.getContent(""));
		return texture;
	}
	public Bindable getTransTexture()
	{
		if (texture == null)
			texture = app.client.getResource(StreamedTexture.class, imageUri.getContent(""));
		if (transTexture == null && transImageUri != null)
			transTexture = app.client.getResource(StreamedTexture.class, transImageUri.getContent(""));
		return transTexture == null ? texture : transTexture;
	}
	
	public void release()
	{
		if (texture != null)
		{
			texture.release();
			texture = null;
		}
		if (transTexture != null)
		{
			transTexture.release();
			transTexture = null;
		}
	}
	
	static int inRGB = 0xffffffff;
	static int outRGB = 0x00000000;
	public void drawROIMask(BufferedImage image, int inRGB, int outRGB)
	{
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
		{
			float x = i*1f/image.getWidth();
			float y = j*1f/image.getHeight();
			
			boolean in = false;
			for (ROISpecification region : regions)
				if (region.shape.contains(x, y))
					{in = true; break;}
			
			image.setRGB(i, j, in ? inRGB : outRGB);
		}
	}
}
