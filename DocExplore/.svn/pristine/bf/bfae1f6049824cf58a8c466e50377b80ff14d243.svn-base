package org.interreg.docexplore.datalink;

import java.awt.Point;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.datalink.objects.MetaDataData;
import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.datalink.objects.RegionData;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.util.Pair;

/**
 * The low-level interface of the manuscript persistence layer.
 * @author Alexander Burnett
 *
 */
public interface DataLink
{
	/**
	 * A source is a serializable object that generates a DataLink. A given source must always generate the same DataLink.
	 * @author Alexander Burnett
	 *
	 */
	public static interface DataLinkSource extends Serializable
	{
		public DataLink getDataLink();
		public String getDescription();
	}
	
	public DataLinkSource getSource();
	/**
	 * Indicates that this DataLink will no longer be used and any associated resources can be freed. The link can however be regenerated from the source.
	 */
	public void release();
	
	/**
	 * Sets an implementation dependent property to the given value.
	 * @param name
	 * @param value
	 * @throws DataLinkException
	 */
	public void setProperty(String name, Object value) throws DataLinkException;
	public boolean hasProperty(String name);
	public Object getProperty(String name);
	
	/**
	 * Books
	 */
	public List<Integer> getAllBookIds() throws DataLinkException;
	public BookData getBookData(int id) throws DataLinkException;
	public void setBookName(int id, String name) throws DataLinkException;
	public int addBook(String name) throws DataLinkException;
	public void removeBook(int bookId) throws DataLinkException;
	
	/**
	 * Pages
	 */
	public PageData getPageData(int bookId, int pageNum) throws DataLinkException;
	public int addPage(int bookId, int pageNum, InputStream data) throws DataLinkException;
	/**
	 * Deletes the specified page. Will delete all the associated regions of interest.
	 * @param bookId Id of a book.
	 * @param pageNum Number of the page to be deleted.
	 * @throws DataLinkException If an error occurs.
	 */
	public void removePage(int bookId, int pageNum) throws DataLinkException;
	/**
	 * Increases the number of each page after the specified page number included.
	 * @param bookId The id of the book that holds the pages.
	 * @param fromPageNum The lowest page number to be increased.
	 * @throws DataLinkException If an error occurs.
	 */
	public void increasePageNumbers(int bookId, int fromPageNum) throws DataLinkException;
	/**
	 * Decreases the number of each page after the specified page number included.
	 * @param bookId The id of the book that holds the pages.
	 * @param fromPageNum The lowest page number to be decreased.
	 * @throws DataLinkException If an error occurs.
	 */
	public void decreasePageNumbers(int bookId, int fromPageNum) throws DataLinkException;
	public void movePage(int bookId, int from, int to) throws DataLinkException;
	public void setPageImage(int pageId, int bookId, int pageNum, InputStream file) throws DataLinkException;
	public byte [] getPageImage(int pageId, int bookId, int pageNum) throws DataLinkException;
	
	/**
	 * Regions
	 */
	public RegionData getRegionData(int id, int bookId, int pageNum) throws DataLinkException;
	public int addRegion(int pageId, int bookId, int pageNum) throws DataLinkException;
	public void removeRegion(int regionId, int bookId, int pageNum) throws DataLinkException;
	public void setRegionOutline(int regionId, int bookId, int pageNum, Point [] outline) throws DataLinkException;
	
	/**
	 * Meta data
	 */
	public void addMetaDataToObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException;
	public void removeMetaDataFromObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException;
	/**
	 * Returns the name of a metadata key given its id and a language.
	 * @param language ISO-639 lower-case two letter code.
	 * @return The name of the key.
	 * @throws DataLinkException If an error occurs.
	 */
	public String getMetaDataKeyName(int id, String language) throws DataLinkException;
	public void setMetaDataKey(int objectId, int keyId) throws DataLinkException;
	public MetaDataData getMetaDataData(int metaDataId) throws DataLinkException;
	public InputStream getMetaDataValue(int metaDataId) throws DataLinkException;
	public File getMetaDataFile(int metaDataId) throws DataLinkException;
	public void setMetaDataValue(int metaDataId, InputStream stream) throws DataLinkException;
	public int addMetaData(int keyId, String type) throws DataLinkException;
	public void removeMetaData(int objectId) throws DataLinkException;
	/**
	 * Returns all the different string values within the set of metadata objects with key 'associatedKeyId' associated to metadata objects with key 'keyId'
	 * @param keyId
	 * @param associatedKeyId
	 * @return
	 * @throws DataLinkException
	 */
//	public List<Integer> getAssociatedMetaDataIds(int keyId, int associatedKeyId) throws DataLinkException;
	public List<Integer> getMetaDataIds(int keyId, String type) throws DataLinkException;
	
	/**
	 * Meta data keys
	 */
	public int getMetaDataKeyId(String name, String language) throws DataLinkException;
	public int addMetaDataKey() throws DataLinkException;
	public void setMetaDataKeyName(int keyId, String name, String language) throws DataLinkException;
	public List<Integer> getMetaDataKeyIds() throws DataLinkException;
	public void removeMetaDataKey(int keyId) throws DataLinkException;
	
	/**
	 * Search for objects that are associated to meta data with the given key and whose value contains the specified value among a specified sub set 
	 * of objects and restricted to the given type of objects.
	 * @param keyId The id of a meta data key.
	 * @param value A value to find within meta data string values.
	 * @param subSet A sub set of object identifiers to search in. A null sub set means all objects.
	 * @param objectType An object type to restrict to (either 'book', 'page', 'region', 'metadata' or null for all types).
	 * @param relevance Minimum relevance of matches.
	 * @return A list of relevances indexed by the identifier of the matching object.
	 * @throws DataLinkException If an error occurs.
	 */
	public Map<String, Double> search(int keyId, String value, Collection<String> subSet, String objectType, double relevance) throws DataLinkException;
	public String getBookTitle(int bookId) throws DataLinkException;
	public Pair<Integer, Integer> getPageBookIdAndNumber(String pageId) throws DataLinkException;
	public String getRegionPageId(String regionId) throws DataLinkException;
	/**
	 * Get the text meta data values if its key is within keyIds and it is associated to the object id.
	 * @param id Id of an object.
	 * @param keyIds Set of key ids for meta data.
	 * @return A list of text values (pair keyId, text).
	 * @throws DataLinkException If an error occurs.
	 */
	public List<Pair<Integer, String>> getMetaDataText(String id, Collection<Integer> keyIds) throws DataLinkException;
	
	public ActionProvider getActionProvider(DocExploreDataLink link);
	public boolean supportsHistory();
}
