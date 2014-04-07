package org.interreg.docexplore.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.interreg.docexplore.gui.ErrorHandler;

public class ByteImageSource extends BufferedImageSource
{
	private static final long serialVersionUID = -2298493030430089277L;
	
	byte [] bytes;
	
	public ByteImageSource(byte [] bytes)
	{
		this.bytes = bytes;
	}
	public ByteImageSource(InputStream input)
	{
		try {this.bytes = ByteUtils.readStream(input);}
		catch (Exception e)
		{
			this.bytes = null;
			ErrorHandler.defaultHandler.submit(e, true);
		}
	}
	
	public InputStream getFile()
	{
		return new ByteArrayInputStream(bytes);
	}

	public String getURI()
	{
		return "byte buffer";
	}

	public boolean isValid()
	{
		return true;
	}

}
