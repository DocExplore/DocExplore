/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.search;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.gui.MMTApp;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.ResultBuffer;
import org.interreg.docexplore.util.Pair;

public class SearchHandler
{
	public static class MetaDataKeyEntry
	{
		MetaDataKey key;
		public MetaDataKeyEntry(MetaDataKey key) {this.key = key;}
		public String toString()
		{
			try {return key.getBestName();}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
			return "key "+key.getId();}
	}
	
	public static class SearchSummary
	{
		List<Pair<MetaDataKey, String>> criteria;
		Map<Class<? extends AnnotatedObject>, ResultBuffer<?>> results;
		double relevance;
	
		public SearchSummary(List<Pair<MetaDataKey, String>> criteria, Map<Class<? extends AnnotatedObject>, ResultBuffer<?>> results, double relevance)
		{
			this.criteria = criteria;
			this.results = results;
			this.relevance = relevance;
		}
		
		public SearchSummary(String term, Map<Class<? extends AnnotatedObject>, ResultBuffer<?>> results, double relevance)
		{
			this.criteria = new LinkedList<Pair<MetaDataKey,String>>();
			criteria.add(new Pair<MetaDataKey, String>(null, term));
			this.results = results;
			this.relevance = relevance;
		}
	}
	
	MMTApp win;
	
	public SearchHandler(MMTApp win)
	{
		this.win = win;
	}
	
	double relevance = .65;
	public SearchSummary doSearch(List<Pair<MetaDataKeyEntry, String> > _criteria, boolean disjoint, Collection<Class<? extends AnnotatedObject>> types)
	{
		List<Pair<MetaDataKey, String>> criteria = new LinkedList<Pair<MetaDataKey,String>>();
		for (Pair<MetaDataKeyEntry, String> pair : _criteria)
			criteria.add(new Pair<MetaDataKey, String>((pair.first).key, pair.second));
		
		Map<Class<? extends AnnotatedObject>, ResultBuffer<?>> results = new HashMap<Class<? extends AnnotatedObject>, ResultBuffer<?>>();
		
		try
		{
			if (types.contains(Book.class))
			{
				ResultBuffer<Book> books = Book.search(win.host.getLink(), criteria, disjoint, relevance);
				if (books.bufferSize() > 0)
					results.put(Book.class, books);
			}
			
			if (types.contains(Page.class))
			{
				ResultBuffer<Page> pages = Page.search(win.host.getLink(), criteria, disjoint, relevance);
				if (pages.bufferSize() > 0)
					results.put(Page.class, pages);
			}
			
			if (types.contains(Region.class))
			{
				ResultBuffer<Region> regions = Region.search(win.host.getLink(), criteria, disjoint, relevance);
				if (regions.bufferSize() > 0)
					results.put(Region.class, regions);
			}
			
			if (types.contains(MetaData.class))
			{
				ResultBuffer<MetaData> metaDatas = MetaData.search(win.host.getLink(), criteria, disjoint, relevance);
				if (metaDatas.bufferSize() > 0)
					results.put(MetaData.class, metaDatas);
			}
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		
		return new SearchSummary(criteria, results, relevance);
	}
	
	public SearchSummary doSearch(String term)
	{
		Map<Class<? extends AnnotatedObject>, ResultBuffer<?>> results = new HashMap<Class<? extends AnnotatedObject>, ResultBuffer<?>>();
		
		try
		{
			ResultBuffer<Book> books = Book.search(win.host.getLink(), term, relevance);
			if (books.bufferSize() > 0)
				results.put(Book.class, books);
			
			ResultBuffer<Page> pages = Page.search(win.host.getLink(), term, relevance);
			if (pages.bufferSize() > 0)
				results.put(Page.class, pages);
			
			ResultBuffer<Region> regions = Region.search(win.host.getLink(), term, relevance);
			if (regions.bufferSize() > 0)
				results.put(Region.class, regions);
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		
		return new SearchSummary(term, results, relevance);
	}
	
	public Vector<Object> getSearchKeys()
	{
		Vector<Object> keyFields = new Vector<Object>();
		
		if (win.host.getLink().isLinked()) try
		{
			Set<MetaDataKey> keys = new TreeSet<MetaDataKey>(new Comparator<MetaDataKey>()
				{Collator collator = Collator.getInstance(Locale.getDefault());
				public int compare(MetaDataKey m1, MetaDataKey m2)
				{
					try
					{
						String s1 = m1.getName();
						if (s1 == null) s1 = m1.getName("");
						String s2 = m2.getName();
						if (s2 == null) s2 = m2.getName("");
						if (s1 == null)
							return -1;
						if (s2 == null)
							return 1;
						return collator.compare(s1, s2);
					}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
					return 0;
				}});
			
			for (MetaDataKey key : win.host.getLink().getAllKeys())
				keys.add(key);
			for (MetaDataKey key : keys)
				keyFields.add(new MetaDataKeyEntry(key));
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		return keyFields;
	}
	
	public void resultClicked(SearchResult result)
	{
		try {win.addTab(result.getStub().getObject(), null);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	/*static String buildDescription(List<MetaData> metaDataList) throws ClassCastException, DataLinkException
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (MetaData metaData : metaDataList)
		{
			if (!first)
				sb.append("<br>");
			else first = false;
			
			if (metaData.getKey() == win.getLink().transcriptionKey)
			{
				String value = metaData.getString();
				sb.append("<i>"+extractTagContent(value, "author")+":</i>&nbsp;");
				sb.append(extractTagContent(value, "content"));
			}
		}
		return sb.toString();
	}*/
	
	static String extractTagContent(String xml, String tagName)
	{
		return xml.substring(xml.indexOf("<"+tagName+">")+tagName.length()+2, xml.indexOf("</"+tagName+">"));
	}
}
