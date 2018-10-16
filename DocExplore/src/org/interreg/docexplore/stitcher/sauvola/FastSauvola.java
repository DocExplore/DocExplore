/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
/* Created on       May 28, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage (IDCH),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED.
 */
package org.interreg.docexplore.stitcher.sauvola;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 * An adaptive thresholding algorithm based on the technique
 * described by J. Sauvola in:
 *
 * Sauvola, J. and M. Pietik�inen, Adaptive document image binarization. In
 *      Pattern Recognition 33 (2000) pp 255-236.
 *
 * @author Neal Audenaert
 */
public class FastSauvola
{
	// TODO need to factor out the integral image concepts and tools from the thresholder
	private static final int N_THREADS = 10;		// default number of threads to use internally

	private static final ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

	private static final int bgPx = 0;
	private static final int fgPx = 1;

	// FIXME need to shut down executor service. We should probably think about starting up
	//       a Thresholder, configure it, use it to process multiple images and
	//	     then shut it down. The current architecture requires us to create, configure and
	//		 shutdown a lot of thresholder instances when the only thing that will change will
	//	     be the individual image being processed
    private final ExecutorService ex;

    private int width  = 0;
    private int height = 0;
    private int imArea = 0;
    private final AtomicInteger ct = new AtomicInteger(0);

//    private boolean enableOutput = true;
//    private BufferedImage sourceImage = null;
    private BufferedImage outputImage = null;

    private IntegralImage iImage;
    private int[] data;

    // -----------------------------------------------------------------------
    // PROPERTIES
    // -----------------------------------------------------------------------
    // TODO make these configuration parameters.
    private int    ts = 48;     	// tile size
    private int    whalf = ts / 2;	// half the window size
    private double k  = 0.3;    	//
    private int    range  = 128;    	// control for dynamic range
	private int[]  output;

    public static BufferedImage toImage(int[] data, BufferedImage model)
	{
		int offset = 0;
		int width = model.getWidth();
		int height = model.getHeight();

		ColorModel colorModel = model.getColorModel();
		WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height);
		for (int r = 0; r < height; r++)
		{
			for (int c = 0; c < width; c++)
			{
				raster.setSample(c, r, 0, data[offset + c] == fgPx ? 0 : 255);
			}

			offset += width;
		}

