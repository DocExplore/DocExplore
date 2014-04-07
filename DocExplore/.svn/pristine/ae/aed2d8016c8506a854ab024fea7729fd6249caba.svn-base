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
