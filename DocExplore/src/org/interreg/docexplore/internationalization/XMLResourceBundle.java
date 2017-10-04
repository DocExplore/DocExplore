/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
				try {return ResourceBundle.getBundle("default-lrb", bundleControl).getString(key);} catch (Exception e) {e.printStackTrace(); return "???";}
			try {return ResourceBundle.getBundle(sub.substring(0, index)+"-lrb", bundleControl).getString(key);} catch (Exception e) {e.printStackTrace(); return "???";}
		}
		
		try {return ResourceBundle.getBundle("langBundle", bundleControl).getString(key);} catch (Exception e) {e.printStackTrace(); return "???";}
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
