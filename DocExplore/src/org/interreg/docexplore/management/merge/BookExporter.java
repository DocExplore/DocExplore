/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.merge;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.ManuscriptLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.Pair;

/**
 * Exports objects from a datalink to another. After each add operation, all transfered items are stored in arrays for additional filtering in subclasses.
 * @author Alexander Burnett
 *
 */
public class BookExporter
{
	public static interface MetaDataFilter
	{
		public boolean keepAnnotation(AnnotatedObject from, AnnotatedObject to, MetaData annotation) throws DataLinkException;
	}
	
	public float progress;
	
	protected List<Book> copiedBooks = new Vector<Book>();
	protected List<Page> copiedPages = new Vector<Page>();
	protected List<Region> copiedRegions = new Vector<Region>();
	protected List<Pair<AnnotatedObject, MetaData>> copiedMds = new Vector<Pair<AnnotatedObject, MetaData>>();
	
	public BookExporter()
	{
		this.progress = 0;
	}
	
	void clearCopies()
	{
		copiedBooks.clear();
		copiedPages.clear();
		copiedRegions.clear();
		copiedMds.clear();
	}
	
	public Book add(Book from, ManuscriptLink to, MetaDataFilter filter) throws Exception
	{
		clearCopies();
		try
		{
			Boolean isAutoWrite = (Boolean)to.getProperty("autoWrite");
			if (isAutoWrite != null && isAutoWrite)
				to.setProperty("autoWrite", false);
			
			int bookId = to.getLink().addBook(from.getName());
			Book copy = to.getBook(bookId);
			copiedBooks.add(copy);
			copyMetaData(from, copy, filter);
			
			int lastPage = from.getLastPageNumber();
			for (int i=1;i<=lastPage;i++)
			{
				progress = i*1f/lastPage;
				
				Page page = from.getPage(i);
				Page pageCopy = copy.appendPage(page.getImage());
				copiedPages.add(pageCopy);
				
				copyMetaData(page, pageCopy, filter);
				
				for (Region region : page.getRegions())
					addRegion(region, pageCopy, filter);
				
				page.unloadImage();
				page.unloadMetaData();
				page.unloadRegions();
				pageCopy.unloadImage();
				pageCopy.unloadMetaData();
				pageCopy.unloadRegions();
			}
			
			if (isAutoWrite != null && isAutoWrite)
				to.setProperty("autoWrite", true);
			progress = 1;
			
			from.unloadMetaData();
			copy.unloadMetaData();
			return copy;
		}
		catch (Exception e)
		{
			to.setProperty("autoWrite", true);
			progress = 1;
			throw e;
		}
	}
	
	public Page add(Page page, Book to, MetaDataFilter filter) throws Exception {return add(page, to, -1, filter);}
	public Page add(Page page, Book to, int pageNum, MetaDataFilter filter) throws Exception
	{
		clearCopies();
		try
		{
			Boolean isAutoWrite = (Boolean)to.getLink().getProperty("autoWrite");
			if (isAutoWrite != null && isAutoWrite)
				to.getLink().setProperty("autoWrite", false);
			
			Page pageCopy = pageNum == to.getLastPageNumber()+1 || pageNum < 0 ? 
				to.appendPage(page.getImage()) : 
				to.insertPage(pageNum, page.getImage());
			copiedPages.add(pageCopy);
			copyMetaData(page, pageCopy, filter);
			for (Region region : page.getRegions())
				addRegion(region, pageCopy, filter);
			
			page.unloadImage();
			page.unloadMetaData();
			page.unloadRegions();
			pageCopy.unloadImage();
			pageCopy.unloadMetaData();
			pageCopy.unloadRegions();
			
			if (isAutoWrite != null && isAutoWrite)
				to.getLink().setProperty("autoWrite", true);
			
			return pageCopy;
		}
		catch (Exception e)
		{
			to.getLink().setProperty("autoWrite", true);
			progress = 1;
			throw e;
		}
	}
	
