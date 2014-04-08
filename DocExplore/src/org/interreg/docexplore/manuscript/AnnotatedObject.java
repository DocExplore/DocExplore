/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.ObjectData;
import org.interreg.docexplore.util.Pair;

/**
 * Base class for the {@link Book}, {@link Page}, {@link Region} and {@link MetaData} classes. Annotated objects have a set of {@link MetaData} objects associated to them.
 * @author Alexander Burnett
 */
public abstract class AnnotatedObject extends ManuscriptObject
{
	Map<MetaDataKey, Map<Integer, MetaData>> metaDataByKey;
	
	/**
	 * Loads an existing AnnotatedObject from a {@link ManuscriptLink} with the given id.
	 * @param link 
	 * @param objectId
	 */
	AnnotatedObject(ManuscriptLink link, int objectId)
	{
		super(link, objectId);
		this.metaDataByKey = new TreeMap<MetaDataKey, Map<Integer, MetaData>>();
	}
	
	/**
	 * Creates a new AnnotatedObject and persists it in the {@link ManuscriptLink}.
	 * @param link
	 */
	AnnotatedObject(ManuscriptLink link)
	{
		super(link);
		this.metaDataByKey = new TreeMap<MetaDataKey, Map<Integer, MetaData>>();
	}
	
	/**
	 * Loads the {@link MetaData} objects contained in an {@link ObjectData}. 
	 * @param data
	 * @throws DataLinkException
	 */
	protected void fillMetaData(ObjectData data) throws DataLinkException
	{
		for (Pair<Integer, Integer> pair : data.metaData)
		{
			MetaDataKey key = link.getKey(pair.second);
			Map<Integer, MetaData> keyMap = metaDataByKey.get(key);
			if (keyMap == null)
			{
				keyMap = new TreeMap<Integer, MetaData>();
				metaDataByKey.put(key, keyMap);
			}
			keyMap.put(pair.first, null);
		}
	}
	
	/**
	 * Returns the annotations associated with this AnnotatedObject.
	 * @return The set of annotations indexed by their {@link MetaDataKey}.
	 * @throws DataLinkException
	 */
	public Map<MetaDataKey, List<MetaData>> getMetaData() throws DataLinkException
	{
		Map<MetaDataKey, List<MetaData>> result = new TreeMap<MetaDataKey, List<MetaData>>();
		
		for (Map.Entry<MetaDataKey, Map<Integer, MetaData>> entry : metaDataByKey.entrySet())
			result.put(entry.getKey(), getMetaDataListForKey(entry.getKey()));
		
		return result;
	}
	
	/**
	 * Returns the annotations with a given key associated with this AnnotatedObject. This method will lazily initialize the annotations.
	 * @param key
	 * @return
	 * @throws DataLinkException
	 */
	public List<MetaData> getMetaDataListForKey(MetaDataKey key) throws DataLinkException
	{
		List<MetaData> metaDatas = new LinkedList<MetaData>();
		Map<Integer, MetaData> map = metaDataByKey.get(key);
		if (map != null)
			for (Map.Entry<Integer, MetaData> entry : map.entrySet())
		{
			if (entry.getValue() == null)
				entry.setValue(link.getMetaData(entry.getKey()));
			metaDatas.add(entry.getValue());
		}
		
		return metaDatas;
	}
	
	/**
	 * Adds an annotation to this AnnotatedObject.
	 * @param metaData
	 * @throws DataLinkException
	 */
	public void addMetaData(MetaData metaData) throws DataLinkException
	{
		Map<Integer, MetaData> map = metaDataByKey.get(metaData.key);
		if (map == null)
		{
			map = new TreeMap<Integer, MetaData>();
			metaDataByKey.put(metaData.key, map);
		}
		map.put(metaData.id, metaData);
		
		if (this instanceof Book)
			link.getLink().addMetaDataToObject(id, id, -1, -1, metaData.id);
		else if (this instanceof Page)
			link.getLink().addMetaDataToObject(id, ((Page)this).book.id, ((Page)this).pageNum, -1, metaData.id);
		else if (this instanceof Region)
			link.getLink().addMetaDataToObject(id, ((Region)this).page.book.id, ((Region)this).page.pageNum, ((Region)this).id, metaData.id);
		else link.getLink().addMetaDataToObject(id, -1, -1, -1, metaData.id);
	}
	
