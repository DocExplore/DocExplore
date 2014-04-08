/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.align;
import java.awt.Color;
import java.awt.image.BufferedImage;


public class PixelUtils
{
	static int red(int rgb) {return (rgb >> 16) & 0x000000FF;}
	static int green(int rgb) {return (rgb >> 8 ) & 0x000000FF;}
	static int blue(int rgb) {return (rgb) & 0x000000FF;}
	static int rgb(int r, int g, int b) {return (r << 16)+(g << 8)+b;}
	static int gamma(int rgb) {return (red(rgb)+green(rgb)+blue(rgb))/3;}
	
	public static int diff(int rgb1, int rgb2)
	{
		int rd = Math.abs(red(rgb1)-red(rgb2));
		int gd = Math.abs(green(rgb1)-green(rgb2));
		int bd = Math.abs(blue(rgb1)-blue(rgb2));
		
		return Math.max(rd, Math.max(gd, bd));
	}
	
	public static int gradAt(BufferedImage image, int i, int j)
	{
		int mid = image.getRGB(i, j);
		int bottom = j == 0 ? mid : image.getRGB(i, j-1);
		int top = j == image.getHeight()-1 ? mid : image.getRGB(i, j+1);
		int left = i == 0 ? mid : image.getRGB(i-1, j);
		int right = i == image.getWidth()-1 ? mid : image.getRGB(i+1, j);
		
		return Math.max(diff(mid, bottom), Math.max(diff(mid, top), Math.max(diff(mid, left), diff(mid, right))));
	}
	
	public static int [][] buildGrad(BufferedImage input)
	{
		int [][] output = new int [input.getWidth()][input.getHeight()];
		for (int i=0;i<input.getWidth();i++)
			for (int j=0;j<input.getHeight();j++)
				output[i][j] = gradAt(input, i, j);
		return output;
	}
	
	public static float lineScore(int [][] grad, int line)
	{
		int sum = 0;
		for (int i=0;i<grad.length;i++)
			sum += grad[i][line];
		return sum*1f/grad.length/255f;
	}
	
	public static float [] blur(float [] scores, int ray)
	{
		float [] blurred = new float [scores.length];
		for (int i=0;i<scores.length;i++)
		{
			int nSamples = 0;
			float sum = 0;
			for (int j=i-ray;j<=i+ray;j++)
				if (j >= 0 && j < scores.length)
					{sum += scores[j]; nSamples++;}
			blurred[i] = sum/nSamples;
		}
		return blurred;
	}
	
	public static int moveToLowestScore(int start, float [] scores, int ray)
	{
		boolean moved = true;
		while (moved)
		{
			moved = false;
			int lower = start;
			for (int i=start-ray;i<=start+ray;i++)
				if (i>=0 && i<scores.length && scores[i] < scores[lower])
					{moved = true; lower = i;}
			start = lower;
		}
		return start;
	}
	
	public static void binarize(BufferedImage image)
	{
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, gamma(image.getRGB(i, j)) > 127 ? Color.white.getRGB() : Color.black.getRGB());
	}
	
	static int nNeighbors(BufferedImage image, int i, int j)
	{
		int res = 0;
		int col = image.getRGB(i, j);
		for (int di=-1;di<=1;di++)
			for (int dj=-1;dj<=1;dj++)
				if (di!=0 || dj!=0)
		{
			int x = i+di, y = j+dj;
			if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight())
				continue;
			if (image.getRGB(x, y) == col)
				res++;
		}
		return res;
	}
	
	public static void cleanBinaryImage(BufferedImage image)
	{
		boolean needsMore = true;
		while (needsMore)
		{
			needsMore = false;
			for (int i=0;i<image.getWidth();i++)
				for (int j=0;j<image.getHeight();j++)
					if (image.getRGB(i, j) == Color.black.getRGB() && nNeighbors(image, i, j) < 3)
			{
				image.setRGB(i, j, Color.white.getRGB());
				needsMore = true;
			}
		}
	}
}
