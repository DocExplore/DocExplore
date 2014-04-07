package org.interreg.docexplore.manuscript;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.RegionData;
import org.interreg.docexplore.manuscript.Book.BookStub;
import org.interreg.docexplore.manuscript.Page.PageStub;
import org.interreg.docexplore.util.Pair;

/**
 * A region of interest in a {@link Page}.
 * @author Alexander Burnett
 *
 */
public class Region extends AnnotatedObject
{
	Page page;
	Point [] outline;
	
	/**
	 * Load an existing region in a page with the given id.
	 * @param page
	 * @param id
	 * @throws DataLinkException
	 */
	Region(Page page, int id) throws DataLinkException
	{
		super(page.link, id);
		RegionData data = link.getLink().getRegionData(id, page.getBook().getId(), page.getPageNumber());
		this.page = page;
		this.outline = new Point [data.outline.size()];
		int cnt = 0;
		for (Point point : data.outline)
			outline[cnt++] = new Point(point.x, point.y);
		fillMetaData(data);
	}
	
	/**
	 * Create a new region in a page.
	 * @param page
	 * @throws DataLinkException
	 */
	Region(Page page) throws DataLinkException
	{
		super(page.link);
		this.id = link.getLink().addRegion(page.id, page.getBook().getId(), page.getPageNumber());
		this.page = page;
		this.outline = new Point [0];
	}
	
	public Page getPage() {return page;}
	
	/**
	 * Removes this region from the containing page.
	 * @throws DataLinkException
	 */
	public void removeFromPage() throws DataLinkException
	{
		page.removeRegion(this);
	}
	
	/**
	 * Returns an array of points representing the outline of this region. The last point connects to the first to close the shape.
	 * @return
	 */
	public Point [] getOutline() {return outline;}
	
	/**
	 * Set a new outline for this region. The last point connects to the first to close the shape.
	 * @param outline
	 * @throws DataLinkException
	 */
	public void setOutline(Point [] outline) throws DataLinkException
	{
		link.getLink().setRegionOutline(id, page.getBook().getId(), page.getPageNumber(), outline);
		this.outline = outline;
	}
	
	/**
	 * Reference to a region of interest as a search result.
	 * @author Alexander Burnett
	 *
	 */
	public static class RegionStub extends ObjectStub<Region>
	{
		public final PageStub page;
		RegionStub(PageStub page, String regionId)
		{
			super(page.link, regionId);
			this.page = page;
		}
		
		public Region getObject() throws DataLinkException
		{
			String [] parts = objectId.split("/");
			return page.getObject().getRegion(Integer.parseInt(parts[parts.length-1]));
		}
	}
	
	/**
	 * Search a {@link ManuscriptLink} for regions matching textual criteria.
	 * @param link The link to search.
	 * @param criteria A list of criteria. Each pair indicates a {@link MetaDataKey} and a search term.
	 * @param disjoint If set to true, at least one criterion must be met to create a match. Otherwise, all criteria must be met.
	 * @param relevance Relevance threshold, between 0 to 1.
	 * @return A buffer containing search matches.
	 * @throws DataLinkException
	 */
	public static ResultBuffer<Region> search(final ManuscriptLink link, List<Pair<MetaDataKey, String>> criteria, boolean disjoint, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = search(link, criteria, disjoint, "region", relevance);
		return new ResultBuffer<Region>(link, res, criteria)
		{
			@SuppressWarnings("unchecked")
			protected RegionStub getStubFromId(String id) throws DataLinkException {return Region.getStub(link, id);}
		};
	}
	public static ResultBuffer<Region> search(final ManuscriptLink link, String term, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = search(link, term, "region", relevance);
		return new ResultBuffer<Region>(link, res, null)
		{
			@SuppressWarnings("unchecked")
			protected RegionStub getStubFromId(String id) throws DataLinkException {return Region.getStub(link, id);}
		};
	}
	static RegionStub getStub(ManuscriptLink link, String regionId) throws DataLinkException
	{
		String pageId = link.getLink().getRegionPageId(regionId);
		Pair<Integer, Integer> pair = link.getLink().getPageBookIdAndNumber(pageId);
		String name = link.getLink().getBookTitle(pair.first);
		return new RegionStub(new PageStub(new BookStub(link, ""+pair.first, name), pageId, pair.second), regionId);
	}
}
