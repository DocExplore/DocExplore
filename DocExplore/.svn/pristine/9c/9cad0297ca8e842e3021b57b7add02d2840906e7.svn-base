package org.interreg.docexplore.management.align;


public class MergeWithNeighborFunction implements LabeledImage.LabelFunction
{
	public MergeWithNeighborFunction()
	{
	}
	
	LabeledImage.Label getNear(LabeledImage image, int i, int j)
	{
		for (int di=-1;di<=1;di++)
			for (int dj=-1;dj<=1;dj++)
				if (di!=0 || dj!=0)
		{
			int x = i+di, y = j+dj;
			if (x < 0 || x >= image.data.length || y < 0 || y >= image.data[0].length || image.data[i][j] == image.data[x][y])
				continue;
			return image.data[x][y];
		}
		return null;
	}
	
	int curComp = -1;
	LabeledImage.Label near = null;
	boolean deleted;
	public void doPixel(LabeledImage image, LabeledImage.Label label, int comp, int x, int y)
	{
		if (curComp != comp)
		{
			near = getNear(image, x, y);
			if (near == null)
				return;
			
			curComp = comp;
		}
		image.data[x][y] = near;
		near.size++;
		label.size--;
	}

	public void beforeStart(LabeledImage image, LabeledImage.Label label)
	{
		curComp = -1;
		near = null;
		deleted = false;
	}

	public void afterEnd(LabeledImage image, LabeledImage.Label label)
	{
		if (label.size == 0)
		{
			image.labels.remove(label);
			deleted = true;
		}
		else
		{
			for (int i=0;i<label.components.size();i++)
			{
				int [] where = label.components.get(i);
				if (image.data[where[0]][where[1]] != label)
					label.components.remove(i--);
			}
			
			if (label.components.isEmpty())
			{
				image.labels.remove(label);
				deleted = true;
			}
		}
	}

}
