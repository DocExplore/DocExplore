/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher.fast;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Filter {
	
	private static final int MAX_RGB = 255;
	private static final int MIN_RGB = 0;
	
	private static int safe(int value)
	{
		return Math.max(MIN_RGB, Math.min(MAX_RGB, value));
	}
	
	private static int avg(int color)
	{
		float sum = 0f;

		for (int i = 0; i < 3; ++i) {
			sum += (color & (0xFF << (i * 8))) >> (i * 8);
		}
		
		int avg = safe((int) sum/3);
		return 0x00 | avg | avg << 8 | avg << 16 | color & (0xFF << 24);
	}
	
	public static BufferedImage grayScale(BufferedImage image) 
	{
		int w = image.getWidth();
		int h = image.getHeight();
		for (int x = 0; x < w; ++x) {
			for (int y = 0; y < h; ++y) {
				int avg = avg(image.getRGB(x, y));
				image.setRGB(x, y, avg);
			}
		}
		return image;
	}
	
	public static BufferedImage grayScaleGC(BufferedImage image)
	{
		BufferedImage gImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);  
		Graphics g = gImage.getGraphics();  
		g.drawImage(image, 0, 0, null);  
		g.dispose();  
		return gImage;
	}
	
}
