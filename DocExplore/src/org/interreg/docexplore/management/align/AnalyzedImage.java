package org.interreg.docexplore.management.align;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.interreg.docexplore.management.align.LabeledImage.Label;
import org.interreg.docexplore.management.process.filters.Sauvola;
import org.interreg.docexplore.util.ImageUtils;

public class AnalyzedImage
{
	BufferedImage original;
	BinaryImage bimage;
	LabeledImage limage;
	
	public AnalyzedImage(BufferedImage original) {this(original, true, true);}
	public AnalyzedImage(BufferedImage original, boolean useLabels) {this(original, useLabels, true);}
	public AnalyzedImage(BufferedImage original, boolean useLabels, boolean useBinary)
	{
		this.original = original;
		
		this.bimage = null;
		if (useBinary)
		{
			BufferedImage simage = new BufferedImage(
					original.getWidth(), 
					original.getHeight(), 
					BufferedImage.TYPE_INT_RGB);
			new Sauvola().apply(original, simage, new Object [] {40., 75., .15});
			
			this.bimage = new BinaryImage(simage, .5f);
			bimage.clean(3, true);
			bimage.clean(3, false);
	//		bimage = bimage.dilate(true);
	//		bimage = bimage.dilate(false);
	//		bimage = bimage.dilate(true);
	//		bimage = bimage.dilate(false);
		}
		
		this.limage = null;
		if (useLabels)
		{
			this.limage = new LabeledImage(bimage);
			
			MergeWithNeighborFunction mn = new MergeWithNeighborFunction();
			for (int i=0;i<limage.labels.size();i++)
			{
				Label label = limage.labels.get(i);
				if (label.size > 10)
					continue;
				limage.traverseLabel(label, mn);
				if (mn.deleted)
					i = -1;
			}
			
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
		}
	}
	
	public static void main(String [] args) throws Exception
	{
		BufferedImage image = ImageUtils.read(new File("C:\\sci\\align\\roi.PNG"));
		AnalyzedImage aimage = new AnalyzedImage(image);
		
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
		
		BufferedImage res = aimage.limage.toImage(cols);
		ImageIO.write(res, "PNG", new File("C:\\sci\\align\\labeled.PNG"));
	}
}
