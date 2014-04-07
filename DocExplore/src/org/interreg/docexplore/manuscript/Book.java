package org.interreg.docexplore.manuscript;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.util.ImageSource;
import org.interreg.docexplore.util.Pair;

/**
 * A book that is persisted in a {@link ManuscriptLink}.
 * @author Alexander Burnett
 */
public class Book extends AnnotatedObject
{
	String name;
	public NavigableMap<Integer, Page> pagesByNumber;
	
	/**
	 * Loads an existing AnnotatedObject from a {@link ManuscriptLink} with the given id.
	 * @param link 
	 * @param id 
	 * @throws DataLinkException If an error occurs or the book is already loaded.
	 */
	Book(ManuscriptLink link, int id) throws DataLinkException
	{
		super(link, id);
		if (link.books.containsKey(id)) throw new DataLinkException(link.getLink(), "Book "+id+" already loaded");
		
		BookData data = link.getLink().getBookData(id);
		this.name = data.name;
		this.pagesByNumber = new TreeMap<Integer, Page>();
		for (Integer pageNum : data.pageNumbers)
			pagesByNumber.put(pageNum, null);
		fillMetaData(data);
		
		link.books.put(id, this);
	}
	
	/**
	 * Creates a new AnnotatedObject and persists it in the {@link ManuscriptLink}.
	 * @param link 
	 * @param name The name of the book.
	 * @throws DataLinkException
	 */
	public Book(ManuscriptLink link, String name) throws DataLinkException
	{
		super(link);
		
		this.id = link.getLink().addBook(name);
		this.name = name;
		this.pagesByNumber = new TreeMap<Integer, Page>();
		
		link.books.put(id, this);
	}
	
	/**
	 * Synchronize this book with the underlying {@link ManuscriptLink}
	 * @throws DataLinkException
	 */
	public void reload() throws DataLinkException
	{
		BookData data = link.getLink().getBookData(id);
		this.name = data.name;
		this.pagesByNumber = new TreeMap<Integer, Page>();
		for (Integer pageNum : data.pageNumbers)
			pagesByNumber.put(pageNum, null);
		fillMetaData(data);
	}
	
	public int getId() {return id;}
	public String getName() {return name;}
	
	public void setName(String name) throws DataLinkException
	{
		this.name = name;
		link.getLink().setBookName(id, name);
	}
	
	public int getLastPageNumber() throws DataLinkException
	{
		int max = 0;
		for (Integer pageNum : pagesByNumber.keySet())
			if (pageNum > max)
				max = pageNum;
		return max;
	}
	
	/**
	 * Retrieves the specified page. Pages are numbered from 1 to {@link #getLastPageNumber()} included.
	 * @param pageNum The page number.
	 * @return Returns
	 * @throws DataLinkException If an error occurs or the page does not exist.
	 */
	public Page getPage(int pageNum) throws DataLinkException
	{
		if (!pagesByNumber.containsKey(pageNum))
			throw new DataLinkException(link.getLink(), "Page "+pageNum+" does not exist in book "+id);
		Page page = pagesByNumber.get(pageNum);
		if (page == null)
		{
			page = new Page(this, pageNum, link.getLink().getPageData(id, pageNum));
			pagesByNumber.put(pageNum, page);
		}
		return page;
	}
	
	/**
	 * Adds a new page at the end of the book. If the book is empty, this page will become page number 1.
	 * @return The new page image.
	 * @throws DataLinkException
	 */
	public Page appendPage(ImageSource data) throws IOException, DataLinkException
	{
		int pageNum = getLastPageNumber()+1;
		Page page = new Page(this, pageNum, data);
		pagesByNumber.put(page.pageNum, page);
		return page;
	}
	
	/**
	 * Remove this book from the {@link ManuscriptLink}
	 * @throws DataLinkException
	 */
	public void remove() throws DataLinkException
	{
		removeAllMetaData();
		while (!pagesByNumber.isEmpty())
			removePage(pagesByNumber.keySet().iterator().next());
		link.getLink().removeBook(id);
		link.books.remove(id);
	}
	
