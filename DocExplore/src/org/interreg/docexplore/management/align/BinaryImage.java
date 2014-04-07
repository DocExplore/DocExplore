package org.interreg.docexplore.management.align;
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
				data[i][j] = PixelUtils.gamma(image.getRGB(i, j)) < gammaThreshold*255;
	}
	
	public BufferedImage toImage()
	{
		BufferedImage image = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, data[i][j] ? Color.black.getRGB() : Color.white.getRGB());
		return image;
	}
	
	public int lineScore(int line)
	{
		int sum = 0;
		for (int i=0;i<data.length;i++)
			if (data[i][line])
				sum++;
		return sum;
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
