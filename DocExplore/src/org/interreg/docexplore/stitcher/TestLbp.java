package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.interreg.docexplore.management.process.filters.Sauvola;
import org.interreg.docexplore.stitcher.fast.Fast12;
import org.interreg.docexplore.stitcher.fast.FeaturePoint;
import org.interreg.docexplore.stitcher.sauvola.FastSauvola;
import org.interreg.docexplore.util.ImageUtils;

import de.lmu.ifi.dbs.jfeaturelib.ImagePoint;
import de.lmu.ifi.dbs.jfeaturelib.LibProperties;
import de.lmu.ifi.dbs.jfeaturelib.pointDetector.FASTCornerDetector;
import de.lmu.ifi.dbs.jfeaturelib.pointDetector.Moravec;
import de.lmu.ifi.dbs.jfeaturelib.pointDetector.TrajkovicHedley4N;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class TestLbp
{
	static short gray(int rgb)
	{
		return (short)((ImageUtils.red(rgb)+ImageUtils.green(rgb)+ImageUtils.blue(rgb))/3);
	}
	public static void main(String [] args) throws Exception
	{
		BufferedImage image = ImageUtils.read(new File("C:\\Users\\aburn\\Documents\\pres\\Msu_18bis-140\\Msu_18bis _1.jpg"));
		System.out.println("Image loaded "+image.getWidth()*image.getHeight());
		
		FastSauvola fs = new FastSauvola();
		fs.initialize(image);
		int [] res = fs.call();
		
		List<POI> points = FeatureDetector.Surf.computeFeatures(null, image);
		System.out.println(points.size());
		
		JFrame win = new JFrame("TextLBP");
		JPanel canvas = new JPanel(null) {@Override protected void paintChildren(Graphics g)
		{
			g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			g.setColor(Color.red);
			for (POI point : points)
			{
				int x = (int)(point.x*getWidth()/image.getWidth());
				int y = (int)(point.y*getHeight()/image.getHeight());
				g.drawRect(x, y, 1, 1);
			}
			for (int i=0;i<res.length;i++)
			{
				int x = i%image.getWidth();
				int y = i/image.getWidth();
				g.setColor(res[i] == 0 ? Color.black : Color.white);
				g.drawRect(x, y, 1, 1);
			}
		}};
		canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		win.add(canvas);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setVisible(true);
	}
}
