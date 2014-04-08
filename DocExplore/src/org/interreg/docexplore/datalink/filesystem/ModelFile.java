/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.interreg.docexplore.util.Pair;

public class ModelFile
{
	NavigableMap<Integer, ObjectFile> objects;
	Map<Integer, BookFile> books;
	Map<Integer, PageFile> pages;
	Map<Integer, RegionFile> regions;
	Map<Integer, MetaDataFile> metaDatas;
	NavigableMap<Integer, MetaDataKeyFile> metaDataKeys;
	
	public ModelFile(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this();
		
		@SuppressWarnings("unchecked")
		Collection<ObjectFile> objectSet = (Vector<ObjectFile>)ois.readObject();
		for (ObjectFile object : objectSet)
		{
			int id = object.id;
			objects.put(id, object);
			if (object instanceof BookFile)
				books.put(id, (BookFile)object);
			else if (object instanceof PageFile)
				pages.put(id, (PageFile)object);
			else if (object instanceof RegionFile)
				regions.put(id, (RegionFile)object);
			else if (object instanceof MetaDataFile)
				metaDatas.put(id, (MetaDataFile)object);
		}
		
		@SuppressWarnings("unchecked")
		Collection<MetaDataKeyFile> keySet = (Vector<MetaDataKeyFile>)ois.readObject();
		for (MetaDataKeyFile key : keySet)
			metaDataKeys.put(key.id, key);
	}
	
	public ModelFile()
	{
		this.objects = new TreeMap<Integer, ObjectFile>();
		this.books = new TreeMap<Integer, BookFile>();
		this.pages = new TreeMap<Integer, PageFile>();
		this.regions = new TreeMap<Integer, RegionFile>();
		this.metaDatas = new TreeMap<Integer, MetaDataFile>();
		this.metaDataKeys = new TreeMap<Integer, MetaDataKeyFile>();
	}
	
	public int nextObjectId()
	{
		if (objects.isEmpty())
			return 1;
		return objects.lastKey()+1;
	}
	public int nextMetaDataKeyId()
	{
		if (metaDataKeys.isEmpty())
			return 1;
		return metaDataKeys.lastKey()+1;
	}
	
	boolean autoWrite = true;
	public void write(File file) throws FileNotFoundException, IOException
	{
		if (autoWrite)
		{
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			write(oos);
			oos.close();
			fos.close();
		}
	}
	
	public void write(ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(new Vector<ObjectFile>(objects.values()));
		oos.writeObject(new Vector<MetaDataKeyFile>(metaDataKeys.values()));
	}
	
	public List<Integer> getBookPageNumbers(BookFile book)
	{
		Set<Integer> pageSet = new TreeSet<Integer>();
		for (PageFile page : pages.values())
			if (page.bookId == book.id)
				pageSet.add(page.pageNum);
		return new Vector<Integer>(pageSet);
	}
	
	public List<Integer> getPageRegions(PageFile page)
	{
		Set<Integer> regionSet = new TreeSet<Integer>();
		for (RegionFile region : regions.values())
			if (region.pageId == page.id)
				regionSet.add(region.id);
		return new Vector<Integer>(regionSet);
	}
	
	public List<Pair<Integer, Integer>> getMetadata(ObjectFile object)
	{
		List<Pair<Integer, Integer>> res = new Vector<Pair<Integer, Integer>>(object.metaData.size());
		for (int metaDataId : object.metaData)
		{
			MetaDataFile md = metaDatas.get(metaDataId);
			res.add(new Pair<Integer, Integer>(md.id, md.keyId));
		}
		return res;
	}
	
	public void addBook(BookFile book)
	{
		books.put(book.id, book);
		objects.put(book.id, book);
	}
	public void addPage(PageFile page)
	{
		pages.put(page.id, page);
		objects.put(page.id, page);
	}
	public void addRegion(RegionFile region)
	{
		regions.put(region.id, region);
		objects.put(region.id, region);
	}
	public void addMetaData(MetaDataFile metaData)
	{
		metaDatas.put(metaData.id, metaData);
		objects.put(metaData.id, metaData);
	}
	public void addMetaDataKey(MetaDataKeyFile metaDataKey)
	{
		metaDataKeys.put(metaDataKey.id, metaDataKey);
	}
	
	public void removeBook(int book)
	{
		books.remove(book);
		objects.remove(book);
	}
	public void removePage(int page)
	{
		pages.remove(page);
		objects.remove(page);
	}
	public void removeRegion(int region)
	{
		regions.remove(region);
		objects.remove(region);
	}
	public void removeMetaData(int metaData)
	{
		metaDatas.remove(metaData);
		objects.remove(metaData);
	}
	public void removeMetaDataKey(int metaDataKey)
	{
		metaDataKeys.remove(metaDataKey);
	}
}
