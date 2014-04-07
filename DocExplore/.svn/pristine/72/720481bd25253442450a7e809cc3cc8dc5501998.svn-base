package org.interreg.docexplore.management.merge;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.ManuscriptLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.Pair;

public class BookExporter
{
	public static interface MetaDataFilter
	{
		public boolean keepAnnotation(AnnotatedObject from, AnnotatedObject to, MetaData annotation) throws DataLinkException;
	}
	
	public float progress;
	
	public List<Book> copiedBooks = new Vector<Book>();
	public List<Page> copiedPages = new Vector<Page>();
	public List<Region> copiedRegions = new Vector<Region>();
	public List<Pair<AnnotatedObject, MetaData>> copiedMds = new Vector<Pair<AnnotatedObject, MetaData>>();
	
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
		clearCopies();
		try
		{
			Boolean isAutoWrite = (Boolean)to.getLink().getProperty("autoWrite");
			if (isAutoWrite != null && isAutoWrite)
				to.getLink().setProperty("autoWrite", false);
			
			MetaData mdCopy = addMetaData(md, to, filter);
			if (mdCopy != null)
				copiedMds.add(new Pair<AnnotatedObject, MetaData>(to, mdCopy));
			
			if (isAutoWrite != null && isAutoWrite)
				to.getLink().setProperty("autoWrite", true);
			return mdCopy;
		}
		catch (Exception e)
		{
			to.getLink().setProperty("autoWrite", true);
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
				throw new Exception(XMLResourceBundle.getBundledString("importPageNumberErrorMessage"));
			
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
	protected MetaData addMetaData(MetaData metaData, AnnotatedObject to, MetaDataFilter filter) throws DataLinkException
	{
		MetaData mdCopy = null;
		MetaDataKey key = null;
		
		String keyName = metaData.getKey().getName("");
		if (keyName == null)
			return null;
		key = to.getLink().getKey(keyName, "");
		if (key == null)
		{
			key = to.getLink().getOrCreateKey(keyName, "");
			String locName = metaData.getKey().getName();
			if (locName != null)
				key.setName(locName);
		}
		
//		if (metaData.getType().equals(MetaData.textType))
//			mdCopy = new MetaData(to.getLink(), key, metaData.getString());
//		else if (metaData.getType().equals(MetaData.imageType))
//			mdCopy = new MetaData(to.getLink(), key, MetaData.imageType, metaData.getValue());
		mdCopy = new MetaData(to.getLink(), key, metaData.getType(), metaData.getValue());
		to.addMetaData(mdCopy);
		
		copyMetaData(metaData, mdCopy, filter);
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
