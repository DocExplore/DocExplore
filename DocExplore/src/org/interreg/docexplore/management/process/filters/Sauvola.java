/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.process.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.management.process.Filter;
import org.interreg.docexplore.management.process.FilterParameter;
import org.interreg.docexplore.management.process.FloatParameter;

public class Sauvola implements Filter
{
	public String getName() {return "Sauvola binarization";}

	FilterParameter [] params = {
		new FloatParameter("Window size", 1, 200, 40),
		new FloatParameter("Standard deviation", 1, 512, 128),
		new FloatParameter("Threshold", .01, .99, .3)
	};
	public FilterParameter [] getParameters() {return params;}

	public void apply(BufferedImage colIn, BufferedImage out, Object [] parameters)
	{
		int w = (int)((Double)parameters[0]).doubleValue();
		double R = (Double)parameters[1];
		double k = (Double)parameters[2];
		
		int [][] in = new int [colIn.getWidth()][colIn.getHeight()];
		int min = 9999, max = -9999;
		double sum = 0;
		for (int i=0;i<colIn.getWidth();i++)
			for (int j=0;j<colIn.getHeight();j++)
			{
				Color col = new Color(colIn.getRGB(i, j), true);
				in[i][j] = (col.getRed()+col.getBlue()+col.getGreen())/3;
				
				if (in[i][j] > max)
					max = in[i][j];
				if (in[i][j] < min)
					min = in[i][j];
				sum += in[i][j];
			}
		
		int [][] I = new int [in.length][in[0].length];
		I[0][0] = in[0][0];
		for (int i=1;i<in.length;i++)
			I[i][0] = in[i][0]+I[i-1][0];
		for (int j=1;j<in[0].length;j++)
			I[0][j] = in[0][j]+I[0][j-1];
		for (int i=1;i<in.length;i++)
			for (int j=4;j<in[0].length;j++)
				I[i][j] = in[i][j]+I[i-1][j]+I[i][j-1]-I[i-1][j-1];
		
		double [][] mean = new double [in.length][in[0].length];
		for (int i=0;i<in.length;i++)
			for (int j=0;j<in[0].length;j++)
			{
				int left = i-w/2 < 0 ? 0 : i-w/2;
				int top = j-w/2 < 0 ? 0 : j-w/2;
				int right = i+w/2 > in.length-1 ? in.length-1 : i+w/2;
				int bottom = j+w/2 > in[0].length-1 ? in[0].length-1 : j+w/2;
				mean[i][j] = ((double)(I[right][bottom]+I[left][top]-I[right][top]-I[left][bottom]))/(w*w);
			}
		
		double [][] s2 = new double [in.length][in[0].length];
		for (int x=0;x<in.length;x++)
			for (int y=0;y<in[0].length;y++)
		{
			sum = 0;
			int left = x-w/2 < 0 ? 0 : x-w/2;
			int top = y-w/2 < 0 ? 0 : y-w/2;
			int right = x+w/2 > in.length-1 ? in.length-1 : x+w/2;
			int bottom = y+w/2 > in[0].length-1 ? in[0].length-1 : y+w/2;
			for (int i=left;i<=right;i++)
				for (int j=top;j<=bottom;j++)
					sum += in[i][j]*in[i][j]-mean[x][y]*mean[x][y];
			s2[x][y] = sum/(w*w);
		}
		
		for (int i=0;i<in.length;i++)
			for (int j=0;j<in[0].length;j++)
		{
			double t = mean[i][j]*(1+k*(Math.sqrt(s2[i][j])/R-1));
			out.setRGB(i, j, in[i][j] <= t ? -16777216 : -1);
		}
	}

}
