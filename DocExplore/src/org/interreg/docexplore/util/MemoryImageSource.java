package org.interreg.docexplore.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import org.interreg.docexplore.gui.ErrorHandler;

public class MemoryImageSource extends ByteImageSource
{
	private static final long serialVersionUID = 3823602969707547623L;

	public MemoryImageSource(BufferedImage image)
	{
		super(toBytes(image));
	}
	
	static byte [] toBytes(BufferedImage image)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {ImageUtils.write(image, "PNG", out);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return out.toByteArray();
	}
}
