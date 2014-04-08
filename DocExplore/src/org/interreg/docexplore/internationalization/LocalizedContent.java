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

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class LocalizedContent<Type>
{
	private static Locale docexLocale = Locale.getDefault();
	public static Locale getDefaultLocale() {return docexLocale;}
	public static void setDefaultLocale(Locale locale)
	{
		docexLocale = locale;
		try {Locale.setDefault(docexLocale);}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public static interface ContentTransform<Type, ToType>
	{
		public ToType transform(String lang, Type data);
	}
	
	public Map<String, Type> content;
	
	public LocalizedContent()
	{
		this.content = new TreeMap<String, Type>();
	}
	public LocalizedContent(String lang, Type data)
	{
		this();
		content.put(lang, data);
	}
	
	public void addContent(String lang, Type data)
	{
		content.put(lang, data);
	}
	public void addContent(Locale locale, Type data)
	{
		addContent(locale.getLanguage(), data);
	}
	public void addContent(Type data) {addContent(getDefaultLocale(), data);}
	
	public Type getContent(String lang)
	{
		return content.get(lang);
	}
	public Type getContent(Locale locale)
	{
		return getContent(locale.getLanguage());
	}
	public Type getContent() {return getContent(getDefaultLocale());}
	
	public <ToType> void transform(LocalizedContent<ToType> target, 
		ContentTransform<Type, ToType> transform)
	{
		for (Map.Entry<String, Type> entry : content.entrySet())
			target.addContent(entry.getKey(), 
				transform.transform(entry.getKey(), entry.getValue()));
	}
}
