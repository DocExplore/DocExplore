package org.interreg.docexplore.stitcher.sauvola;

public interface IntegralImage {

	int getWidth();

	int getHeight();

	int getArea();

	/**
	 *
	 * @param y
	 * @param window The size of the smoothing window to use (in pixels). Must be greater
	 * 		than zero and smaller than the height of the image.
	 * @return The
	 */
	long getHorizontalProjection(int y, int window);

	double[] getGausModel(int xmin, int ymin, int xmax, int ymax);

}