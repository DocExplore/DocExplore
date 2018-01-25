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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.interreg.docexplore.util.ByteUtils;

public class XMLBundleControl extends ResourceBundle.Control
{
	public List<String> getFormats(String baseName)
	{
		if (baseName == null) throw new NullPointerException();
		return Arrays.asList("xml");
	}
	
	private static Map<String, byte []> addedBundles = new TreeMap<String, byte []>();
	public static void addBundle(String resourceName, InputStream stream) throws IOException 
	{
		byte [] bytes = ByteUtils.readStream(stream);
		addedBundles.put(resourceName, bytes);
		stream.close();
	}

	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
		throws IllegalAccessException, InstantiationException, IOException
	{
		if (baseName == null || locale == null || format == null || loader == null)
			throw new NullPointerException();
		
		ResourceBundle bundle = null;
		if (format.equals("xml"))
		{
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, format);
			InputStream stream = null;
			if (addedBundles.containsKey(resourceName))
			{
				byte [] bytes = addedBundles.get(resourceName);
				stream = new ByteArrayInputStream(bytes);
			}
			else if (reload)
			{
				URL url = loader.getResource(resourceName);
				if (url != null)
				{
					URLConnection connection = url.openConnection();
					if (connection != null)
					{
						// Disable caches to get fresh data for reloading.
						connection.setUseCaches(false);
                    	stream = connection.getInputStream();
					}
				}
			}
			else
			{
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null)
			{
				BufferedInputStream bis = new BufferedInputStream(stream);
				bundle = new Lang(bis);
				bis.close();
			}
		}
		return bundle;
	}
	
	
}
