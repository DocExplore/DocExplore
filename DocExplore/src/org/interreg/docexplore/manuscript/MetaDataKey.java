package org.interreg.docexplore.manuscript;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.interreg.docexplore.datalink.DataLinkException;

/**
 * A key for an annotation. A key can have different names for different languages.
 * @author Alexander Burnett
 *
 */
public class MetaDataKey extends ManuscriptObject
{
	Map<String, String> nameMap;
	
	/**
	 * Load an existing key in a page with the given id.
	 * @param link
	 * @param id
	 * @throws DataLinkException
	 */
	MetaDataKey(ManuscriptLink link, int id) throws DataLinkException
	{
		super(link, id);
		String language = Locale.getDefault().getLanguage();
		language = language.equals("") ? "en" : language;
		this.nameMap = new TreeMap<String, String>();
		link.keys.put(id, this);
	}
	
	/**
	 * Create a new key with the given name and language.
	 * @param link
	 * @param name
	 * @param language A two letter language code. "" defaults to "en".
	 * @throws DataLinkException
	 */
	MetaDataKey(ManuscriptLink link, String name, String language) throws DataLinkException
	{
		super(link, link.getLink().addMetaDataKey());
		language = language.equals("") ? "en" : language;
		link.getLink().setMetaDataKeyName(id, name, language);
		if (!language.equals("en"))
			link.getLink().setMetaDataKeyName(id, name, "en");
		this.nameMap = new TreeMap<String, String>();
		nameMap.put(language, name);
		link.keys.put(id, this);
	}
	
	public int getId() {return id;}
	
	/**
	 * Get the name of this key in a given language.
	 * @param language A two letter language code. "" defaults to "en".
	 * @return The name of the key or null if no name is defined in the given language.
	 * @throws DataLinkException
	 */
	public String getName(String language) throws DataLinkException
	{
		language = language.equals("") ? "en" : language;
		String name = nameMap.get(language);
		if (name == null)
		{
			name = link.getLink().getMetaDataKeyName(id, language);
			if (name != null)
				nameMap.put(language, name);
		}
		return name;
	}
	/**
	 * Get the name of this key in the current {@link Locale} language.
	 * @return
	 * @throws DataLinkException
	 */
	public String getName() throws DataLinkException {return getName(Locale.getDefault().getLanguage());}
	
	/**
	 * Set the name of this key in a given language.
	 * @param name
	 * @param language A two letter language code. "" defaults to "en".
	 * @throws DataLinkException
	 */
	public void setName(String name, String language) throws DataLinkException
	{
		language = language.equals("") ? "en" : language;
		link.getLink().setMetaDataKeyName(id, name, language);
		nameMap.put(language, name);
	}
	/**
	 * Set the name of this key in the current {@link Locale} language.
	 * @param name
	 * @throws DataLinkException
	 */
	public void setName(String name) throws DataLinkException {setName(name, Locale.getDefault().getLanguage());}
	
	public String getBestName() throws DataLinkException
	{
		String name = getName();
		if (name == null || name.length() == 0)
			name = getName("");
		return name;
	}
	
	/**
	 * Gets all the textual annotations in the {@link ManuscriptLink} with this key.
	 * @return
	 * @throws DataLinkException
	 */
	public Set<MetaData> getMetaData(String type) throws DataLinkException
	{
		Set<MetaData> mds = new TreeSet<MetaData>();
		List<Integer> ids = link.getLink().getMetaDataIds(id, type);
		for (int id : ids)
			mds.add(link.getMetaData(id));
		return mds;
	}
	public List<Integer> getMetaDataIds(String type) throws DataLinkException
	{
		return link.getLink().getMetaDataIds(id, type);
	}
}
