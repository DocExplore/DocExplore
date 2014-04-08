/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
