/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring;

import java.util.List;
import java.util.Map;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.management.merge.BookExporter;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.ManuscriptLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.Pair;

/**
 * Handles the import of objects from a datalink into a presentation (hence the BookImporter inheritance), 
 * with extended filtering options (ImportOptions class) and default values for display ranks of annotations inside regions.
 * @author Alexander Burnett
 *
 */
public class BookImporter extends BookExporter
{
	public static interface PresentationFilter extends BookExporter.MetaDataFilter
	{
		public boolean keepPage(Page page) throws DataLinkException;
		public boolean keepRegion(Region region) throws DataLinkException;
		public void updateMetaData(MetaData md) throws DataLinkException;
	}
	
	public BookImporter()
	{
	}
	
	@Override public Book add(Book from, ManuscriptLink to, MetaDataFilter filter) throws Exception
	{
		Book res = super.add(from, to, filter);
		updateCopiedMetaData(filter != null && filter instanceof PresentationFilter ? (PresentationFilter)filter : null);
		return res;
	}
	@Override public Page add(Page page, Book to, int pageNum, MetaDataFilter filter) throws Exception
	{
		Page res = super.add(page, to, pageNum, filter);
		updateCopiedMetaData(filter != null && filter instanceof PresentationFilter ? (PresentationFilter)filter : null);
		MetaDataKey key = res.getLink().getOrCreateKey("imported-from", "");
		MetaData importedFrom = new MetaData(res.getLink(), key, "From '"+page.getBook().getName()+"', page "+page.getPageNumber());
		res.addMetaData(importedFrom);
		return res;
	}
	@Override public Region add(Region region, Page to, MetaDataFilter filter) throws Exception
	{
		Region res = super.add(region, to, filter);
		updateCopiedMetaData(filter != null && filter instanceof PresentationFilter ? (PresentationFilter)filter : null);
		return res;
	}
	@Override public MetaData add(MetaData md, AnnotatedObject to, MetaDataFilter filter) throws Exception
	{
		MetaData res = super.add(md, to, filter);
		updateCopiedMetaData(filter != null && filter instanceof PresentationFilter ? (PresentationFilter)filter : null);
		return res;
	}
	@Override public MetaData add(MetaData md, ManuscriptLink to, MetaDataFilter filter) throws Exception
	{
		MetaData res = super.add(md, to, filter);
		updateCopiedMetaData(filter != null && filter instanceof PresentationFilter ? (PresentationFilter)filter : null);
		return res;
	}
	
	@Override protected MetaData addMetaData(MetaData metaData, AnnotatedObject to, MetaDataFilter filter) throws DataLinkException
	{
		return super.addMetaData(metaData, to, filter);
	}
	@Override protected void addRegion(Region region, Page toPage, MetaDataFilter filter) throws DataLinkException
	{
		if (filter == null || (filter instanceof PresentationFilter && ((PresentationFilter)filter).keepRegion(region)))
			super.addRegion(region, toPage, filter);
	}