	/**
	 * Removes an annotation from this AnnotatedObject.
	 * @param metaData
	 * @throws DataLinkException
	 */
	public void removeMetaData(MetaData metaData) throws DataLinkException
	{
		Map<Integer, MetaData> map = metaDataByKey.get(metaData.key);
		if (!map.containsKey(metaData.id))
			throw new DataLinkException(link.getLink(), "Object "+id+" doesn't contain metadata "+metaData.id);
		map.remove(metaData.id);
		
		if (this instanceof Book)
			link.getLink().removeMetaDataFromObject(id, id, -1, -1, metaData.id);
		else if (this instanceof Page)
			link.getLink().removeMetaDataFromObject(id, ((Page)this).book.id, ((Page)this).pageNum, -1, metaData.id);
		else if (this instanceof Region)
			link.getLink().removeMetaDataFromObject(id, ((Region)this).page.book.id, ((Region)this).page.pageNum, ((Region)this).id, metaData.id);
		else link.getLink().removeMetaDataFromObject(id, -1, -1, -1, metaData.id);
	}
	
	/**
	 * Check whether the keys of the annotations are valid.
	 * @throws DataLinkException If an invalid annotation key is found.
	 */
	public void check() throws DataLinkException
	{
		Map<MetaDataKey, List<MetaData>> metaData = getMetaData();
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : metaData.entrySet())
			for (MetaData data : entry.getValue())
				if (entry.getKey() != data.getKey())
					throw new DataLinkException(link.getLink(), "corrupt! "+
						entry.getKey().id+" ("+entry.getKey().getName("")+") - "+data.getKey().id+" ("+data.getKey().getName("")+")");
	}
	
	/**
	 * Removes all the annotations from this AnnotatedObject.
	 * @throws DataLinkException
	 */
	public void removeAllMetaData() throws DataLinkException
	{
		Map<MetaDataKey, List<MetaData>> metaData = getMetaData();
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : metaData.entrySet())
			for (MetaData data : entry.getValue())
				removeMetaData(data);
	}
	
	/**
	 * Builds a URI identifying this object. The URI can be used with {@link #resolveUri(ManuscriptLink, String)}.
	 * @return
	 */
	public String getCanonicalUri()
	{
		if (this instanceof Book)
			return "docex://"+((Book)this).id;
		if (this instanceof Page)
		{
			Page page = (Page)this;
			return page.book.getCanonicalUri()+"/p"+page.pageNum;
		}
		if (this instanceof Region)
		{
			Region region = (Region)this;
			return region.page.getCanonicalUri()+"/"+region.id;
		}
		if (this instanceof MetaData)
		{
			MetaData metaData = (MetaData)this;
			return "docex://?"+metaData.id;
		}
		return null;
	}
	
	
	static String getNextField(String subUri)
	{
		int delim = subUri.indexOf("/");
		if (delim < 0)
			return subUri;
		return subUri.substring(0, delim);
	}
	static Book resolveBookField(ManuscriptLink link, String field)
		throws DataLinkException
	{
		if (field.startsWith("\""))
		{
			if (!field.endsWith("\""))
				throw new DataLinkException(link.getLink(), "Malformed URI bookField : "+field);
			String title = field.substring(1, field.length()-1);
			throw new DataLinkException(link.getLink(), "Unsupported feature : book title in URI ("+
				title+")");
		}
		else
		{
			int id = Integer.parseInt(field);
			return link.getBook(id);
		}
	}
	static Page resolvePageField(Book book, String field)
		throws DataLinkException
	{
		if (field.startsWith("p"))
		{
			int pageNum = Integer.parseInt(field.substring(1));
			return book.getPage(pageNum);
		}
		else
		{
			//throw new DataLinkException(book.link.getLink(), "Unsupported feature : page id in URI (page "+field+" in book "+book.id+")");
			int id = Integer.parseInt(field);
			return book.getPage(book.getLink().getLink().getPageBookIdAndNumber(""+id).second);
		}
	}
	static Region resolveRegionField(Page page, String field)
		throws DataLinkException
	{
		int id = Integer.parseInt(field);
		Set<Region> regions = page.getRegions();
		for (Region region : regions)
			if (region.id == id)
				return region;
		throw new DataLinkException(page.link.getLink(), "Unknown region id "+id);
	}
	
	/**
	 * Retrieves an AnnotatedObject from a URI in the given {@link ManuscriptLink}.
	 * @param link
	 * @param uri
	 * @return
	 * @throws DataLinkException If the URI is invalid or an error occurs.
	 */
	public static AnnotatedObject resolveUri(ManuscriptLink link, String uri) 
		throws DataLinkException
	{
		uri = uri.trim();
		if (!uri.startsWith("docex://"))
			throw new DataLinkException(link.getLink(), "malformed URI : "+uri);
		uri = uri.substring("docex://".length());
		
		String bookField = getNextField(uri);
		if (bookField == uri)
			uri = "";
		else uri = uri.substring(bookField.length()+1);
		Book book = resolveBookField(link, bookField);
		if (uri.length() == 0)
			return book;
		
		String pageField = getNextField(uri);
		if (pageField == uri)
			uri = "";
		else uri = uri.substring(pageField.length()+1);
		Page page = resolvePageField(book, pageField);
		if (uri.length() == 0)
			return page;
		
		String regionField = getNextField(uri);
		if (regionField == uri)
			uri = "";
		else uri = uri.substring(regionField.length()+1);
		Region region = resolveRegionField(page, regionField);
		if (uri.length() == 0)
			return region;
		
		throw new DataLinkException(link.getLink(), "Unsupported feature : metadata URI");
	}
	
	/**
	 * Unloads the annotations that were initialized so they can be finalized.
	 * TODO: replace mechanism with reference counting.
	 */
	public void unloadMetaData()
	{
		for (Map.Entry<MetaDataKey, Map<Integer, MetaData>> entry : metaDataByKey.entrySet())
			if (entry.getValue() != null)
				for (Map.Entry<Integer, MetaData> entry2 : entry.getValue().entrySet())
					entry2.setValue(null);
	}
	
	/**
	 * Base class of the stubs used to retrieve search results. A stub is a reference to an {@link AnnotatedObject}.
	 * @author Alexander Burnett
	 *
	 * @param <ObjectType> The type of AnnotatedObject referenced by a stub class.
	 */
	public static abstract class ObjectStub<ObjectType extends AnnotatedObject>
	{
		ManuscriptLink link;
		public final String objectId;
		
		ObjectStub(ManuscriptLink link, String objectId)
		{
			this.link = link;
			this.objectId = objectId;
		}
		
		public abstract ObjectType getObject() throws DataLinkException;
	}
	
	/**
	 * Search a {@link ManuscriptLink} based on textual criteria. This method is called by subclasses of AnnotatedObject.
	 * @param link The link to search.
	 * @param criteria A list of criteria. Each pair indicates a {@link MetaDataKey} and a search term.
	 * @param disjoint If set to true, at least one criterion must be met to create a match. Otherwise, all criteria must be met.
	 * @param objectType The type of manuscript object to return.
	 * @param relevance Relevance threshold, between 0 to 1.
	 * @return A set of result relevances indexed by object ids.
	 * @throws DataLinkException
	 */
	static Map<String, Double> search(ManuscriptLink link, List<Pair<MetaDataKey, String>> criteria, boolean disjoint, String objectType, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = new TreeMap<String, Double>();
		Map<String, Double> subSet = null;
		
		for (Pair<MetaDataKey, String> criterion : criteria)
		{
			if (!disjoint && subSet!=null && subSet.isEmpty())
				break;
			subSet = link.getLink().search(criterion.first.id, criterion.second, 
				disjoint ? null : (subSet == null ? null : subSet.keySet()), 
				objectType, relevance);
			if (disjoint)
				res.putAll(subSet);
		}
		if (!disjoint && subSet!=null)
			res.putAll(subSet);
		
		return res;
	}
	
	static Map<String, Double> search(ManuscriptLink link, String term, String objectType, double relevance) throws DataLinkException
	{
		return link.getLink().search(-1, term, null, objectType, relevance);
	}
}
