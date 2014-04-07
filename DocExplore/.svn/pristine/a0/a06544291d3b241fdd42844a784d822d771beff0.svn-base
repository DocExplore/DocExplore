package org.interreg.docexplore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileImageSource extends BufferedImageSource
{
	private static final long serialVersionUID = -2233760169291967207L;
	
	File file;
	
	public FileImageSource(File file)
	{
		this.file = file;
	}
	
	public void setFile(File file)
	{
		this.file = file;
		this.buffer = null;
	}
	public File getReferencedFile() {return file;}
	
	public InputStream getFile()
	{
		try {return new FileInputStream(file);}
		catch (IOException e) {return null;}
	}

	public String getURI()
	{
		try {return file.getCanonicalPath();}
		catch (IOException e) {return null;}
	}

	public boolean isValid()
	{
		return file!=null && file.exists() && ImageUtils.isSupported(file.getName());
	}

}
