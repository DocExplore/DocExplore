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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
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
		if (book == null)
			return;
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
		if (book == null)
			return;
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
