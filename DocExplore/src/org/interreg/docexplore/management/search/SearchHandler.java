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
import org.interreg.docexplore.management.gui.MainWindow;
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
	
	MainWindow win;
	
	public SearchHandler(MainWindow win)
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
				ResultBuffer<Book> books = Book.search(win.getDocExploreLink(), criteria, disjoint, relevance);
				if (books.bufferSize() > 0)
					results.put(Book.class, books);
			}
			
			if (types.contains(Page.class))
			{
				ResultBuffer<Page> pages = Page.search(win.getDocExploreLink(), criteria, disjoint, relevance);
				if (pages.bufferSize() > 0)
					results.put(Page.class, pages);
			}
			
			if (types.contains(Region.class))
			{
				ResultBuffer<Region> regions = Region.search(win.getDocExploreLink(), criteria, disjoint, relevance);
				if (regions.bufferSize() > 0)
					results.put(Region.class, regions);
			}
			
			if (types.contains(MetaData.class))
			{
				ResultBuffer<MetaData> metaDatas = MetaData.search(win.getDocExploreLink(), criteria, disjoint, relevance);
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
			ResultBuffer<Book> books = Book.search(win.getDocExploreLink(), term, relevance);
			if (books.bufferSize() > 0)
				results.put(Book.class, books);
			
			ResultBuffer<Page> pages = Page.search(win.getDocExploreLink(), term, relevance);
			if (pages.bufferSize() > 0)
				results.put(Page.class, pages);
			
			ResultBuffer<Region> regions = Region.search(win.getDocExploreLink(), term, relevance);
			if (regions.bufferSize() > 0)
				results.put(Region.class, regions);
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		
		return new SearchSummary(term, results, relevance);
	}
	
	public Vector<Object> getSearchKeys()
	{
		Vector<Object> keyFields = new Vector<Object>();
		
		if (win.getDocExploreLink().isLinked()) try
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
			
			for (MetaDataKey key : win.getDocExploreLink().getAllKeys())
				keys.add(key);
			for (MetaDataKey key : keys)
				keyFields.add(new MetaDataKeyEntry(key));
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		return keyFields;
	}
	
	public void resultClicked(SearchResult result)
	{
		try {win.addTab(result.getStub().getObject());}
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
