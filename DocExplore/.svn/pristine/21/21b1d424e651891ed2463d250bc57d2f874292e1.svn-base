package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import org.interreg.docexplore.manuscript.MetaData;

@SuppressWarnings("serial")
public class ImageElement extends InfoElement
{
	BufferedImage image;
	
	public ImageElement(MetaDataEditor editor, int width, MetaData md) throws Exception
	{
		super(editor, md);
		
		image = md.getImage();
		JLabel canvas = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			}
		};
		canvas.setPreferredSize(new Dimension(width, (image.getHeight()*width)/image.getWidth()));
		add(canvas);
	}
	
	public int getReaderWidth(int maxWidth)
	{
		if (image.getWidth() > maxWidth)
			return maxWidth;
		int minDim = maxWidth/3;
		if (image.getWidth() < minDim && image.getHeight() < minDim)
			return minDim*image.getWidth()/Math.max(image.getWidth(), image.getHeight());
		return image.getWidth();
	}
	public int getReaderHeight(int width)
	{
		return image.getHeight()*width/image.getWidth();
	}

	public BufferedImage getPreview(int width, Color back)
	{
		int iw = getReaderWidth(width);
		int ih = getReaderHeight(iw);
		BufferedImage res = new BufferedImage(iw, ih, BufferedImage.TYPE_3BYTE_BGR);
		res.createGraphics().drawImage(image, 0, 0, iw, ih, 0, 0, image.getWidth(), image.getHeight(), null);
		return res;
	}
}
