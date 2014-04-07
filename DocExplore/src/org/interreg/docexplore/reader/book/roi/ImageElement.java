package org.interreg.docexplore.reader.book.roi;

import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.gfx.Filler;
import org.interreg.docexplore.reader.gfx.StreamedTexture;

public class ImageElement implements OverlayElement
{
	ROIOverlay overlay;
	StreamedTexture texture;
	
	public ImageElement(ROIOverlay overlay, ROISpecification.ImageInfo image) throws Exception
	{
		this.overlay = overlay;
		this.texture = overlay.engine.app.client.getResource(StreamedTexture.class, image.getUri());
		if (texture == null)
			throw new NullPointerException();
	}
	
	public boolean bind()
	{
		if (texture == null || texture.texture == null)
			Filler.getBindable().bind();
		else texture.bind();
		return true;
	}

	public int getWidth(int maxWidth)
	{
		if (texture == null || texture.texture == null)
			return 0;
		if (texture.texture.width() > maxWidth)
			return maxWidth;
		int minDim = maxWidth/3;
		if (texture.texture.width() < minDim && texture.texture.height() < minDim)
			return minDim*texture.texture.width()/Math.max(texture.texture.width(), texture.texture.height());
		return texture.texture.width();
	}
	public int getHeight(int width)
	{
		if (texture == null || texture.texture == null)
			return 0;
		return texture.texture.height()*width/texture.texture.width();
	}
	
	public void dispose() {texture.release();}

	public boolean clicked(float x, float y) {return false;}
	
	public float marginFactor() {return 1;}
}
