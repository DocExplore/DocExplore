package org.interreg.docexplore.internationalization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

public class XMLResourceBundle extends ResourceBundle
{
	private Properties props;
	
	XMLResourceBundle(InputStream stream) throws IOException
	{
		props = new Properties();
		props.loadFromXML(stream);
	}
	XMLResourceBundle(File file) throws IOException
	{
		this(new FileInputStream(file));
	}
	
	protected Object handleGetObject(String key)
	{
		return props.getProperty(key);
	}
	
	public Enumeration<String> getKeys()
	{
		return new Enumeration<String>()
		{
			Enumeration<Object> innerEnum = props.keys();
			public boolean hasMoreElements() {return innerEnum.hasMoreElements();}
			public String nextElement() {return innerEnum.nextElement().toString();}
		};
	}
	
	private static XMLBundleControl bundleControl = new XMLBundleControl();
	public static String getString(String baseName, String key)
	{
		return ResourceBundle.getBundle(baseName, bundleControl).getString(key);
	}
	public static String getBundledString(String key)
	{
		String className = new Throwable().getStackTrace()[1].getClassName();
		if (className.startsWith("org.interreg.docexplore."))
		{
			String sub = className.substring("org.interreg.docexplore.".length());
			int index = sub.indexOf('.');
			if (index < 0)
				return ResourceBundle.getBundle("default-lrb", bundleControl).getString(key);
			return ResourceBundle.getBundle(sub.substring(0, index)+"-lrb", bundleControl).getString(key);
		}
		
		return ResourceBundle.getBundle("langBundle", bundleControl).getString(key);
	}
	
	public static String getDefaultBaseName()
	{
		String className = new Throwable().getStackTrace()[1].getClassName();
		if (className.startsWith("org.interreg.docexplore."))
		{
			String sub = className.substring("org.interreg.docexplore.".length());
			return sub.substring(0, sub.indexOf('.'))+"-lrb";
		}
		
		return "langBundle";
	}
}
