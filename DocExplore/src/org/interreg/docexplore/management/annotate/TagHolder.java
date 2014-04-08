/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
