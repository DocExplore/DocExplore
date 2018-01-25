/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.fs2;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.datalink.objects.MetaDataData;
import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.datalink.objects.RegionData;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;

public class DataLinkFS2 implements DataLink
{
	public final DataLinkFS2Source source;
	File root;
	
	public DataLinkFS2(DataLinkFS2Source source)
	{
		this.source = source;
		this.root = source.getFile();
		if (!root.exists())
			root.mkdirs();
	}
	
	public synchronized DataLinkSource getSource() {return source;}

	public synchronized void release() {source.link = null;}

	public synchronized void setProperty(String name, Object value) throws DataLinkException {}
	public synchronized boolean hasProperty(String name) {return false;}
	public synchronized Object getProperty(String name) {return null;}
	
	public void setFile(File file) throws IOException
	{
		if (file.getAbsolutePath().equals(root.getAbsolutePath()))
			return;
		if (file.exists())
			FS2Utils.deleteRoot(file);
		file.mkdirs();
		FS2Utils.copy(root, file);
		if (!file.isAbsolute())
			file = new File(DocExploreTool.getHomeDir(), file.getPath());
		root = file;
		System.out.println("Using FS2 data link: "+root.getPath());
		source.file = root.getAbsolutePath();
	}
	public File getFile() {return root;}
	
	public synchronized static int getNextId(File root) throws IOException
	{
		File file = new File(root, "index.xml");
		String data = "";
		if (!file.exists())
		{
			data = "<Collection>\n\t<LastId>0</LastId>\n</Collection>";
			StringUtils.writeFile(file, data);
		}
		else data = StringUtils.readFile(file);
		int next = Integer.parseInt(StringUtils.getTagContent(data, "LastId").trim())+1;
		data = StringUtils.setTagContent(data, "LastId", ""+next);
		StringUtils.writeFile(file, data);
		return next;
	}