	/**
	 * Deletes the specified page. All subsequent page numbers will be decremented by one.
	 * @param pageNum The page number to remove.
	 * @throws DataLinkException If an error occurs or if the specified page does not exist.
	 */
	public void removePage(int pageNum) throws DataLinkException
	{
		if (!pagesByNumber.containsKey(pageNum))
			throw new DataLinkException(link.getLink(), "Page "+pageNum+" does not exist in book "+id);
		
		int lastPage = getLastPageNumber();
		getPage(pageNum).removeAllMetaData();
		getPage(pageNum).removeAllRegions();
		link.getLink().removePage(id, pageNum);
		link.getLink().decreasePageNumbers(id, pageNum);
		
		for (int i=pageNum+1;i<=lastPage;i++)
		{
			if (!pagesByNumber.containsKey(i))
				continue;
			Page page = pagesByNumber.get(i);
			if (page != null)
				page.pageNum = i-1;
			pagesByNumber.put(i-1, page);
		}
		pagesByNumber.remove(lastPage);
	}
	
	/**
	 * Inserts a page at the specified position. All pages with a page number equal or greater will be incremented by one.
	 * @param pageNum The position of the page to insert.
	 * @return The new page.
	 * @throws DataLinkException
	 */
	public Page insertPage(int pageNum, ImageSource data) throws IOException, DataLinkException
	{
		link.getLink().increasePageNumbers(id, pageNum);
		
		int lastPage = getLastPageNumber();
		for (int i=lastPage;i>=pageNum;i--)
		{
			if (!pagesByNumber.containsKey(i))
				continue;
			Page page = pagesByNumber.get(i);
			if (page != null)
				page.pageNum = i+1;
			pagesByNumber.put(i+1, page);
		}
		
		Page newPage = new Page(this, pageNum, data);
		pagesByNumber.put(pageNum, newPage);
		return newPage;
	}
	
	/**
	 * Moves a page to any position from a specified position. All pages between from and to will be incremented
	 * or decremented depending on whether from is greater than to or not.
	 * @param from The page to move.
	 * @param to Where to move the page.
	 * @throws DataLinkException
	 */
	public void movePage(int from, int to) throws DataLinkException
	{
		link.getLink().movePage(id, from, to);
		Page move = pagesByNumber.get(from);
		
		int lastPage = getLastPageNumber();
		for (int i=from+1;i<=lastPage;i++)
		{
			if (!pagesByNumber.containsKey(i))
				continue;
			Page page = pagesByNumber.get(i);
			if (page != null)
				page.pageNum = i-1;
			pagesByNumber.put(i-1, page);
		}
		lastPage--;
		for (int i=lastPage;i>=to;i--)
		{
			if (!pagesByNumber.containsKey(i))
				continue;
			Page page = pagesByNumber.get(i);
			if (page != null)
				page.pageNum = i+1;
			pagesByNumber.put(i+1, page);
		}
		pagesByNumber.put(to, move);
		if (move != null)
			move.pageNum = to;
	}
	
	/**
	 * Reference to a book as a search result.
	 * @author Alexander Burnett
	 *
	 */
	public static class BookStub extends ObjectStub<Book>
	{
		public final String name;
		BookStub(ManuscriptLink link, String bookId, String name)
		{
			super(link, bookId);
			this.name = name;
		}
		
		public Book getObject() throws DataLinkException {return link.getBook(Integer.parseInt(objectId));}
	}
	
	/**
	 * Search a {@link ManuscriptLink} for books matching textual criteria.
	 * @param link The link to search.
	 * @param criteria A list of criteria. Each pair indicates a {@link MetaDataKey} and a search term.
	 * @param disjoint If set to true, at least one criterion must be met to create a match. Otherwise, all criteria must be met.
	 * @param relevance Relevance threshold, between 0 to 1.
	 * @return A buffer containing search matches.
	 * @throws DataLinkException
	 */
	public static ResultBuffer<Book> search(final ManuscriptLink link, List<Pair<MetaDataKey, String>> criteria, boolean disjoint, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = search(link, criteria, disjoint, "book", relevance);
		return new ResultBuffer<Book>(link, res, criteria)
		{
			@SuppressWarnings("unchecked")
			protected BookStub getStubFromId(String id) throws DataLinkException {return Book.getStub(link, id);}
		};
	}
	
	public static ResultBuffer<Book> search(final ManuscriptLink link, String term, double relevance) throws DataLinkException
	{
		Map<String, Double> res = search(link, term, "book", relevance);
		return new ResultBuffer<Book>(link, res, null)
		{
			@SuppressWarnings("unchecked")
			protected BookStub getStubFromId(String id) throws DataLinkException {return Book.getStub(link, id);}
		};
	}
	
	static BookStub getStub(ManuscriptLink link, String bookId) throws DataLinkException {return new BookStub(link, bookId, link.getLink().getBookTitle(Integer.parseInt(bookId)));}
	
}
