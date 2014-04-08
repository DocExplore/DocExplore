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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;

public class PageFS2
{
	public static File getPageDir(File root, int bookId, int pageNum) {return new File(BookFS2.getBookDir(root, bookId), "page"+pageNum);}
	
	public static PageData getPageData(File root, int bookId, int pageNum) throws IOException
	{
		File pageDir = getPageDir(root, bookId, pageNum);
		String data = StringUtils.readFile(new File(pageDir, "index.xml"));
		int id = Integer.parseInt(StringUtils.getTagContent(data, "Id").trim());
		
		List<Integer> regions = new LinkedList<Integer>();
		for (File file : pageDir.listFiles())
			if (file.isDirectory() && file.getName().startsWith("region"))
				try {regions.add(Integer.parseInt(file.getName().substring("region".length())));}
				catch (Exception e) {e.printStackTrace();}
		
		return new PageData(id, regions, MetaDataFS2.getMetaData(root, pageDir));
	}
	
	public static int addPage(File root, int bookId, int pageNum, InputStream data) throws IOException
	{
		File pageDir = getPageDir(root, bookId, pageNum);
		pageDir.mkdir();
		
		int id = DataLinkFS2.getNextId(root);
		StringUtils.writeFile(new File(pageDir, "index.xml"), "<Page>\n\t<Id>"+id+"</Id>\n</Page>");
		ByteUtils.writeStream(data, new FileOutputStream(new File(pageDir, "image"), false));
		return id;
	}
	
	public static void removePage(File root, int bookId, int pageNum) throws IOException
	{
		File pageDir = getPageDir(root, bookId, pageNum);
		FS2Utils.delete(pageDir);
	}
	
	public static void setPageNum(File root, int bookId, int pageNum, int toPageNum)
	{
		File pageDir = getPageDir(root, bookId, pageNum);
		pageDir.renameTo(new File(pageDir.getParent(), "page"+toPageNum));
	}
	
	public static void setPageImage(File root, int bookId, int pageNum, InputStream file) throws IOException
	{
		File pageDir = getPageDir(root, bookId, pageNum);
		ByteUtils.writeStream(file, new FileOutputStream(new File(pageDir, "image"), false));
	}
	
	public static byte [] getPageImage(File root, int bookId, int pageNum) throws IOException
	{
		File pageDir = getPageDir(root, bookId, pageNum);
		return ByteUtils.readFile(new File(pageDir, "image"));
	}
	
	public static void addMetaData(File root, int bookId, int pageNum, int metaDataId) throws IOException
	{
		MetaDataFS2.addMetaDataToObject(getPageDir(root, bookId, pageNum), metaDataId);
	}
	public static void removeMetaData(File root, int bookId, int pageNum, int metaDataId) throws IOException
	{
		MetaDataFS2.removeMetaDataFromObject(getPageDir(root, bookId, pageNum), metaDataId);
	}
	
	public static Pair<Integer, Integer> getPageBookIdAndNumber(int bookId, int pageId) throws IOException
	{
		return null;
	}
}
