package org.interreg.docexplore.reader.gfx;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.util.ImageUtils;

public class Filler
{
	static Texture [] texes = null;
	
	public static Bindable getBindable()
	{
		if (texes == null)
			texes = new Texture [] {
				new Texture(getImage("filler0.png"), false),
				new Texture(getImage("filler1.png"), false),
				new Texture(getImage("filler2.png"), false),
				new Texture(getImage("filler3.png"), false),
				new Texture(getImage("filler4.png"), false),
				new Texture(getImage("filler5.png"), false),
				new Texture(getImage("filler6.png"), false),
				new Texture(getImage("filler7.png"), false)
			};
		return texes[(int)((System.currentTimeMillis()/100L)%texes.length)];
	}
	static BufferedImage getImage(String name)
	{
		try {return ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(Filler.class.getPackage().getName().replace('.', '/')+"/"+name));}
		catch (Exception e) {e.printStackTrace();}
		return null;
	}
}
