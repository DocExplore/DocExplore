package org.interreg.docexplore.management.annotate;

import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.MetaData;

public class TagHolder
{
	MetaData tag;
	private String tagName;
	
	public TagHolder(MetaData tag) throws DataLinkException
	{
		this.tag = tag;
		this.tagName = tag.getString();
	}
	public TagHolder(String tagName)
	{
		this.tag = null;
		this.tagName = tagName;
	}
	
	public String toString() {return extractLocalizedTag(tagName, Locale.getDefault().getLanguage());}
	
	public static final TagHolder emptyTagHolder = new TagHolder("<tag></tag>");
	
	public static Vector<TagHolder> createTagVector(Set<MetaData> tags)
	{
		Vector<TagHolder> res = new Vector<TagHolder>(tags.size());
		for (MetaData tag : tags) try
		{
			String tagName = extractLocalizedTag(tag.getString(), Locale.getDefault().getLanguage());
			if (tagName != null && tagName.trim().length() > 0)
				res.add(new TagHolder(tag));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return res;
	}
	
	public String extractLocalizedTag(String lang) {return extractLocalizedTag(tagName, lang);}
	public static String extractLocalizedTag(String tagXml, String lang)
	{
		if (lang == null)
			return extractLocalizedTag(tagXml, "");
		
		int start = tagXml.indexOf("<tag lang=\""+lang+"\">");
		if (start >= 0)
		{
			int end = tagXml.indexOf("</tag>", start);
			return tagXml.substring(start+lang.length()+13, end);
		}
		
		start = tagXml.indexOf("<tag>");
		if (start >= 0)
		{
			int end = tagXml.indexOf("</tag>", start);
			return tagXml.substring(start+5, end);
		}
		
		if (lang.length() > 0)
			return extractLocalizedTag(tagXml, "");
		return null;
	}
}
