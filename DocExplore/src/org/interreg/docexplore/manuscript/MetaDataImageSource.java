package org.interreg.docexplore.manuscript;

import java.io.InputStream;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.BufferedImageSource;

/**
 * An image source that reads its data from a {@link MetaData} object.
 * @author Alexander Burnett
 *
 */
public class MetaDataImageSource extends BufferedImageSource
{
	private static final long serialVersionUID = -3943856435571070511L;

	MetaData annotation;
	
	public MetaDataImageSource(MetaData annotation)
	{
		this.annotation = annotation;
	}
	
	public InputStream getFile()
	{
		try {return annotation.getValue();}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e); return null;}
	}

	public String getURI() {return annotation.getCanonicalUri();}

	public boolean isValid() {return annotation.getType() == MetaData.imageType;}

}