	public Region add(Region region, Page to, MetaDataFilter filter) throws Exception
	{
		clearCopies();
		try
		{
			Boolean isAutoWrite = (Boolean)to.getLink().getProperty("autoWrite");
			if (isAutoWrite != null && isAutoWrite)
				to.getLink().setProperty("autoWrite", false);
			
			Region regionCopy = addMergedRegion(region, to, filter);
			copiedRegions.add(regionCopy);
			
			to.unloadRegions();
			if (isAutoWrite != null && isAutoWrite)
				to.getLink().setProperty("autoWrite", true);
			return regionCopy;
		}
		catch (Exception e)
		{
			to.getLink().setProperty("autoWrite", true);
			progress = 1;
			throw e;
		}
	}
	
	public MetaData add(MetaData md, AnnotatedObject to, MetaDataFilter filter) throws Exception
	{
		return add(md, to.getLink(), to, filter);
	}
	public MetaData add(MetaData md, ManuscriptLink link, MetaDataFilter filter) throws Exception
	{
		return add(md, link, null, filter);
	}
	public MetaData add(MetaData md, ManuscriptLink link, AnnotatedObject to, MetaDataFilter filter) throws Exception
	{
		clearCopies();
		try
		{
			Boolean isAutoWrite = (Boolean)link.getProperty("autoWrite");
			if (isAutoWrite != null && isAutoWrite)
				link.setProperty("autoWrite", false);
			
			MetaData mdCopy = to == null ? addMetaData(md, link, filter) : addMetaData(md, to, filter);
			if (mdCopy != null)
				copiedMds.add(new Pair<AnnotatedObject, MetaData>(to, mdCopy));
			
			if (isAutoWrite != null && isAutoWrite)
				link.setProperty("autoWrite", true);
			return mdCopy;
		}
		catch (Exception e)
		{
			link.setProperty("autoWrite", true);
			progress = 1;
			throw e;
		}
	}
	
	public void merge(Book from, Book to, MetaDataFilter filter) throws Exception
	{
		clearCopies();
		try
		{
			to.getLink().setProperty("autoWrite", false);
			
			int lastPage = from.getLastPageNumber();
			if (lastPage != to.getLastPageNumber())
				throw new Exception(Lang.s("importPageNumberErrorMessage"));
			
			mergeMetaData(from, to);
			for (int i=1;i<=lastPage;i++)
			{
				progress = i*1f/lastPage;
				
				Page fromPage = from.getPage(i);
				Page toPage = to.getPage(i);
				mergeMetaData(fromPage, toPage);
				
				Set<Region> toRegions = toPage.getRegions();
				for (Region fromRegion : fromPage.getRegions())
				{
					Region toRegion = findSimilar(fromRegion, toRegions);
					if (toRegion == null)
						addMergedRegion(fromRegion, toPage, filter);
					else mergeMetaData(fromRegion, toRegion);
				}
				
				fromPage.unloadImage();
				fromPage.unloadMetaData();
				fromPage.unloadRegions();
				toPage.unloadImage();
				toPage.unloadMetaData();
				toPage.unloadRegions();
			}
			
			to.getLink().setProperty("autoWrite", true);
			progress = 1;
			
			from.unloadMetaData();
			to.unloadMetaData();
		}
		catch (Exception e)
		{
			to.getLink().setProperty("autoWrite", true);
			progress = 1;
			throw e;
		}
	}
	
	Region findSimilar(Region from, Set<Region> toRegions) throws DataLinkException
	{
		for (Region to : toRegions)
			if (similar(from, to))
				return to;
		return null;
	}
	
	boolean similar(Region r1, Region r2) throws DataLinkException
	{
		if (r1.getOutline().length != r2.getOutline().length)
			return false;
		float [][] f1 = toNormalizedCoords(r1);
		float [][] f2 = toNormalizedCoords(r2);
		float sum = 0;
		for (int i=0;i<f1.length;i++)
			sum += (f1[i][0]-f2[i][0])*(f1[i][0]-f2[i][0])+(f1[i][1]-f2[i][1])*(f1[i][1]-f2[i][1]);
		return sum < .00001;
	}
	
