/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher.fast;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class FastBenchmark {

	private int[][] pixels = null;
	BufferedImage img = null;
	private int w = 0;
	private int h = 0;
	private String IMG_PATH = null;
	private String IMG_NAME = null;
	private final String SEPARATOR = System.getProperty("file.separator");
	
	public static void main(String[] args) throws IOException
	{
		FastBenchmark benchmark = new FastBenchmark();
		boolean createImages = false;
		if (args != null && args.length == 1 && args[0].equals("-i")) {
			createImages = true;
		}
		benchmark.run(createImages);
	}
	
	/**
	 * Start the benchmark.
	 * @throws IOException
	 */
	public void run(boolean createImages) throws IOException
	{
		setUp();
		_run(createImages);
	}
	
	/**
	 * Pre-benchmark setup.
	 * @throws IOException
	 */
	private void setUp() throws IOException
	{	
		// Put the path to your own images here. Use the SEPARATOR constant to ensure
		// cross-platform directories.
		IMG_PATH = "";
		// Image name. The reason for separating image name and path is to have easy access to
		// the directory to create the grayscale "cornered" images.
		IMG_NAME = "";
		
		// Load image and turn it to gray.
		img = ImageIO.read(new File(IMG_PATH + IMG_NAME));
		img = Filter.grayScale(img);
		w = img.getWidth();
		h = img.getHeight();
		pixels = new int[h][w];
		
		// The FAST detector takes a two-dimensional integer array,
		// so we need to convert it.
		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				pixels[y][x] = img.getRGB(x, y) & 0xFF;
			}
		}
	}
	
	/**
	 * The main benchmark method.
	 * @throws IOException 
	 */
	private void _run(boolean createImages) throws IOException 
	{
		// The benchmark's output:
		// + B: threshold
		// + N: number of corners
		// + T: time taken
		
		// Choose your own thresholds, these are the ones I've tested with.
		int[] thresholds = new int[]{30, 25, 22, 20, 19};
	
		for (int t : thresholds) {
			// Run non-maximum suppression first. We will run both versions
			// without a cap on the number of returned corners so we can
			// compare the results.
			long start = System.currentTimeMillis();
			List<FeaturePoint> f1 = Fast12.detectWithNonMax(pixels, w, h, t, -1);
			long stop = System.currentTimeMillis();
			if (createImages)
				drawImage(f1, t, "with_nonmax");
			System.out.println("With non-max: B=" + t + ": N=" + f1.size() + ": T=" + (stop - start));
			
			// Run without non-maximum suppression.
			start = System.currentTimeMillis();
			List<FeaturePoint> f2 = Fast12.detect(pixels, w, h, t, -1);
			stop = System.currentTimeMillis();
			if (createImages)
				drawImage(f2, t,"without_nonmax");
			System.out.println("Without non-max: B=" + t + ": N=" + f2.size() + ": T=" + (stop - start));
			System.out.println("-----------------------");
		}
	}
	
	/**
	 * Creates a grayscale image with the corners painted in red, for visualising
	 * algorithm.
	 * @param featurePoints The feature points.
	 * @param t The threshold to append to the image name.
	 * @param extra Extra information to append to the image name, mostly used,
	 * 				to distinguish between "with non max" and "without non max".
	 * @throws IOException
	 */
	private void drawImage(List<FeaturePoint> featurePoints, int t, String extra) throws IOException
	{
		//Draw up some rectangles to have something to demonstrate.
		Graphics2D g2 = img.createGraphics();
		g2.setColor(new Color(250, 0 ,0));
		int count = featurePoints.size();
		for (int i = 0; i < count; ++i) {
			FeaturePoint p = featurePoints.get(i);
			g2.drawRect(p.x(), p.y(), 1, 1);
		}
		String[] tokens = IMG_NAME.split("\\.");
		File out = new File(IMG_PATH + tokens[0] + "_" + extra + "_t" + t + "." + tokens[tokens.length-1]);
		ImageIO.write(img, "jpg", out);
	}
}

