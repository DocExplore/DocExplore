/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher.sauvola;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class ImageWrapper {
	private final BufferedImage src;
	private final int w;
	private final int h;

	int[] raster = null;
	private IntegralImage iImage;

	public ImageWrapper(BufferedImage im) {
		this.src = im;
		this.w = im.getWidth();
		this.h = im.getHeight();
	}

	public final int getWidth()
	{
		return w;
	}

	public final int getHeight()
	{
		return h;
	}

	public synchronized int[] getRaster()
	{
		if (raster == null)
			init(src.getData());

		return raster;
	}

	public synchronized IntegralImage getIntegralImage()
	{
		if (iImage == null)
			init(src.getData());

		return iImage;
	}

	private void init(Raster data) {
		raster = new int[w * h];
		int[] rowsumImage = new int[w * h];
		int[] rowsumSqImage = new int[w * h];

		int offset = 0; // index of the current pixel. Will be y * w + x
		for (int y = 0; y < h; y++) {
			int s = data.getSample(0, y, 0);

			raster[offset] = s;
			rowsumImage[offset] = s;
			rowsumSqImage[offset] = s * s;
			for (int x = 1; x < w; x++) {
				s = data.getSample(x, y, 0);

				int ix = offset + x;
				raster[ix] = s;
				rowsumImage[ix] = rowsumImage[ix - 1] + s;
				rowsumSqImage[ix] = rowsumSqImage[ix - 1] + s * s;
			}

			offset += w;
		}

		long[] integralImage = new long[w * h];
		long[] integralSqImage = new long[w * h];

		for (int x = 0; x < w; x++) {
			integralImage[x] = rowsumImage[x];
			integralSqImage[x] = rowsumSqImage[x];
		}

		offset = w;
		for (int y = 1; y < h; y++) {
			for (int x = 0; x < w; x++) {

				int ix = offset + x;
				integralImage[ix] = integralImage[ix - w] + rowsumImage[ix];
				integralSqImage[ix] = integralSqImage[ix - w] + rowsumSqImage[ix];

			}

			offset += w;
		}

		iImage = new IntegralImageImpl(w, h, integralImage, integralSqImage);
	}
}
