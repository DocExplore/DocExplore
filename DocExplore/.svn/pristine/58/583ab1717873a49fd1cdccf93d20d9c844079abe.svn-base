package org.interreg.docexplore.management.process.align;
import java.util.HashSet;
import java.util.Set;



public class FillBackgroundFunction implements LabeledImage.LabelFunction
{
	LabeledImage.Label background;
	Set<LabeledImage.Label> neighbors;
	boolean deleted;
	
	public FillBackgroundFunction(LabeledImage.Label background)
	{
		this.background = background;
		this.neighbors = new HashSet<LabeledImage.Label>();
	}
	
	public void doPixel(LabeledImage image, LabeledImage.Label label, int comp, int i, int j)
	{
		if (label == background)
			return;
		
		for (int di=-1;di<2;di++)
			for (int dj=-1;dj<2;dj++)
				if (di != 0 || dj != 0)
					if (i+di >= 0 && i+di < image.data.length && j+dj >= 0 && j+dj < image.data[0].length)
						neighbors.add(image.data[i+di][j+dj]);
	}

	public void beforeStart(LabeledImage image, LabeledImage.Label label)
	{
		this.neighbors.clear();
		this.deleted = false;
	}

	public void afterEnd(LabeledImage image, LabeledImage.Label label)
	{
		if (neighbors.contains(background))
			return;
		
		for (int i=0;i<image.data.length;i++)
			for (int j=0;j<image.data[0].length;j++)
				if (image.data[i][j] == label)
					image.data[i][j] = background;
		
		background.components.addAll(label.components);
		background.size += label.size;
		image.labels.remove(label);
		
		deleted = true;
	}
}
