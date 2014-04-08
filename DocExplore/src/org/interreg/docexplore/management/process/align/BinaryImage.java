/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.process.align;
import java.awt.Color;
import java.awt.image.BufferedImage;


public class BinaryImage
{
	boolean [][] data;
	
	public BinaryImage(int w, int h)
	{
		this.data = new boolean [w][h];
	}
	
	public BinaryImage(BufferedImage image, float gammaThreshold)
	{
		this.data = new boolean [image.getWidth()][image.getHeight()];
		
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				data[i][j] = ImageUtils.gamma(image.getRGB(i, j)) < gammaThreshold*255;
	}
	
	public BufferedImage toImage()
	{
		BufferedImage image = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, data[i][j] ? Color.black.getRGB() : Color.white.getRGB());
		return image;
	}
	
	int nNeighbors(int i, int j)
	{
		int res = 0;
		boolean val = data[i][j];
		for (int di=-1;di<=1;di++)
			for (int dj=-1;dj<=1;dj++)
				if (di!=0 || dj!=0)
		{
			int x = i+di, y = j+dj;
			if (x < 0 || x >= data.length || y < 0 || y >= data[0].length)
				continue;
			if (data[x][y] == val)
				res++;
		}
		return res;
	}
	
	public void clean(int neighborThreshold, boolean dir)
	{
		boolean needsMore = true;
		while (needsMore)
		{
			needsMore = false;
			for (int i=0;i<data.length;i++)
				for (int j=0;j<data[0].length;j++)
					if (data[i][j] == dir && nNeighbors(i, j) < neighborThreshold)
			{
				data[i][j] = !dir;
				needsMore = true;
			}
		}
	}
	
	public BinaryImage dilate(boolean dir)
	{
		BinaryImage res = new BinaryImage(data.length, data[0].length);
		for (int i=0;i<data.length;i++)
			for (int j=0;j<data[0].length;j++)
				if (data[i][j] == dir)
				{
					for (int di=-1;di<=1;di++)
						for (int dj=-1;dj<=1;dj++)
							if (i+di >= 0 && i+di < data.length && j+dj >= 0 && j+dj < data[0].length)
								res.data[i+di][j+dj] = dir;
				}
				else res.data[i][j] = !dir;
		return res;
					
	}
}