	public synchronized List<Integer> getAllBookIds() throws DataLinkException
	{
		try {return BookFS2.getAllBookIds(root);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized BookData getBookData(int id) throws DataLinkException
	{
		try {return BookFS2.getBookData(root, id);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void setBookName(int id, String name) throws DataLinkException
	{
		try {BookFS2.setBookName(root, id, name);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized int addBook(String name) throws DataLinkException
	{
		try {return BookFS2.addBook(root, name);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void removeBook(int bookId) throws DataLinkException
	{
		try {BookFS2.removeBook(root, bookId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized PageData getPageData(int bookId, int pageNum) throws DataLinkException
	{
		try {return PageFS2.getPageData(root, bookId, pageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized int addPage(int bookId, int pageNum, InputStream data) throws DataLinkException
	{
		try {return PageFS2.addPage(root, bookId, pageNum, data);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void removePage(int bookId, int pageNum) throws DataLinkException
	{
		try {PageFS2.removePage(root, bookId, pageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void increasePageNumbers(int bookId, int fromPageNum) throws DataLinkException
	{
		try {BookFS2.increasePageNumbers(root, bookId, fromPageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void decreasePageNumbers(int bookId, int fromPageNum) throws DataLinkException
	{
		try {BookFS2.decreasePageNumbers(root, bookId, fromPageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void movePage(int bookId, int from, int to) throws DataLinkException
	{
		try
		{
			PageFS2.setPageNum(root, bookId, from, 0);
			decreasePageNumbers(bookId, from);
			increasePageNumbers(bookId, to);
			PageFS2.setPageNum(root, bookId, 0, to);
		}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void setPageImage(int pageId, int bookId, int pageNum, InputStream file) throws DataLinkException
	{
		try {PageFS2.setPageImage(root, bookId, pageNum, file);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized byte [] getPageImage(int pageId, int bookId, int pageNum) throws DataLinkException
	{
		try {return PageFS2.getPageImage(root, bookId, pageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized RegionData getRegionData(int id, int bookId, int pageNum) throws DataLinkException
	{
		try {return RegionFS2.getRegionData(root, id, bookId, pageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized int addRegion(int pageId, int bookId, int pageNum) throws DataLinkException
	{
		try {return RegionFS2.addRegion(root, pageId, bookId, pageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void removeRegion(int regionId, int bookId, int pageNum) throws DataLinkException
	{
		try {RegionFS2.removeRegion(root, regionId, bookId, pageNum);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void setRegionOutline(int regionId, int bookId, int pageNum, Point[] outline) throws DataLinkException
	{
		try {RegionFS2.setRegionOutline(root, regionId, bookId, pageNum, outline);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void addMetaDataToObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException
	{
		try
		{
			if (bookId < 0)
				MetaDataFS2.addMetaDataToObject(MetaDataFS2.getMDDir(root, objectId), metaDataId);
			else if (pageNum < 0)
				BookFS2.addMetaData(root, bookId, metaDataId);
			else if (regionId < 0)
				PageFS2.addMetaData(root, bookId, pageNum, metaDataId);
			else RegionFS2.addMetaData(root, bookId, pageNum, regionId, metaDataId);
		}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void removeMetaDataFromObject(int objectId, int bookId, int pageNum, int regionId, int metaDataId) throws DataLinkException
	{
		try
		{
			if (bookId < 0)
				MetaDataFS2.removeMetaDataFromObject(MetaDataFS2.getMDDir(root, objectId), metaDataId);
			else if (pageNum < 0)
				BookFS2.removeMetaData(root, bookId, metaDataId);
			else if (regionId < 0)
				PageFS2.removeMetaData(root, bookId, pageNum, metaDataId);
			else RegionFS2.removeMetaData(root, bookId, pageNum, regionId, metaDataId);
		}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized String getMetaDataKeyName(int id, String language) throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataKeyName(root, id, language);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void setMetaDataKey(int mdId, int keyId) throws DataLinkException
	{
		try {MetaDataFS2.setMetaDataKey(root, mdId, keyId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized MetaDataData getMetaDataData(int mdId) throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataData(root, mdId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized InputStream getMetaDataValue(int mdId) throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataValue(root, mdId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}
	
	public synchronized File getMetaDataFile(int mdId) throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataFile(root, mdId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void setMetaDataValue(int mdId, InputStream stream) throws DataLinkException
	{
		try {MetaDataFS2.setMetaDataValue(root, mdId, stream);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized int addMetaData(int keyId, String type) throws DataLinkException
	{
		try {return MetaDataFS2.addMetaData(root, keyId, type);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void removeMetaData(int mdId) throws DataLinkException
	{
		try {MetaDataFS2.removeMetaData(root, mdId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized List<Integer> getMetaDataIds(int keyId, String type) throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataIds(root, keyId, type);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized int getMetaDataKeyId(String name, String language) throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataKeyId(root, name, language);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized int addMetaDataKey() throws DataLinkException
	{
		try {return MetaDataFS2.addMetaDataKey(root);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}
	
	public synchronized void removeMetaDataKey(int keyId) throws DataLinkException
	{
		try {MetaDataFS2.removeMetaDataKey(root, keyId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized void setMetaDataKeyName(int keyId, String name, String language) throws DataLinkException
	{
		try {MetaDataFS2.setMetaDataKeyName(root, keyId, name, language);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized List<Integer> getMetaDataKeyIds() throws DataLinkException
	{
		try {return MetaDataFS2.getMetaDataKeyIds(root);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized Map<String, Double> search(int keyId, String value, Collection<String> subset, String objectType, double relevance) throws DataLinkException
	{
		try
		{
			if (objectType.equals("book"))
				return SearchFS2.searchBooks(root, subset, keyId, value, relevance);
			else if (objectType.equals("page"))
				return SearchFS2.searchPages(root, subset, keyId, value, relevance);
			else if (objectType.equals("region"))
				return SearchFS2.searchRegions(root, subset, keyId, value, relevance);
		}
		catch (Exception e) {throw new DataLinkException(this, e);}
		return null;
	}

	public synchronized String getBookTitle(int bookId) throws DataLinkException
	{
		try {return BookFS2.getBookTitle(root, bookId);}
		catch (Exception e) {throw new DataLinkException(this, e);}
	}

	public synchronized Pair<Integer, Integer> getPageBookIdAndNumber(String url) throws DataLinkException
	{
		String [] parts = url.split("/");
		int bookId = Integer.parseInt(parts[0]);
		int pageNum = Integer.parseInt(parts[1].substring(1));
		return new Pair<Integer, Integer>(bookId, pageNum);
	}

	public synchronized String getRegionPageId(String url) throws DataLinkException
	{
		String [] parts = url.split("/");
		return parts[0]+"/"+parts[1];
	}

	public synchronized List<Pair<Integer, String>> getMetaDataText(String id, Collection<Integer> keyIds) throws DataLinkException
	{
		try
		{
			String [] parts = id.split("/");
			if (parts.length == 1)
				return MetaDataFS2.getMetaDataText(root, BookFS2.getBookDir(root, Integer.parseInt(parts[0])), keyIds);
			if (parts.length == 2)
				return MetaDataFS2.getMetaDataText(root, PageFS2.getPageDir(root, Integer.parseInt(parts[0]), Integer.parseInt(parts[1].substring(1))), keyIds);
			if (parts.length == 3)
				return MetaDataFS2.getMetaDataText(root, 
					RegionFS2.getRegionDir(root, Integer.parseInt(parts[0]), Integer.parseInt(parts[1].substring(1)), Integer.parseInt(parts[2])), keyIds);
		}
		catch (Exception e) {throw new DataLinkException(this, e);}
		return null;
	}
	
	public ActionProvider getActionProvider(DocExploreDataLink link) {return new FS2ActionProvider(link);}
	public boolean supportsHistory() {return true;}
}
