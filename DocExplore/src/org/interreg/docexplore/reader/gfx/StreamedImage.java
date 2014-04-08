/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.StreamedResource;
import org.interreg.docexplore.util.ImageUtils;


public class StreamedImage extends StreamedResource implements IIOReadUpdateListener
{
	public BufferedImage image;
	
	protected StreamedImage(ReaderClient client, final String uri, File file)
	{
		super(client, uri, file);
		this.image = null;
	}
	
	public void handle(InputStream stream) throws Exception
	{
//		final ImageReader reader = ImageIO.getImageReadersBySuffix(uri.substring(uri.lastIndexOf(".")+1)).next();
//		reader.addIIOReadUpdateListener(this);
//		reader.setInput(new MemoryCacheImageInputStream(stream));
//		reader.read(0);
		
		if (file == null)
			this.image = ImageUtils.read(stream);
		else if (!file.exists())
			throw new FileNotFoundException(file.getCanonicalPath());
		else this.image = ImageUtils.read(file);
	}
	
	public void passStarted(ImageReader source, BufferedImage theImage,
		int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands)
	{
		this.image = theImage;
	}
	public void imageUpdate(ImageReader source, BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {}
	public void passComplete(ImageReader source, BufferedImage theImage) {}
	public void thumbnailPassComplete(ImageReader source, BufferedImage theThumbnail) {}
	public void thumbnailPassStarted(ImageReader source, BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {}
	public void thumbnailUpdate(ImageReader source, BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {}
	
	public static Allocator<StreamedImage> allocator = new Allocator<StreamedImage>()
	{
		public StreamedImage cast(StreamedResource stream) {return (StreamedImage)stream;}
		public StreamedImage allocate(ReaderClient client, String uri, File file) {return new StreamedImage(client, uri, file);}
	};
}
