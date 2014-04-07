package org.interreg.docexplore.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

public class MaskIcon implements Icon
{
	BufferedImage image;
	
	public MaskIcon(Icon icon, float resize)
	{
		image = new BufferedImage((int)(resize*icon.getIconWidth()), 
			(int)(resize*icon.getIconHeight()), BufferedImage.TYPE_INT_ARGB);
		
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, 0);
		
		Graphics2D g = image.createGraphics();
		g.scale((double)(image.getWidth())/icon.getIconWidth(), 
			(double)(image.getHeight())/icon.getIconHeight());
		icon.paintIcon(null, g, 0, 0);
	}
	
	public void setColor(Color color)
	{
		int col = color.getRGB();
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				if (image.getRGB(i, j) != 0) 
					image.setRGB(i, j, col);
	}
	
	public int getIconHeight() {return image.getWidth();}
	public int getIconWidth() {return image.getHeight();}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		g.drawImage(image, x, y, null);
	}

}
