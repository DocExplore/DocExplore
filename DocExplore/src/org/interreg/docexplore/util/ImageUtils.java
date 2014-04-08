/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.util;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.imgscalr.Scalr;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.LocalizedContent;

import com.sixlegs.png.PngImage;
import com.sun.media.jai.codec.SeekableStream;

public class ImageUtils
{

	public static Cursor createCursorFromComponent(Component comp, String name)
	{
		BufferedImage image = new BufferedImage(comp.getWidth(), comp.getHeight(), 
			BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(0, 0, 0, 0));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		comp.paint(graphics);
		return createCursorFromImage(image, name);
	}

	public static Cursor createCursorFromIcon(Icon icon, String name)
	{
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), 
			BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(0, 0, 0, 0));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		icon.paintIcon(null, graphics, 0, 0);
		return createCursorFromImage(image, name);
	}

	public static Cursor createCursorFromImage(BufferedImage image, String name)
	{
		Dimension best = Toolkit.getDefaultToolkit().getBestCursorSize(
			image.getWidth(), image.getHeight());
		float ratio = 1f;
		if (best.getWidth() != image.getWidth())
			ratio = ((float)best.getWidth())/image.getWidth();
		if (best.getHeight()!=image.getHeight() && best.getHeight()/image.getHeight()<ratio)
			ratio = ((float)best.getHeight())/image.getHeight();
		Dimension res = new Dimension((int)(image.getWidth()*ratio), (int)(image.getHeight()*ratio));
		if (ratio != 1f)
		{
			BufferedImage newImage = new BufferedImage(res.width, res.height, image.getType());
			newImage.createGraphics().drawImage(image, 0, 0, res.width, res.height, null);
			image = newImage;
		}
		
		return Toolkit.getDefaultToolkit().createCustomCursor(image, 
			new Point(res.width/2, res.height/2), name);
	}

	public static BufferedImage resize(BufferedImage image, int width, int height)
	{
		/*BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = res.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);*/
		return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, width, height);
	}
	public static BufferedImage createIconSizeImage(BufferedImage image, float maxDim)
	{
		float dim = image.getWidth()>image.getHeight() ? image.getWidth() : image.getHeight();
		if (dim > maxDim)
			image = resize(image, (int)(image.getWidth()*maxDim/dim), (int)(image.getHeight()*maxDim/dim));
		return image;
	}
	public static Icon createIconFromImage(BufferedImage image, float maxDim)
	{
		return new ImageIcon(createIconSizeImage(image, maxDim));
	}
	
	public static Applet applet = null;
	public static Icon getIcon(String iconName)
	{
		if (applet == null)
			return new ImageIcon(
				ClassLoader.getSystemResource("org/interreg/docexplore/gui/icons/"+iconName));
		else try
		{
			URL url = Thread.currentThread().getContextClassLoader().getResource(
				"org/interreg/docexplore/gui/icons/"+iconName);
			Image image = applet.getImage(url);
			return new ImageIcon(image);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	
	public static BufferedImage getImageFromIcon(Icon icon)
	{
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		int val = new Color(0, 0, 0, 0).getRGB();
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, val);
		icon.paintIcon(null, image.createGraphics(), 0, 0);
		return image;
	}
	
	public static Icon getFlagIcon(Locale locale)
	{
		if (locale.getLanguage().equals("fr"))
			return getIcon("france-flag-48x48.png");
		if (locale.getLanguage().equals("en"))
			return getIcon("uk-flag-48x48.png");
		return null;
	}
	public static Icon getFlagIcon() {return getFlagIcon(LocalizedContent.getDefaultLocale());}

	public static double getResizeRatio(BufferedImage image, Dimension dim)
	{
		float xdim = ((float)dim.width)/image.getWidth();
		float ydim = ((float)dim.height)/image.getHeight();
		float minDim = Math.min(xdim, ydim);
		if (minDim < 1)
			return minDim;
		return 1d;
	}
	
	public static void clear(BufferedImage image, int x, int y, int w, int h, int col)
	{
		for (int i=x;i<x+w;i++)
			for (int j=y;j<y+h;j++)
				if (x>=0 && y>=0 && x<image.getWidth() && y<image.getHeight())
					image.setRGB(i, j, col);
	}
	
	public static Set<String> supportedFormats = new TreeSet<String>();
	static
	{
		supportedFormats.add("bmp");
		supportedFormats.add("fpx");
		supportedFormats.add("gif");
		supportedFormats.add("jpg");
		supportedFormats.add("jpeg");
		supportedFormats.add("png");
		supportedFormats.add("pnm");
		supportedFormats.add("pbm");
		supportedFormats.add("pgm");
		supportedFormats.add("ppm");
		supportedFormats.add("tif");
		supportedFormats.add("tiff");
		supportedFormats.add("wbmp");
	}
	public static String supportedFormatsString = null;
	static
	{
		for (String format : supportedFormats)
			supportedFormatsString += (supportedFormatsString != null ? ";" : "")+"*."+format;
	}
	public static boolean isSupported(URL url) {return isSupported(url.getPath());}
	public static boolean isSupported(String path)
	{
		int end = path.lastIndexOf('.');
		if (end < 0)
			return false;
		String ext = path.substring(end+1).toLowerCase();
		if (supportedFormats.contains(ext))
			return true;
		return ImageIO.getImageReadersBySuffix(ext).hasNext();
	}
	public static BufferedImage read(URL url) throws IOException
	{
//		if (!url.getPath().toLowerCase().endsWith(".png"))
		{
			RenderedImage ri = JAI.create("url", url);
			if (ri != null)
				return new BufferedImage(ri.getColorModel(), (WritableRaster)ri.getData(), true, null);
		}
		return ImageIO.read(url);
	}
	public static BufferedImage read(InputStream stream) throws IOException
	{
		if (!stream.markSupported())
			stream = new BufferedInputStream(stream);
		stream.mark(4);
		int [] header = {stream.read(), stream.read(), stream.read(), stream.read()};
		stream.reset();
		
		if (header[0] == 137 && header[1] == 80 && header[2] == 78 && header[3] == 71)
			return new PngImage().read(stream, true);
		
		RenderedImage ri = JAI.create("stream", SeekableStream.wrapInputStream(stream, true));
		BufferedImage image = null;
		if (ri != null)
			try {image = new BufferedImage(ri.getColorModel(), (WritableRaster)ri.getData(), true, null);}
			catch (Exception e) {}
		if (image == null)
			try {image = ImageIO.read(stream);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		try {stream.close();} catch (Exception e) {e.printStackTrace();}
		return image;
	}
	public static BufferedImage read(File file) throws IOException
	{
		if (file.getName().toLowerCase().endsWith(".png"))
			return new PngImage().read(file);
		RenderedImage ri = JAI.create("fileload", file.getAbsolutePath());
		if (ri != null)
			return new BufferedImage(ri.getColorModel(), (WritableRaster)ri.getData(), true, null);
		return ImageIO.read(file);
	}
	
	public static void write(BufferedImage image, String format, File file) throws IOException
	{
		ImageIO.write(image, format, file);
	}
	public static void write(BufferedImage image, String format, OutputStream stream) throws IOException
	{
		ImageIO.write(image, format, stream);
	}
	
	public static int alpha(int rgb) {return (rgb >>24) & 0x000000FF;}
	public static int red(int rgb) {return (rgb >> 16) & 0x000000FF;}
	public static int green(int rgb) {return (rgb >> 8 ) & 0x000000FF;}
	public static int blue(int rgb) {return (rgb) & 0x000000FF;}
	public static int rgb(int r, int g, int b) {return (r << 16)+(g << 8)+b;}
	public static int rgba(int r, int g, int b, int a) {return (a << 24)+(r << 16)+(g << 8)+b;}
}
