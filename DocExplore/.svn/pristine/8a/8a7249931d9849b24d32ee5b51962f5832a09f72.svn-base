package org.interreg.docexplore.manuscript;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.MetaDataData;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

/**
 * Class representing annotations associated with an {@link AnnotatedObject}.
 * An annotation is characterized by a key {@link MetaDataKey}, a type and a value.
 * A type is a three letter {@link String}. Built-in types are "txt" and "img". Other types can be added through plugins.
 * @author Alexander Burnett
 *
 */
public class MetaData extends AnnotatedObject
{
	public static String textType = "txt";
	public static String imageType = "img";
	
	MetaDataKey key;
	String type;
	
	/**
	 * Load an existing annotation with the given id.
	 * @param link
	 * @param id
	 * @throws DataLinkException
	 */
	MetaData(ManuscriptLink link, int id) throws DataLinkException
	{
		this(link, id, link.getLink().getMetaDataData(id));
	}
	
	/**
	 * Load an existing annotation with the given id.
	 * @param link
	 * @param id
	 * @param data The annotation data fetched from the {@link ManuscriptLink}.
	 * @throws DataLinkException
	 */
	MetaData(ManuscriptLink link, int id, MetaDataData data) throws DataLinkException
	{
		super(link, id);
		this.key = link.getKey(data.keyId);
		this.type = data.type;
		
		fillMetaData(data);
		link.putMetaData(id, this);
	}
	
	/**
	 * Create an annotation of type text.
	 * @param link
	 * @param key The annotation key.
	 * @param value The initial value.
	 * @throws DataLinkException
	 */
	public MetaData(ManuscriptLink link, MetaDataKey key, String value) throws DataLinkException
	{
		super(link, link.getLink().addMetaData(key.id, textType));
		link.getLink().setMetaDataValue(id, new ByteArrayInputStream(value.getBytes()));
		this.key = key;
		this.type = textType;
		
		link.putMetaData(id, this);
	}
	/**
	 * Create an annotation.
	 * @param link
	 * @param key
	 * @param type The type of the annotation.
	 * @param value The initial value as an input stream. The stream will be read and closed if non errors occur.
	 * @throws DataLinkException
	 */
	public MetaData(ManuscriptLink link, MetaDataKey key, String type, InputStream value) throws DataLinkException
	{
		super(link, link.getLink().addMetaData(key.id, type));
		link.getLink().setMetaDataValue(id, value);
		try {value.close();} catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		this.key = key;
		this.type = type;
		
		link.putMetaData(id, this);
	}
	
	public MetaDataKey getKey() {return key;}
	public String getType() {return type;}
	
	/**
	 * Returns the text value of this annotation.
	 * @return
	 * @throws ClassCastException
	 * @throws DataLinkException If there is an error or the annotation is not a "txt" type.
	 */
	public String getString() throws ClassCastException, DataLinkException
	{
		if (!type.equals(textType))
			throw new ClassCastException("MetaData value is not a string (type "+type+")");
		try {return new String(ByteUtils.readStream(link.getLink().getMetaDataValue(id)));}
		catch (Exception e) {throw new DataLinkException(link.getLink(), e);}
	}
	/**
	 * Returns the image value of this annotation.
	 * @return
	 * @throws ClassCastException
	 * @throws DataLinkException If there is an error or the annotation is not a "img" type.
	 */
	public BufferedImage getImage() throws DataLinkException
	{
		try {return ImageUtils.read(getValue());}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	/**
	 * Returns the value of this annotation.
	 * @return
	 * @throws ClassCastException
	 * @throws DataLinkException
	 */
	public InputStream getValue() throws DataLinkException
	{
		return link.getLink().getMetaDataValue(id);
	}
	
	public void setKey(MetaDataKey key) throws DataLinkException
	{
		link.getLink().setMetaDataKey(id, key.id);
		this.key = key;
	}
	
	public void setString(String s) throws DataLinkException
	{
		setValue(textType, new ByteArrayInputStream(s.getBytes()));
	}
	public void setValue(String type, InputStream stream) throws DataLinkException
	{
		this.type = type;
		link.getLink().setMetaDataValue(id, stream);
		try {stream.close();}
		catch (Exception e) {throw new DataLinkException(link.getLink(), e);}
	}
	
	public void remove() throws DataLinkException
	{
		link.link.removeMetaData(id);
	}
	
	/**
	 * If the underlying {@link ManuscriptLink} is file system based, returns the file representing this annotation.
	 * @return The file representing this annotation or null if there is none.
	 * @throws DataLinkException
	 */
	public File getFile() throws DataLinkException
	{
		return link.getLink().getMetaDataFile(id);
	}
	
	/**
	 * Reference to an annotation as a search result.
	 * @author Alexander Burnett
	 *
	 */
	public static class MetaDataStub extends ObjectStub<MetaData>
	{
		MetaDataStub(ManuscriptLink link, String metaDataId)
		{
			super(link, metaDataId);
		}

		public MetaData getObject() throws DataLinkException
		{
			return link.getMetaData(Integer.parseInt(objectId));
		}
	}
	/**
	 * Search a {@link ManuscriptLink} for annotations matching textual criteria.
	 * @param link The link to search.
	 * @param criteria A list of criteria. Each pair indicates a {@link MetaDataKey} and a search term.
	 * @param disjoint If set to true, at least one criterion must be met to create a match. Otherwise, all criteria must be met.
	 * @param relevance Relevance threshold, between 0 to 1.
	 * @return A buffer containing search matches.
	 * @throws DataLinkException
	 */
	public static ResultBuffer<MetaData> search(final ManuscriptLink link, List<Pair<MetaDataKey, String>> criteria, boolean disjoint, double relevance) 
		throws DataLinkException
	{
		Map<String, Double> res = search(link, criteria, disjoint, "metadata", relevance);
		return new ResultBuffer<MetaData>(link, res, criteria)
		{
			@SuppressWarnings("unchecked")
			protected MetaDataStub getStubFromId(String id) throws DataLinkException {return MetaData.getStub(link, id);}
		};
	}
	static MetaDataStub getStub(ManuscriptLink link, String metaDataId) throws DataLinkException {return new MetaDataStub(link, metaDataId);}
}