	float [][] toNormalizedCoords(Region region) throws DataLinkException
	{
		Point [] points = region.getOutline();
		Dimension dim = DocExploreDataLink.getImageDimension(region.getPage());
		float [][] res = new float [points.length][2];
		for (int i=0;i<points.length;i++)
		{
			res[i][0] = points[i].x*1f/dim.width;
			res[i][1] = points[i].y*1f/dim.height;
		}
		return res;
	}
	
	Point [] toPageCoords(float [][] outline, Page page) throws DataLinkException
	{
		Dimension dim = DocExploreDataLink.getImageDimension(page);
		Point [] res = new Point [outline.length];
		for (int i=0;i<outline.length;i++)
			res[i] = new Point((int)(outline[i][0]*dim.width), (int)(outline[i][1]*dim.height));
		return res;
	}
	
	protected void addRegion(Region region, Page toPage, MetaDataFilter filter) throws DataLinkException
	{
		Region regionCopy = toPage.addRegion();
		copiedRegions.add(regionCopy);
		regionCopy.setOutline(region.getOutline());
		copyMetaData(region, regionCopy, filter);
	}
	
	Region addMergedRegion(Region region, Page toPage, MetaDataFilter filter) throws DataLinkException
	{
		float [][] norm = toNormalizedCoords(region);
		Region regionCopy = toPage.addRegion();
		copiedRegions.add(regionCopy);
		regionCopy.setOutline(toPageCoords(norm, toPage));
		copyMetaData(region, regionCopy, filter);
		return regionCopy;
	}
	
	void copyMetaData(AnnotatedObject from, AnnotatedObject to, MetaDataFilter filter) throws DataLinkException
	{
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : from.getMetaData().entrySet())
			for (MetaData metaData : entry.getValue())
				if (filter == null || filter.keepAnnotation(from, to, metaData))
					addMetaData(metaData, to, filter);
	}
	protected MetaData addMetaData(MetaData metaData, ManuscriptLink to, MetaDataFilter filter) throws DataLinkException
	{
		MetaData mdCopy = null;
		MetaDataKey key = null;
		
		String keyName = metaData.getKey().getName("");
		if (keyName == null)
			return null;
		
		key = to.getKey(keyName, "");
		if (key == null)
		{
			key = to.getOrCreateKey(keyName, "");
			String locName = metaData.getKey().getName();
			if (locName != null)
				key.setName(locName);
		}

		mdCopy = new MetaData(to, key, metaData.getType(), metaData.getValue());
		
		copyMetaData(metaData, mdCopy, filter);
		return mdCopy;
	}
	protected MetaData addMetaData(MetaData metaData, AnnotatedObject to, MetaDataFilter filter) throws DataLinkException
	{
		MetaData mdCopy = addMetaData(metaData, to.getLink(), filter);
		to.addMetaData(mdCopy);
		return mdCopy;
	}
	
	void mergeMetaData(AnnotatedObject from, AnnotatedObject to) throws DataLinkException
	{
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : from.getMetaData().entrySet())
			for (MetaData metaData : entry.getValue())
		{
			MetaData mdCopy = null;
			MetaDataKey key = null;
			
			String keyName = entry.getKey().getName("");
//			if (keyName == null)
//			{
//				keyName = entry.getKey().getName("");
//				key = to.getLink().getOrCreateKey(keyName, "");
//			}
//			else key = to.getLink().getOrCreateKey(keyName);
			key = to.getLink().getOrCreateKey(keyName, "");
			
			for (MetaData toMd : to.getMetaDataListForKey(key))
				if (toMd.getType().equals(metaData.getType())/* && 
					(metaData.getType().equals(MetaData.imageType) || metaData.getString().equals(toMd.getString()))*/)
						{mdCopy = toMd; break;}
			
			if (mdCopy == null)
			{
				if (metaData.getType().equals(MetaData.textType))
					mdCopy = new MetaData(to.getLink(), key, metaData.getString());
				else mdCopy = new MetaData(to.getLink(), key, metaData.getType(), metaData.getValue());
				to.addMetaData(mdCopy);
			}
			
			mergeMetaData(metaData, mdCopy);
			metaData.unloadMetaData();
			mdCopy.unloadMetaData();
		}
		from.unloadMetaData();
		to.unloadMetaData();
	}
}
