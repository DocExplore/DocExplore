package org.interreg.docexplore.management.align;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;


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
	
	public BufferedImage toImage() {return toImage(cols);}
	public BufferedImage toImage(Color [] cols)
	{
		BufferedImage image = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				image.setRGB(i, j, labelColor(data[i][j], cols).getRGB());
		return image;
	}
	public Color labelColor(Label label) {return labelColor(label, cols);}
	public Color labelColor(Label label, Color [] cols) {return cols[label.val == 0 ? 0 : label.val%(cols.length-1)+1];}
	
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
	
	static Color [] cols;
	static
	{
		cols = new Color [150];
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
	}
}
