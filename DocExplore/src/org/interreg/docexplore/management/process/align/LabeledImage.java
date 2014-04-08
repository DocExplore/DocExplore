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
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;


public class LabeledImage
{
	static class Label
	{
		int val;
		int size;
		List<int []> components;
		
		public Label(int val)
		{
			this.val = val;
			this.components = new LinkedList<int[]>();
			this.size = 0;
		}
	}
	
	static interface LabelFunction
	{
		public void doPixel(LabeledImage image, Label label, int comp, int x, int y);
		public void beforeStart(LabeledImage image, Label label);
		public void afterEnd(LabeledImage image, Label label);
	}
	
	Label [][] data;
	boolean [][] traversalState;
	List<Label> labels;
	
	public LabeledImage(BinaryImage bimage)
	{
		this.data = new Label [bimage.data.length][bimage.data[0].length];
		this.traversalState = new boolean [bimage.data.length][bimage.data[0].length];
		for (int i=0;i<bimage.data.length;i++)
			for (int j=0;j<bimage.data[0].length;j++)
			{
				data[i][j] = null;
				traversalState[i][j] = false;
			}
		this.labels = new LinkedList<Label>();
		
		for (int i=0;i<data.length;i++)
			for (int j=0;j<data[0].length;j++)
				if (data[i][j] == null)
					traverseComponent(bimage, i, j);
	}
	
	public BufferedImage toImage(Color [] cols)
	{
		BufferedImage image = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, cols[data[i][j].val == 0 ? 0 : data[i][j].val%(cols.length-1)+1].getRGB());
		return image;
	}
	
	public void traverseLabel(Label label, LabelFunction function)
	{
		function.beforeStart(this, label);
		
		LinkedList<int []> neighbs = new LinkedList<int []>();
		for (int i=0;i<label.components.size();i++)
		{
			int [] start = label.components.get(i);
			neighbs.clear();
			function.doPixel(this, label, i, start[0], start[1]);
			traversalState[start[0]][start[1]] = !traversalState[start[0]][start[1]];
			neighbs.add(start);
			
			while (!neighbs.isEmpty())
			{
				int [] where = neighbs.removeFirst();
				for (int di=-1;di<=1;di++)
					for (int dj=-1;dj<=1;dj++)
						if (di!=0 || dj!=0)
				{
					int x = where[0]+di, y = where[1]+dj;
					if (x < 0 || x >= data.length || y < 0 || y >= data[0].length || data[x][y] != label || 
						traversalState[x][y] == traversalState[where[0]][where[1]])
							continue;
					
					function.doPixel(this, label, i, x, y);
					traversalState[x][y] = !traversalState[x][y];
					neighbs.add(new int [] {x, y});
				}
			}
		}
		function.afterEnd(this, label);
	}
	
	void traverseComponent(BinaryImage bimage, int i, int j)
	{
		LinkedList<int []> neighbs = new LinkedList<int []>();
		boolean pixel = bimage.data[i][j];
		Label label = new Label(labels.size());
		data[i][j] = label;
		label.size++;
		labels.add(label);
		label.components.add(new int [] {i, j});
		neighbs.add(new int [] {i, j});
		
		while (!neighbs.isEmpty())
		{
			int [] where = neighbs.removeFirst();
			for (int di=-1;di<=1;di++)
				for (int dj=-1;dj<=1;dj++)
					if (di!=0 || dj!=0)
			{
				int x = where[0]+di, y = where[1]+dj;
				if (x < 0 || x >= data.length || y < 0 || y >= data[0].length || bimage.data[x][y] != pixel || data[x][y] == label)
					continue;
				data[x][y] = label;
				label.size++;
				neighbs.add(new int [] {x, y});
			}
		}
	}
	
	public static void main(String [] args) throws Exception
	{
		final BufferedImage image = ImageIO.read(new File("C:\\sci\\align\\roib.PNG"));
		BinaryImage bimage = new BinaryImage(image, .5f);
		bimage.clean(3, true);
		bimage.clean(3, false);
		bimage = bimage.dilate(true);
		bimage = bimage.dilate(false);
		bimage = bimage.dilate(true);
		bimage = bimage.dilate(false);
		
		LabeledImage limage = new LabeledImage(bimage);
		
		int max = 0;
		Label background = null;
		for (int i=0;i<limage.labels.size();i++)
		{
			Label label = limage.labels.get(i);
			if (label.size > max)
				{max = label.size; background = label;}
		}
		
		FillBackgroundFunction fb = new FillBackgroundFunction(background);
		for (int i=0;i<limage.labels.size();i++)
		{
			Label label = limage.labels.get(i);
			if (label == background)
				continue;
			limage.traverseLabel(label, fb);
			if (fb.deleted)
				i--;
		}
		
		Color [] cols = new Color [150];
		cols[0] = Color.black;
		for (int i=1;i<cols.length;i++)
		{
			float r = ((2*i)%cols.length)*1f/cols.length;
			float g = ((3*i)%cols.length)*1f/cols.length;
			float b = ((4*i)%cols.length)*1f/cols.length;
			
			r = (r+.3f)/1.3f;
			g = (g+.3f)/1.3f;
			b = (b+.3f)/1.3f;
			
			cols[i] = new Color(r, g, b);
		}
		
		BufferedImage res = limage.toImage(cols);
		ImageIO.write(res, "PNG", new File("C:\\sci\\align\\labeled.PNG"));
	}
}
