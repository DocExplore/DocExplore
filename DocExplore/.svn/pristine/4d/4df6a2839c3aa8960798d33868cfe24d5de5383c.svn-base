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
