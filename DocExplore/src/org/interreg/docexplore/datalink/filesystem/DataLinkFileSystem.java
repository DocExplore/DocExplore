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

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.datalink.objects.MetaDataData;
import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.datalink.objects.RegionData;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.manuscript.actions.DataLinkActionProvider;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.search.FuzzySearch;

public class DataLinkFileSystem implements DataLink
{
	public static class DataLinkFileSystemSource implements DataLink.DataLinkSource
	{
		private static final long serialVersionUID = 1353591802115395526L;
		private String file;
		
		@SuppressWarnings("unused")
		private DataLinkFileSystemSource() {this.file = null;}
		public DataLinkFileSystemSource(String file)
		{
			this.file = file;
		}
		
		private DataLinkFileSystem link = null;
		public DataLink getDataLink()
		{
			if (link == null) try {link = new DataLinkFileSystem(this);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			return link;
		}
		
		private File getFile() {return new File(file);}

		public String getDescription() {return "File system '"+file+"'";}
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {out.writeUTF(file);}
		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {this.file = in.readUTF();}
		
		public boolean equals(Object o)
		{
			if (o instanceof DataLinkFileSystemSource)
				return file.equals(((DataLinkFileSystemSource)o).file);
			return false;
		}
	}
	
	public final DataLinkFileSystemSource source;
	File modelFile;
	final ModelFile model;
	
