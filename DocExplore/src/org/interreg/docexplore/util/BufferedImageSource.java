package org.interreg.docexplore.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public abstract class BufferedImageSource implements ImageSource
{
	private static final long serialVersionUID = -7172674019910216219L;
	
	protected BufferedImage buffer;
	
	public BufferedImageSource()
	{
		this.buffer = null;
	}
	
	public BufferedImage getImage() throws Exception
	{
		if (buffer == null)
		{
			InputStream in = getFile();
			try
			{
				buffer = ImageUtils.read(in);
				if (buffer == null)
					throw new NullPointerException("Unable to read image!");
				return buffer;}
			catch (Exception e)
			{
				if (in != null)
					try {in.close();}
					catch (Exception ex) {}
				throw e;
			}
		}
		else return buffer;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {this.buffer = null;}
}
