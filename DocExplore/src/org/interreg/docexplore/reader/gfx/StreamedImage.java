package org.interreg.docexplore.reader.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.StreamedResource;
import org.interreg.docexplore.util.ImageUtils;


public class StreamedImage extends StreamedResource implements IIOReadUpdateListener
{
	public BufferedImage image;
	
	protected StreamedImage(ReaderClient client, final String uri, File file)
	{
		super(client, uri, file);
		this.image = null;
	}
	
	public void handle(InputStream stream) throws Exception
	{
//		final ImageReader reader = ImageIO.getImageReadersBySuffix(uri.substring(uri.lastIndexOf(".")+1)).next();
//		reader.addIIOReadUpdateListener(this);
//		reader.setInput(new MemoryCacheImageInputStream(stream));
//		reader.read(0);
		
		if (file == null)
			this.image = ImageUtils.read(stream);
		else if (!file.exists())
			throw new FileNotFoundException(file.getCanonicalPath());
		else this.image = ImageUtils.read(file);
	}
	
	public void passStarted(ImageReader source, BufferedImage theImage,
		int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands)
	{
		this.image = theImage;
	}
	public void imageUpdate(ImageReader source, BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {}
	public void passComplete(ImageReader source, BufferedImage theImage) {}
	public void thumbnailPassComplete(ImageReader source, BufferedImage theThumbnail) {}
	public void thumbnailPassStarted(ImageReader source, BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {}
	public void thumbnailUpdate(ImageReader source, BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {}
	
	public static Allocator<StreamedImage> allocator = new Allocator<StreamedImage>()
	{
		public StreamedImage cast(StreamedResource stream) {return (StreamedImage)stream;}
		public StreamedImage allocate(ReaderClient client, String uri, File file) {return new StreamedImage(client, uri, file);}
	};
}
