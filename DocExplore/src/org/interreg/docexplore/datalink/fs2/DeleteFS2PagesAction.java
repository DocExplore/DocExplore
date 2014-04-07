package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.actions.DeletePagesAction;

public class DeleteFS2PagesAction extends DeletePagesAction
{
	DocExploreDataLink link;
	
	public DeleteFS2PagesAction(DocExploreDataLink link, List<Page> pages)
	{
		super(pages);
		
		this.link = link;
	}
	
	Collection<File> orderedPages(File dir)
	{
		Map<Integer, File> files = new TreeMap<Integer, File>();
		for (File file : dir.listFiles())
			if (file.isDirectory() && file.getName().startsWith("page"))
				files.put(Integer.parseInt(file.getName().substring("page".length())), file);
		return files.values();
	}
	TreeMap<Integer, Integer> newPageNumbers = new TreeMap<Integer, Integer>();
	public void doAction() throws Exception
	{
		File root = ((DataLinkFS2)link.getLink()).getFile();
		newPageNumbers.clear();
		for (Page page : pages)
		{
			File pageDir = PageFS2.getPageDir(root, book.getId(), page.getPageNumber());
			FileUtils.moveDirectoryToDirectory(pageDir, cacheDir, true);
		}
		Collection<File> files = orderedPages(BookFS2.getBookDir(root, book.getId()));
		int curNum = 1;
		NavigableMap<Integer, Page> newPagesByNumber = new TreeMap<Integer, Page>();
		for (File file : files)
		{
			int pageNum = Integer.parseInt(file.getName().substring("page".length()));
			if (curNum != pageNum)
			{
				file.renameTo(new File(file.getParentFile(), "page"+curNum));
				Page page = book.pagesByNumber.get(pageNum);
				if (page != null)
					page.pageNum = curNum;
				book.pagesByNumber.remove(pageNum);
				newPagesByNumber.put(curNum, page);
			}
			else newPagesByNumber.put(pageNum, book.pagesByNumber.get(pageNum));
			newPageNumbers.put(pageNum, curNum);
			curNum++;
		}
		book.pagesByNumber = newPagesByNumber;
	}

	public void undoAction() throws Exception
	{
		File root = ((DataLinkFS2)link.getLink()).getFile();
		for (Map.Entry<Integer, Integer> entry : newPageNumbers.descendingMap().entrySet())
		{
			File file = PageFS2.getPageDir(root, book.getId(), entry.getValue());
			file.renameTo(new File(file.getParentFile(), "page"+entry.getKey()));
			Page page = book.pagesByNumber.get(entry.getValue());
			if (page != null)
				page.pageNum = entry.getKey();
			book.pagesByNumber.remove(entry.getValue());
			book.pagesByNumber.put(entry.getKey(), page);
		}
		Collection<File> files = orderedPages(cacheDir);
		for (File file : files)
			FileUtils.moveDirectoryToDirectory(file, BookFS2.getBookDir(root, book.getId()), true);
		for (Page page : pages)
			book.pagesByNumber.put(page.pageNum, page);
	}
}
