package org.interreg.docexplore.reader.util;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ImageStore
{
	static class Dimension implements Comparable<Dimension>
	{
		int width, height;
		
		public Dimension(int width, int height) {this.width = width; this.height = height;}
		
		public int compareTo(Dimension o) {return o.width != width ? o.width-width : o.height-height;}
		
	}
	
	Map<Dimension, List<BufferedImage>> images;
	
	public ImageStore()
	{
		this.images = new TreeMap<ImageStore.Dimension, List<BufferedImage>>();
	}
	
	public synchronized BufferedImage getImage(int width, int height)
	{
		List<BufferedImage> list = images.get(new Dimension(width, height));
		if (list == null || list.isEmpty())
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return list.remove(0);
	}
	public synchronized void freeImage(BufferedImage image)
	{
		Dimension dim = new Dimension(image.getWidth(), image.getHeight());
		List<BufferedImage> list = images.get(dim);
		if (list == null)
		{
			list = new LinkedList<BufferedImage>();
			images.put(dim, list);
		}
		list.add(image);
	}
}
