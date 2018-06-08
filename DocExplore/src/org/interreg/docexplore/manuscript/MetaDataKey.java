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
	
	@Override public int hashCode() {return id;}
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
