package org.interreg.docexplore.manuscript;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.manuscript.Book.BookStub;
import org.interreg.docexplore.util.ByteImageSource;
import org.interreg.docexplore.util.ImageSource;
import org.interreg.docexplore.util.Pair;

/**
 * A page from a {@link Book}.
 * @author Alexander Burnett
 *
 */
public class Page extends AnnotatedObject
{
	Book book;
	public int pageNum;
	ImageSource image;
	public Map<Integer, Region> regions;
	
	/**
	 * Load an existing page from a book with the given page number.
	 * @param book
	 * @param pageNum
	 * @param data The page data fetched from the {@link ManuscriptLink}.
	 * @throws DataLinkException
	 */
	Page(Book book, int pageNum, PageData data) throws DataLinkException
	{
		super(book.link, data.pageId);
		this.book = book;
		this.pageNum = pageNum;
		this.image = null;
		this.regions = new TreeMap<Integer, Region>();
		for (Integer regionId : data.regionIds)
			regions.put(regionId, null);
		fillMetaData(data);
	}
	
	/**
	 * Create a new page in a book with the given page number.
	 * @param book
	 * @param pageNum
	 * @param data The page image.
	 * @throws IOException
	 * @throws DataLinkException
	 */
	Page(Book book, int pageNum, ImageSource data) throws IOException, DataLinkException
	{
		super(book.link);
		this.id = link.getLink().addPage(book.id, pageNum, data.getFile());
		this.book = book;
		this.pageNum = pageNum;
		this.image = data;
		this.regions = new TreeMap<Integer, Region>();
	}
	
	public int getId() {return id;}
	public Book getBook() {return book;}
	public int getPageNumber() {return pageNum;}
	
	/**
	 * Remove this page from the containing book.
	 * @throws DataLinkException
	 */
	public void removeFromBook() throws DataLinkException
	{
		book.removePage(pageNum);
	}
	
	/**
	 * Adds a new region of interest.
	 * @return
	 * @throws DataLinkException
	 */
	public Region addRegion() throws DataLinkException
	{
		Region region = new Region(this);
		regions.put(region.id, region);
		return region;
	}
	
	/**
	 * Removes the given region of interest.
	 * @param region
	 * @throws DataLinkException
	 */
	public void removeRegion(Region region) throws DataLinkException
	{
		regions.remove(region.id);
		region.removeAllMetaData();
		link.getLink().removeRegion(region.id, book.getId(), pageNum);
	}
	
	/**
	 * Removes all regions of interest from this page.
	 * @throws DataLinkException
	 */
	public void removeAllRegions() throws DataLinkException
	{
		while (!regions.isEmpty())
			regions.remove(regions.keySet().iterator().next());
	}
	
	/**
	 * Get a region from this page by id.
	 * @param regionId
	 * @return
	 * @throws DataLinkException
	 */
	public Region getRegion(int regionId) throws DataLinkException
	{
		if (!regions.containsKey(regionId))
			throw new DataLinkException(link.getLink(), "Region "+regionId+" doesn't exist in page "+id);
		Region region = regions.get(regionId);
		if (region == null)
		{
			region = new Region(this, regionId);
			regions.put(regionId, region);
		}
		return region;
	}
	
	/**
	 * Get all regions from this page.
	 * @return
	 * @throws DataLinkException
	 */
	public Set<Region> getRegions() throws DataLinkException
	{
		Set<Region> res = new TreeSet<Region>();
		for (Map.Entry<Integer, Region> entry : regions.entrySet())
		{
			if (entry.getValue() == null)
				entry.setValue(new Region(this, entry.getKey()));
			res.add(entry.getValue());
		}
		return res;
	}
	
	/**
	 * Get the page image.
	 * @return
	 * @throws DataLinkException
	 */
	public ImageSource getImage() throws DataLinkException
	{
		if (image == null)
			image = new ByteImageSource(link.getLink().getPageImage(getId(), book.getId(), pageNum));
		return image;
	}
	/**
	 * Set the page image.
	 * @param source
	 * @throws DataLinkException
	 */
	public void setImage(ImageSource source) throws DataLinkException
	{
		link.getLink().setPageImage(getId(), book.getId(), pageNum, source.getFile());
	}
	
