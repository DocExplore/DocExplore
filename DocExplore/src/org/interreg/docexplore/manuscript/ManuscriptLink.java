package org.interreg.docexplore.manuscript;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;

/**
 * The high-level interface with the persistence layer for manuscripts which wraps around a {@link DataLink}, the low-level interface.
 * @author Alexander Burnett
 *
 */
public class ManuscriptLink
{
	protected DataLink link;
	
	/**
	 * Creates a new link wrapped around the given {@link DataLink}.
	 * @param link
	 */
	public ManuscriptLink(DataLink link)
	{
		this.link = link;
	}
	
	public synchronized DataLink getLink() {return link;}
	
	/**
	 * Clears the static cache of manuscript objects.
	 * Previously created persistent objects might become invalid.
	 */
	protected synchronized void clearPersistentObjects()
	{
		books.clear();
		keys.clear();
		metaDatas.clear();
	}
	
	public Map<Integer, Book> books = new TreeMap<Integer, Book>();
	/**
	 * Get a {@link Book} instance by id.
	 * @param id
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized Book getBook(int id) throws DataLinkException
	{
		Book book = books.get(id);
		if (book == null)
			book = new Book(this, id);
		return book;
	}
	
	TreeMap<Integer, MetaDataKey> keys = new TreeMap<Integer, MetaDataKey>();
	/**
	 * Get an existing {@link MetaDataKey} by id.
	 * @param id
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized MetaDataKey getKey(int id) throws DataLinkException
	{
		MetaDataKey key = keys.get(id);
		if (key == null)
			key = new MetaDataKey(this, id);
		return key;
	}
	/**
	 * Get an existing {@link MetaDataKey} by name and language.
	 * @param name
	 * @param language A two letter language code. "" defaults to "en".
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized MetaDataKey getKey(String name, String language) throws DataLinkException
	{
		language = language.equals("") ? "en" : language;
		int id = link.getMetaDataKeyId(name, language);
		if (id < 0)
		{
			if (!language.equals("en"))
				id = link.getMetaDataKeyId(name, "en");
			if (id < 0)
				return null;
		}
		return getKey(id);
	}
	/**
	 * Get an existing {@link MetaDataKey} by its name in the current {@link Locale}.
	 * @param name
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized MetaDataKey getKey(String name)  throws DataLinkException
	{
		return getKey(name, Locale.getDefault().getLanguage());
	}
	/**
	 * Get a {@link MetaDataKey} if it exists or create it if doesn't.
	 * @param name
	 * @param language A two letter language code. "" defaults to "en".
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized MetaDataKey getOrCreateKey(String name, String language) throws DataLinkException
	{
		language = language.equals("") ? "en" : language;
		MetaDataKey key = getKey(name, language);
		if (key == null)
		{
			if (!language.equals("en"))
				key = getKey(name, "en");
			if (key == null)
			{
				key = new MetaDataKey(this, name, language);
				if (!language.equals("en"))
					key.setName(name, "en");
			}
		}
		return key;
	}
	/**
	 * Get a {@link MetaDataKey} if it exists or create it if doesn't, in the current {@link Locale}.
	 * @param name
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized MetaDataKey getOrCreateKey(String name) throws DataLinkException
	{
		return getOrCreateKey(name, Locale.getDefault().getLanguage());
	}
	
	/**
	 * Gets all the keys in this link.
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized Collection<MetaDataKey> getAllKeys() throws DataLinkException
	{
		List<Integer> keyIds = link.getMetaDataKeyIds();
		for (Integer keyId : keyIds)
			if (!keys.containsKey(keyId))
				getKey(keyId);
		return keys.values();
	}
	
	Map<Integer, MetaData> metaDatas = new TreeMap<Integer, MetaData>();
	/**
	 * Get a {@link MetaData} instance by id.
	 * @param id
	 * @return
	 * @throws DataLinkException
	 */
	public synchronized MetaData getMetaData(int id) throws DataLinkException
	{
		MetaData metaData = metaDatas.get(id);
		if (metaData == null)
			metaData = new MetaData(this, id);
		return metaData;
	}
	synchronized void putMetaData(int id, MetaData metaData)
	{
		metaDatas.put(id, metaData);
	}
	
	/**
	 * Set a property of this ManuscriptLink. Properties are implementation dependant. The default implementation sets the property in the underlying {@link DataLink}.
	 * @param name
	 * @param value
	 * @throws DataLinkException
	 */
	public synchronized void setProperty(String name, Object value) throws DataLinkException {link.setProperty(name, value);}
	public synchronized Object getProperty(String name) throws DataLinkException {return link.getProperty(name);}
	
	public synchronized void deleteKey(MetaDataKey key) throws DataLinkException
	{
		link.removeMetaDataKey(key.getId());
		keys.remove(key.getId());
	}
}
