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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;


public class LineSeg
{
	static int red(int rgb) {return (rgb >> 16) & 0x000000FF;}
	static int green(int rgb) {return (rgb >> 8 ) & 0x000000FF;}
	static int blue(int rgb) {return (rgb) & 0x000000FF;}
	static int rgb(int r, int g, int b) {return (r << 16)+(g << 8)+b;}
	
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
	
	public static float columnScore(int [][] grad, int col)
	{
		int sum = 0;
		for (int i=0;i<grad[0].length;i++)
			sum += grad[col][i];
		return sum*1f/grad[0].length/255f;
	}
	
	public static float [] blurScores(float [] scores, int ray)
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
	
	public static BufferedImage [] getLines(BufferedImage image, int nLines)
	{
		int [][] grad = buildGrad(image);
		float [] scores = new float [grad[0].length];
		for (int i=0;i<scores.length;i++)
			scores[i] = lineScore(grad, i);
		scores = blurScores(scores, 10);
		
		int [] lineStart = new int [nLines];
		for (int i=0;i<nLines;i++)
			lineStart[i] = moveToLowestScore(i*image.getHeight()/nLines, scores, 5);
		
		BufferedImage [] res = new BufferedImage [nLines];
		for (int i=0;i<nLines;i++)
		{
			int y0 = lineStart[i];
			int y1 = i+1 < nLines ? lineStart[i+1] : image.getHeight();
			BufferedImage line = new BufferedImage(image.getWidth(), y1-y0, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = line.createGraphics();
			g.drawImage(image, 0, 0, image.getWidth(), y1-y0, 0, y0, image.getWidth(), y1, null);
			res[i] = line;
		}
		
		return res;
	}
	
	public static BufferedImage trim(BufferedImage image, float [] columnScores)
	{
		float [] blurred = blurScores(columnScores, 15);
		int start = 0;
		while (blurred[start] < .05f && start < image.getWidth())
			start++;
		int end = image.getWidth()-1;
		while (blurred[end] < .05f && end > start)
			end--;
		
		BufferedImage res = new BufferedImage(end-start, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		res.createGraphics().drawImage(image, 0, 0, res.getWidth(), res.getHeight(), start, 0, start+res.getWidth(), res.getHeight(), null);
		return res;
	}
	
	public static void main(String [] args) throws Exception
	{
		BufferedImage image = ImageUtils.read(new File("C:\\sci\\align\\roib.PNG"));
		TextMetrics text = new TextMetrics(StringUtils.readFile(new File("C:\\sci\\align\\trans.txt")));
		
		BufferedImage [] lines = getLines(image, text.lines.length);
		
		for (int i=0;i<lines.length;i++)
		{
			int [][] grad = buildGrad(lines[i]);
			float [] scores = new float [grad.length];
			for (int j=0;j<grad.length;j++)
				scores[j] = columnScore(grad, j);
			
			lines[i] = trim(lines[i], scores);
			grad = buildGrad(lines[i]);
			scores = new float [grad.length];
			for (int j=0;j<grad.length;j++)
				scores[j] = columnScore(grad, j);
			
			int blurRef = (int)(lines[i].getWidth()/(2*text.lines[i].line.length()));
			
			scores = blurScores(scores, (int)(blurRef));
			/*for (int j=0;j<grad.length;j++)
			{
				float score = (float)Math.sqrt(scores[j]);
				score = 1-score;
				scores[j] = score;
			}*/
			
			Graphics2D g = lines[i].createGraphics();
			/*for (int j=0;j<grad.length;j++)
			{
				g.setColor(new Color(scores[j], scores[j], scores[j], .5f));
				g.drawLine(j, 0, j, lines[i].getHeight()-1);
			}*/
			
			g.setColor(Color.red);
			int [] refPos = new int [text.lines[i].spaces.length];
			int [] pos = new int [text.lines[i].spaces.length];
			for (int j=0;j<refPos.length;j++)
			{
				refPos[j] = text.lines[i].spaces[j]*lines[i].getWidth()/text.lines[i].line.length();
				pos[j] = moveToLowestScore(refPos[j], scores, 2*blurRef);
			}
			
			for (int k=0;k<100;k++)
			{
				int [] newPos = new int [pos.length];
				for (int j=0;j<pos.length;j++)
				{
					int left = j == 0 ? refPos[j] : pos[j-1]+(refPos[j]-refPos[j-1]);
					int right = j == pos.length-1 ? refPos[j] : pos[j+1]-(refPos[j+1]-refPos[j]);
					newPos[j] = moveToLowestScore((int)(.1f*left+.1f*right+.8f*pos[j]), scores, 2*blurRef);
				}
				pos = newPos;
			}
			
			for (int j=0;j<refPos.length;j++)
			{
				g.drawLine(pos[j], 0, pos[j], lines[i].getHeight()-1);
			}
		}
		
		for (int i=0;i<lines.length;i++)
			ImageIO.write(lines[i], "PNG", new File("C:\\sci\\align\\line"+i+".png"));
	}
	
}