	public DataLinkFileSystem(DataLinkFileSystemSource source) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		this.modelFile = source.getFile();
		if (!modelFile.exists())
		{
			this.model = new ModelFile();
			//model.write(modelFile);
		}
		else
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile));
			this.model = new ModelFile(ois);
			ois.close();
		}
		this.source = source;
	}
	
	public void setFile(File file)
	{
		modelFile = file;
		source.file = file.getAbsolutePath();
	}
	
	public DataLinkSource getSource() {return source;}
	
	public void release() {source.link = null;}
	
	public void setProperty(String name, Object value) throws DataLinkException
	{
		if (name.equals("autoWrite"))
		{
			model.autoWrite = (Boolean)value;
			if (model.autoWrite)
				try {model.write(modelFile);}
				catch (IOException e) {throw new DataLinkException(this, "Error writing file '"+modelFile.getAbsolutePath()+"'", e);}
		}
	}
	public boolean hasProperty(String name) {return name.equals("autoWrite");}
	public Object getProperty(String name) {return name.equals("autoWrite") ? model.autoWrite : null;}

	public List<Integer> getAllBookIds() throws DataLinkException
	{
		List<Integer> res = new LinkedList<Integer>();
		res.addAll(model.books.keySet());
		return res;
	}

	public BookData getBookData(int id) throws DataLinkException
	{
		BookFile book = model.books.get(id);
		if (book == null)
			throw new DataLinkException(this, "No such book "+id);
		return new BookData(book.name, model.getBookPageNumbers(book), model.getMetadata(book));
	}
	
	public void setBookName(int id, String name) throws DataLinkException
	{
		model.books.get(id).name = name;
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error setting name for book "+id, e);}
	}

	public int addBook(String name) throws DataLinkException
	{
		int id = model.nextObjectId();
		model.addBook(new BookFile(id, name));
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error creating book "+name, e);}
		return id;
	}

	public PageData getPageData(int bookId, int pageNum) throws DataLinkException
	{
		for (PageFile page : model.pages.values())
			if (page.bookId == bookId && page.pageNum == pageNum)
				return new PageData(page.id, model.getPageRegions(page), model.getMetadata(page));
		throw new DataLinkException(this, "Page "+pageNum+" doesn't exist in book "+bookId);
	}

	public int addPage(int bookId, int pageNum, InputStream data) throws DataLinkException
	{
		int id = model.nextObjectId();
		try
		{
			model.addPage(new PageFile(id, bookId, ByteUtils.readStream(data), pageNum));
			model.write(modelFile);
		}
		catch (Exception e) {throw new DataLinkException(this, "Error creating page "+pageNum+" in book "+bookId, e);}
		return id;
	}
	
	public void removeBook(int bookId) throws DataLinkException
	{
		model.removeBook(bookId);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error removing book "+bookId, e);}
	}

	public void removePage(int bookId, int pageNum) throws DataLinkException
	{
		PageFile page = null;
		for (PageFile pf : model.pages.values())
			if (pf.bookId == bookId && pf.pageNum == pageNum)
				{page = pf; break;}
		if (page == null)
			throw new DataLinkException(this, "Page "+pageNum+" doesn't exist in book "+bookId+" in book "+bookId);
		model.removePage(page.id);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error removing page "+pageNum+" in book "+bookId, e);}
	}

	public void increasePageNumbers(int bookId, int fromPageNum) throws DataLinkException
	{
		for (PageFile pf : model.pages.values())
			if (pf.bookId == bookId && pf.pageNum >= fromPageNum)
				pf.pageNum++;
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error increasing pages in book "+bookId, e);}
	}

	public void decreasePageNumbers(int bookId, int fromPageNum) throws DataLinkException
	{
		for (PageFile pf : model.pages.values())
			if (pf.bookId == bookId && pf.pageNum >= fromPageNum)
				pf.pageNum--;
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error decreasing pages in book "+bookId, e);}
	}

	public void movePage(int bookId, int from, int to) throws DataLinkException
	{
		for (PageFile pf : model.pages.values())
			if (pf.bookId == bookId && pf.pageNum == from)
				{pf.pageNum = 0; break;}
		decreasePageNumbers(bookId, from);
		increasePageNumbers(bookId, to);
		for (PageFile pf : model.pages.values())
			if (pf.bookId == bookId && pf.pageNum == 0)
				{pf.pageNum = to; break;}
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error moving page "+from+" in book "+bookId, e);}
	}

	public void setPageImage(int pageId, int bookId, int pageNum, InputStream file) throws DataLinkException
	{
		try
		{
			model.pages.get(pageId).data = ByteUtils.readStream(file);
			model.write(modelFile);
		}
		catch (Exception e) {throw new DataLinkException(this, "Error setting page "+pageId+" image", e);}
	}

	public byte[] getPageImage(int pageId, int bookId, int pageNum) throws DataLinkException
	{
		return model.pages.get(pageId).data;
	}

	public RegionData getRegionData(int id, int bookId, int pageNum) throws DataLinkException
	{
		RegionFile region = model.regions.get(id);
		return new RegionData(region.getOutline(), model.getMetadata(region));
	}

	public int addRegion(int pageId, int bookId, int pageNum) throws DataLinkException
	{
		int id = model.nextObjectId();
		model.addRegion(new RegionFile(id, pageId, new int [0][2]));
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error adding region", e);}
		return id;
	}

	public void removeRegion(int regionId, int bookId, int pageNum) throws DataLinkException
	{
		model.removeRegion(regionId);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error adding region", e);}
	}

	public void setRegionOutline(int regionId, int bookId, int pageNum, Point [] outline) throws DataLinkException
	{
		RegionFile region = model.regions.get(regionId);
		region.outline = new int [outline.length][2];
		for (int i=0;i<outline.length;i++)
			{region.outline[i][0] = outline[i].x; region.outline[i][1] = outline[i].y;}
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error seeting outline in region "+regionId, e);}
	}

	public void addMetaDataToObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException
	{
		ObjectFile object = model.objects.get(objectId);
		MetaDataFile metaData = model.metaDatas.get(metaDataId);
		object.metaData.add(metaDataId);
		metaData.objects.add(objectId);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error adding metadata "+metaDataId+" to object "+objectId, e);}
	}

	public void removeMetaDataFromObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException
	{
		ObjectFile object = model.objects.get(objectId);
		MetaDataFile metaData = model.metaDatas.get(metaDataId);
		object.metaData.remove(metaDataId);
		metaData.objects.remove(objectId);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error removing metadata "+metaDataId+" to object "+objectId, e);}
	}

	public String getMetaDataKeyName(int id, String language) throws DataLinkException
	{
		return model.metaDataKeys.get(id).names.get(language);
	}

	public void setMetaDataKey(int objectId, int keyId) throws DataLinkException
	{
		model.metaDatas.get(objectId).keyId = keyId;
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error setting metadata key on"+objectId, e);}
	}

	public MetaDataData getMetaDataData(int metaDataId) throws DataLinkException
	{
		MetaDataFile metaData = model.metaDatas.get(metaDataId);
		return new MetaDataData(metaData.keyId, metaData.type, model.getMetadata(metaData));
	}

	public InputStream getMetaDataValue(int metaDataId) throws DataLinkException
	{
		return new ByteArrayInputStream(model.metaDatas.get(metaDataId).value);
	}
	
	public File getMetaDataFile(int metaDataId) throws DataLinkException
	{
		return null;
	}

	public void setMetaDataValue(int metaDataId, InputStream stream) throws DataLinkException
	{
		try
		{
			model.metaDatas.get(metaDataId).value = ByteUtils.readStream(stream);
			model.write(modelFile);
		}
		catch (Exception e) {throw new DataLinkException(this, "Error setting metadata value on"+metaDataId, e);}
	}

	public int addMetaData(int keyId, String type) throws DataLinkException
	{
		int id = model.nextObjectId();
		model.addMetaData(new MetaDataFile(id, keyId, type, null));
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error adding metaData", e);}
		return id;
	}

	public void removeMetaData(int objectId) throws DataLinkException
	{
		model.removeMetaData(objectId);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error removing metaData "+objectId, e);}
	}

	public List<Integer> getAssociatedMetaDataIds(int keyId, int associatedKeyId) throws DataLinkException
	{
		Set<Integer> idSet = new TreeSet<Integer>();
		for (MetaDataFile metaData : model.metaDatas.values())
			if (metaData.keyId == keyId)
				for (int associatedId : metaData.metaData)
				{
					MetaDataFile associated = model.metaDatas.get(associatedId);
					if (associated.keyId == associatedKeyId)
						idSet.add(associated.id);
				}
		return new Vector<Integer>(idSet);
	}

	public int getMetaDataKeyId(String name, String language) throws DataLinkException
	{
		for (MetaDataKeyFile key : model.metaDataKeys.values())
			if (key.names.containsKey(language) && key.names.get(language).equals(name))
				return key.id;
		return -1;
	}

	public int addMetaDataKey() throws DataLinkException
	{
		int id = model.nextMetaDataKeyId();
		model.addMetaDataKey(new MetaDataKeyFile(id));
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error adding metaData key", e);}
		return id;
	}

	public void setMetaDataKeyName(int keyId, String name, String language) throws DataLinkException
	{
		model.metaDataKeys.get(keyId).names.put(language, name);
		try {model.write(modelFile);}
		catch (Exception e) {throw new DataLinkException(this, "Error setting name for metaData key "+keyId, e);}
	}

	public List<Integer> getMetaDataKeyIds() throws DataLinkException
	{
		return new Vector<Integer>(model.metaDataKeys.keySet());
	}

	public Map<String, Double> search(int keyId, String value, Collection<String> _subSet, String objectType, double relevance) throws DataLinkException
	{
		Map<String, Double> res = new TreeMap<String, Double>();
		
		Collection<Integer> subSet = null;
		if (_subSet != null)
		{
			subSet = new Vector<Integer>(_subSet.size());
			for (String s : _subSet)
				subSet.add(Integer.parseInt(s));
		}
		
		if (subSet == null)
			subSet = new Vector<Integer>(objectType == null ? model.objects.keySet() : 
				objectType.equals("book") ? model.books.keySet() :
				objectType.equals("page") ? model.pages.keySet() :
				objectType.equals("region") ? model.regions.keySet() :
				objectType.equals("metadata") ? model.metaDatas.keySet() : null);
		
		String term = value.toLowerCase();
		for (int objectId : subSet)
		{
			ObjectFile object = model.objects.get(objectId);
			double score = 0;
			for (int metaDataId : object.metaData)
			{
				MetaDataFile metaData = model.metaDatas.get(metaDataId);
				if (metaData.keyId == keyId && metaData.type.equals("txt"))
				{
					List<int []> searchRes = FuzzySearch.process(term, new String(metaData.value).toLowerCase(), 1);
					for (int [] occ : searchRes)
					{
						double val = occ[2]*1./term.length();
						if (val > score)
							score = val;
					}
				}
			}
			score = score > 1 ? 1 : score;
			if (score > relevance)
				res.put(""+objectId, score);
		}
		
		return res;
	}

	public String getBookTitle(int bookId) throws DataLinkException
	{
		return model.books.get(bookId).name;
	}

	public Pair<Integer, Integer> getPageBookIdAndNumber(String pageId) throws DataLinkException
	{
		PageFile page = model.pages.get(Integer.parseInt(pageId));
		return new Pair<Integer, Integer>(page.bookId, page.pageNum);
	}

	public String getRegionPageId(String regionId) throws DataLinkException
	{
		return ""+model.regions.get(Integer.parseInt(regionId)).pageId;
	}

	public List<Pair<Integer, String>> getMetaDataText(String id, Collection<Integer> keyIds) throws DataLinkException
	{
		List<Pair<Integer, String>> res = new LinkedList<Pair<Integer,String>>();
		for (int metaDataId : model.objects.get(Integer.parseInt(id)).metaData)
		{
			MetaDataFile metaData = model.metaDatas.get(metaDataId);
			if (metaData.type.equals("txt") && keyIds.contains(metaData.keyId))
				res.add(new Pair<Integer, String>(metaData.keyId, new String(metaData.value)));
		}
		return res;
	}

	public List<Integer> getMetaDataIds(int keyId, String type) throws DataLinkException
	{
		List<Integer> res = new LinkedList<Integer>();
		for (MetaDataFile metaData : model.metaDatas.values())
			if (metaData.keyId == keyId && (type == null || metaData.type.equals(type)))
				res.add(metaData.id);
		return res;
	}
	
	public ActionProvider getActionProvider(DocExploreDataLink link) {return new DataLinkActionProvider(link);}
	public boolean supportsHistory() {return false;}

	public void removeMetaDataKey(int keyId) throws DataLinkException
	{
		model.metaDataKeys.remove(keyId);
	}
}