		return new BufferedImage(colorModel, raster, true, new Hashtable<>());
	}

	/** Default constructor. */
    public FastSauvola() {
    	ex = Executors.newFixedThreadPool(N_THREADS);
    }

    public FastSauvola(int nThreads) {
    	ex = Executors.newFixedThreadPool(nThreads);
    }

    // HACK: post construction initialization is a well recognized anti-pattern
    public void initialize(File file) throws IOException {
		if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException("Filename does not refer to a readable image file");
        }

		initialize(ImageIO.read(file));
    }

    public void initialize(BufferedImage image) {
     	image = op.filter(image, null);
     	ImageWrapper wrapper = new ImageWrapper(image);
        initialize(wrapper);

    }

    public void initialize(ImageWrapper iIm) {
    	this.iImage = iIm.getIntegralImage();
    	this.data = iIm.getRaster();

    	width = iImage.getWidth();
    	height = iImage.getHeight();
    	imArea = iImage.getArea();

    	this.output = new int[width * height];

    	// Makes for a reasonable assumption, but this parameter really needs
    	// to be configured for good results
    	ts = width / 15;
    }

    public int[] call() throws InterruptedException, IOException
    {
    	if (!this.isReady())
            throw new IllegalStateException("The thresholding algorithm has not been properly initialized");

        return performThresholding();
    }

    private int[] performThresholding() throws InterruptedException {
		int offset = 0;

		int[] rows = new int[height];
		for (int row = 0; row < height; row++)
		{
			rows[row] = 0;
			for (int col = 0; col < width; col++)
			{
				if (data[offset + col] < 255)
					rows[row]++;
			}
			offset += width;
		}

		offset = 0;
        for (int row = 0; row < height; row++) {
    		ex.execute(new RowProcessor(row, offset));
    		offset += width;
        }

        ex.shutdown();
        ex.awaitTermination(10, TimeUnit.SECONDS);		// HACK: arbitrary delay

        offset = 0;
        rows = new int[height];
		for (int row = 0; row < height; row++)
		{
			rows[row] = 0;
			for (int col = 0; col < width; col++)
			{
				if (output[offset + col] == fgPx)
					rows[row]++;
			}
			offset += width;
		}
        return output;
	}

	// -----------------------------------------------------------------------
    // ACCESSOR METHODS
    // -----------------------------------------------------------------------
    public Map<String, String> listParamters() {
        // NOTE this isn't particularly good form, but I'm working on figuring
        //      out how I want to handle parameters.

        Map<String, String> params = new HashMap<String, String>();
        params.put("k", "Takes positive numbers. This is a weighting factor " +
        		"that adjusts how sensitve the algorithm is to the standard " +
        		"deviation. Lower valus will result in more pixels being " +
        		"identified as foreground, raising this value will result in " +
        		"more pixels being identified as background. The default " +
        		"value 0.5 following the recommendation of the original " +
        		"Sauvola paper.");

        params.put("ts", "Tile Size. This is the size of the tiles the Sauvola" +
        		"algorithm will use to evaluate local features. This should " +
        		"be set to a value that is approximately three characters " +
        		"wide. That is, it should be large enough to capture the " +
        		"variation between the text and background, but small enough" +
        		"that the background (including any damage to the text) is " +
        		"relatively uniform. By default, this is set to w / 15, " +
        		"where 'w' is the image width.");

        return params;
    }

    public double getParameter(String param) {
        if (param.equals("k")) {
            return k;
        } else if (param.equals("ts")) {
            return ts;
        } else {
            throw new IllegalArgumentException("Unrecognized parameter: " + param);
        }
    }

    public void setParameter(String param, double value) {

        if (param.equals("k")) {
            if (value < 0)
                throw new IllegalArgumentException("Invalid value for 'k' (" + value + "). Must be a positive number.");


            k = value;

        } else if (param.equals("ts")) {
            if (value < 0)
                throw new IllegalArgumentException("Invalid value for 'ts' (" + value + "). Must be a positive number.");


            ts = (int) Math.round(value);
            whalf = ts / 2;
        } else {
            throw new IllegalArgumentException("Unrecognized parameter: " + param);
        }
    }

    public BufferedImage getResult() {
        if (outputImage != null)
        	return outputImage;
        else
        	throw new IllegalStateException("Execution is not complete");
    }

    public final int getWidth()
	{
		return width;
	}


	public final int getHeight()
	{
		return height;
	}

	public final int getArea()
	{
		return imArea;
	}

	public final int getForegroundPixelCount()
	{
		return ct.get();
	}

	public final int getBackgroundPixelCount()
	{
		return imArea - ct.get();
	}


	public final boolean isReady() {
        return true;
    }

	private final class RowProcessor implements Runnable {
		private final int rowIx;
		private final int offset;

		private RowProcessor(int rowIx, int offset) {
			this.rowIx = rowIx;
			this.offset = offset;
		}

		@Override
		public void run() {
			double mean, stddev;
			int xmin, ymin, xmax, ymax;

			double threshold;
			boolean isBackground;

			for (int colIx = 0; colIx < width; colIx++) {
				ymin = Math.max(0, rowIx - whalf);
				xmin = Math.max(0, colIx - whalf);
				xmax = Math.min(width - 1, colIx + whalf);
				ymax = Math.min(height - 1, rowIx + whalf);

				double[] model = iImage.getGausModel(xmin, ymin, xmax, ymax);
				mean = model[0];
				stddev = Math.sqrt(model[1]);

				threshold = mean * (1 + k * ((stddev / range) - 1));
//				if (rowIx > 1690)
//					System.out.println(threshold);

				isBackground = data[offset + colIx] > threshold;
				output[offset + colIx] = isBackground ? bgPx : fgPx;

				if (!isBackground)
					ct.incrementAndGet();
			}
		}
	}
}