	/**
	 * Unload the page image if in memory and requests a GC
	 */
	public void unloadImage() {unloadImage(true);}
	/**
	 * Unload the page image if in memory.
	 * @param gc If true, requests a gc after unloading.
	 */
	public void unloadImage(boolean gc)
	{
		image = null;
		if (gc)
			System.gc();
	}
	/**
	 * Unloads the regions of interest from memory.
	 */
	public void unloadRegions()
	{
		for (Map.Entry<Integer, Region> entry : regions.entrySet())
		{
			if (entry.getValue() != null)
			{
				entry.getValue().unloadMetaData();
				entry.setValue(null);
			}
		}
	}
	/**
	 * Unloads all data associated with this page and requests a GC.
	 */
	public void unloadAll() {unloadAll(true);}
	/**
	 * Unloads all data associated with this page.
	 * @param gc If true, requests a gc after unloading.
	 */
	public void unloadAll(boolean gc)
	{
		unloadImage(false);
		unloadRegions();
		unloadMetaData();
		if (gc)
			System.gc();
	}
	
	/**
	 * Returns the previous page number in the containing book.
	 * @return The previous page number or -1 if there is none.
	 * @throws DataLinkException
	 */
	public int getPreviousPageNumber() throws DataLinkException
	{
		Map.Entry<Integer, Page> entry = book.pagesByNumber.lowerEntry(pageNum);
		if (entry == null)
			return -1;
		return entry.getKey();
	}
	/**
	 * Returns the next page number in the containing book.
	 * @return The next page number or -1 if there is none.
	 * @throws DataLinkException
	 */
	public int getNextPageNumber() throws DataLinkException
	{
		Map.Entry<Integer, Page> entry = book.pagesByNumber.higherEntry(pageNum);
		if (entry == null)
			return -1;
		return entry.getKey();
	}
	
	/**
	 * Reference to a page as a search result.
	 * @author Alexander Burnett
	 *
	 */
	public static class PageStub extends ObjectStub<Page>
	{
		public final BookStub book;
		public final int pageNumber;
		PageStub(BookStub book, String pageId, int pageNumber)
		{
			super(book.link, pageId);
			this.book = book;
			this.pageNumber = pageNumber;
		}
		
		public Page getObject() throws DataLinkException
		{
			return book.getObject().getPage(pageNumber);
		}
	}
	
	/**
	 * Search a {@link ManuscriptLink} for pages matching textual criteria.
	 * @param link The link to search.
	 * @param criteria A list of criteria. Each pair indicates a {@link MetaDataKey} and a search term.
	 * @param disjoint If set to true, at least one criterion must be met to create a match. Otherwise, all criteria must be met.
	 * @param relevance Relevance threshold, between 0 to 1.
	 * @return A buffer containing search matches.
	 * @throws DataLinkException
	 */
	public static ResultBuffer<Page> search(final ManuscriptLink link, List<Pair<MetaDataKey, String>> criteria, boolean disjoint, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = search(link, criteria, disjoint, "page", relevance);
		return new ResultBuffer<Page>(link, res, criteria)
		{
			@SuppressWarnings("unchecked")
			protected PageStub getStubFromId(String id) throws DataLinkException {return Page.getStub(link, id);}
		};
	}
	public static ResultBuffer<Page> search(final ManuscriptLink link, String term, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = search(link, term, "page", relevance);
		return new ResultBuffer<Page>(link, res, null)
		{
			@SuppressWarnings("unchecked")
			protected PageStub getStubFromId(String id) throws DataLinkException {return Page.getStub(link, id);}
		};
	}
	static PageStub getStub(ManuscriptLink link, String pageId) throws DataLinkException
	{
		
		Pair<Integer, Integer> pair = link.getLink().getPageBookIdAndNumber(pageId);
		String name = link.getLink().getBookTitle(pair.first);
		return new PageStub(new BookStub(link, ""+pair.first, name), pageId, pair.second);
	}
}
