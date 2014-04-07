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
