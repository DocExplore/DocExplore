package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.util.StringUtils;

public class BookFS2
{
	public static List<Integer> getAllBookIds(File root)
	{
		List<Integer> res = new LinkedList<Integer>();
		for (File file : root.listFiles())
			if (file.isDirectory() && file.getName().startsWith("book"))
				try {res.add(Integer.parseInt(file.getName().substring("book".length())));}
				catch (Exception e) {e.printStackTrace();}
		return res;
	}
	
	public static File getBookDir(File root, int id) {return new File(root, "book"+id);}
	
	public static BookData getBookData(File root, int id) throws IOException
	{
		File bookDir = getBookDir(root, id);
		String data = StringUtils.readFile(new File(bookDir, "index.xml"));
		String name = StringUtils.unescapeSpecialChars(StringUtils.getTagContent(data, "Name"));
		
		List<Integer> pageNums = new LinkedList<Integer>();
		for (File file : bookDir.listFiles())
			if (file.isDirectory() && file.getName().startsWith("page"))
				try {pageNums.add(Integer.parseInt(file.getName().substring("page".length())));}
				catch (Exception e) {e.printStackTrace();}
		
		return new BookData(name, pageNums, MetaDataFS2.getMetaData(root, bookDir));
	}
	
	public static void setBookName(File root, int id, String name) throws IOException
	{
		File bookDir = getBookDir(root, id);
		File bookFile = new File(bookDir, "index.xml");
		String data = StringUtils.readFile(bookFile);
		data = StringUtils.setTagContent(data, "Name", StringUtils.escapeSpecialChars(name));
		StringUtils.writeFile(bookFile, data);
	}
	
	public static int addBook(File root, String name) throws IOException
	{
		int id = DataLinkFS2.getNextId(root);
		File bookDir = new File(root, "book"+id);
		bookDir.mkdir();
		
		StringUtils.writeFile(new File(bookDir, "index.xml"), "<Book>\n\t<Name>"+StringUtils.escapeSpecialChars(name)+"</Name>\n</Book>");
		return id;
	}
	
	public static void removeBook(File root, int id) throws IOException
	{
		File bookDir = getBookDir(root, id);
		FS2Utils.delete(bookDir);
	}
	
	static File [] getOrderedPages(File bookDir)
	{
		SortedMap<Integer, File> files = new TreeMap<Integer, File>();
		for (File file : bookDir.listFiles())
			if (file.isDirectory() && file.getName().startsWith("page"))
				try {files.put(Integer.parseInt(file.getName().substring("page".length())), file);}
				catch (Exception e) {e.printStackTrace();}
		File [] pages;
		if (!files.isEmpty())
		{
			pages = new File [files.lastKey()+1];
			for (Map.Entry<Integer, File> entry : files.entrySet())
				pages[entry.getKey()] = entry.getValue();
		}
		else pages = new File[0];
		return pages;
	}
	
	public static void increasePageNumbers(File root, int bookId, int fromPageNum)
	{
		File bookDir = getBookDir(root, bookId);
		File [] pages = getOrderedPages(bookDir);
		for (int i=pages.length-1;i>=fromPageNum;i--)
			if (pages[i] != null)
				pages[i].renameTo(new File(pages[i].getParent(), "page"+(i+1)));
	}
	
	public static void decreasePageNumbers(File root, int bookId, int fromPageNum)
	{
		File bookDir = getBookDir(root, bookId);
		File [] pages = getOrderedPages(bookDir);
		for (int i=fromPageNum;i<pages.length;i++)
			if (pages[i] != null)
				pages[i].renameTo(new File(pages[i].getParent(), "page"+(i-1)));
	}
	
	public static void addMetaData(File root, int bookId, int metaDataId) throws IOException
	{
		MetaDataFS2.addMetaDataToObject(getBookDir(root, bookId), metaDataId);
	}
	public static void removeMetaData(File root, int bookId, int metaDataId) throws IOException
	{
		MetaDataFS2.removeMetaDataFromObject(getBookDir(root, bookId), metaDataId);
	}
	
	public static String getBookTitle(File root, int bookId) throws IOException
	{
		File bookDir = getBookDir(root, bookId);
		return StringUtils.getTagContent(StringUtils.readFile(new File(bookDir, "index.xml")), "Name");
	}
}
