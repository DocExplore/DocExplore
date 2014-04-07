package org.interreg.docexplore.util;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Base interface for an image source. An image source targets an image with additional information
 * relevant to the components that use it.
 * @author Burnett
 */
public interface ImageSource extends Serializable
{
	/**
	 * Returns a buffered image representing the image targeted by this source.
	 */
	public BufferedImage getImage() throws Exception;
	
	/**
	 * Returns a stream of the target image. If the image is compressed, the stream should contain
	 * the compressed data.
	 */
	public InputStream getFile();
	
	/**
	 * Returns a generic URI stream expliciting this source.
	 */
	public String getURI();
	
	/**
	 * Returns a hint to whether or not subsequent method calls on this object will succeed. 
	 * Even if this method returns true, calls to other methods may fail (return null in the case 
	 * of getImage() and getFile()).
	 */
	public boolean isValid();
}