	void updateCopiedMetaData(PresentationFilter filter) throws Exception
	{
		for (Book book : copiedBooks)
			updateObjectMetaData(book, null);
		for (Page page : copiedPages)
			updateObjectMetaData(page, null);
		for (Region region : copiedRegions)
			updateObjectMetaData(region, filter);
		for (Pair<AnnotatedObject, MetaData> pair : copiedMds)
			updateMetaData(pair.first, pair.second, null);
	}
	void updateObjectMetaData(AnnotatedObject object, PresentationFilter filter) throws Exception
	{
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : object.getMetaData().entrySet())
			for (MetaData md : entry.getValue())
				updateMetaData(object, md, filter);
			
	}
	void updateMetaData(AnnotatedObject object, MetaData md, PresentationFilter filter) throws Exception
	{
		if (object == null)
		{
			
		}
		else if (object instanceof Region)
		{
			if (md.getType().equals(MetaData.textType))
			{
				String content = extractContent(md.getString());
				object.removeMetaData(md);
				if (filter != null)
					filter.updateMetaData(md);
				md.setKey(getDisplayKey(md.getLink()));
				object.addMetaData(md);
				md.setString(content);
			}
			setRank(md, getHighestRank(object)+1);
			object.unloadMetaData();
		}
	}
	public static MetaDataKey getDisplayKey(ManuscriptLink link) throws Exception {return link.getOrCreateKey("display", "");}
	String extractContent(String metaData)
	{
		int startIndex = metaData.indexOf("<content>");
		if (startIndex >= 0)
		{
			int endIndex = metaData.indexOf("</content>");
			metaData = metaData.substring(startIndex+9, endIndex > 0 ? endIndex : metaData.length());
		}
		
		startIndex = metaData.indexOf("<tag lang=\"\">");
		if (startIndex >= 0)
		{
			int end = metaData.indexOf("</tag>", startIndex+13);
			metaData = metaData.substring(startIndex+13, end).trim();
		}
		
		if (metaData.trim().length() == 0)
			return "";
		return metaData;
	}
	
	public static int getHighestRank(AnnotatedObject object) throws Exception
	{
		MetaDataKey key = object.getLink().getOrCreateKey("displayRank", "");
		int max = -1;
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : object.getMetaData().entrySet())
			for (MetaData md : entry.getValue())
		{
			List<MetaData> mds = md.getMetaDataListForKey(key);
			if (mds.isEmpty())
				continue;
			int rank = Integer.parseInt(mds.get(0).getString());
			if (rank  > max)
				max = rank;
			md.unloadMetaData();
		}
		return max;
		
	}
	public static MetaData getAtRank(AnnotatedObject object, int at) throws Exception
	{
		MetaDataKey key = object.getLink().getOrCreateKey("displayRank", "");
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : object.getMetaData().entrySet())
			for (MetaData md : entry.getValue())
		{
			List<MetaData> mds = md.getMetaDataListForKey(key);
			if (mds.isEmpty())
				continue;
			int rank = Integer.parseInt(mds.get(0).getString());
			if (rank == at)
				return md;
		}
		return null;
	}
	public static void setRank(MetaData md, int rank) throws Exception
	{
		MetaDataKey key = md.getLink().getOrCreateKey("displayRank", "");
		List<MetaData> mds = md.getMetaDataListForKey(key);
		if (mds.isEmpty())
		{
			MetaData mdRank = new MetaData(md.getLink(), key, ""+rank);
			md.addMetaData(mdRank);
		}
		else mds.get(0).setString(""+rank);
		md.unloadMetaData();
	}
	public static int getRank(MetaData md) throws Exception
	{
		MetaDataKey key = md.getLink().getOrCreateKey("displayRank", "");
		List<MetaData> mds = md.getMetaDataListForKey(key);
		if (mds.isEmpty())
			return -1;
		return Integer.parseInt(mds.get(0).getString());
	}
	public static void insert(MetaData md, AnnotatedObject object, int rank) throws Exception
	{
		MetaDataKey key = object.getLink().getOrCreateKey("displayRank", "");
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : object.getMetaData().entrySet())
			for (MetaData cur : entry.getValue())
		{
			List<MetaData> mds = cur.getMetaDataListForKey(key);
			if (mds.isEmpty())
				continue;
			int curRank = Integer.parseInt(mds.get(0).getString());
			if (curRank >= rank)
				mds.get(0).setString(""+(curRank+1));
			cur.unloadMetaData();
		}
		setRank(md, rank);
		object.addMetaData(md);
	}
	public static void remove(MetaData md, AnnotatedObject object) throws Exception
	{
		MetaDataKey key = object.getLink().getOrCreateKey("displayRank", "");
		int rank = getRank(md);
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : object.getMetaData().entrySet())
			for (MetaData cur : entry.getValue())
		{
			List<MetaData> mds = cur.getMetaDataListForKey(key);
			if (mds.isEmpty())
				continue;
			if (md == cur)
				md.removeMetaData(mds.get(0));
			else
			{
				int curRank = Integer.parseInt(mds.get(0).getString());
				if (curRank > rank)
					mds.get(0).setString(""+(curRank-1));
				cur.unloadMetaData();
			}
		}
		object.removeMetaData(md);
	}
}
