/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
