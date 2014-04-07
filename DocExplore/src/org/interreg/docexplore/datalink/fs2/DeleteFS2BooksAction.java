package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.actions.DeleteBooksAction;

public class DeleteFS2BooksAction extends DeleteBooksAction
{
	DocExploreDataLink link;
	
	public DeleteFS2BooksAction(DocExploreDataLink link, List<Book> books)
	{
		super(books);
		
		this.link = link;
	}
	
	public void doAction() throws Exception
	{
		File root = ((DataLinkFS2)link.getLink()).getFile();
		for (Book book : books)
		{
			File bookDir = BookFS2.getBookDir(root, book.getId());
			FileUtils.moveDirectoryToDirectory(bookDir, cacheDir, true);
			link.books.remove(book.getId());
		}
	}

	public void undoAction() throws Exception
	{
		File root = ((DataLinkFS2)link.getLink()).getFile();
		for (File bookDir : cacheDir.listFiles())
			if (bookDir.isDirectory() && bookDir.getName().startsWith("book"))
				FileUtils.moveDirectoryToDirectory(bookDir, root, true);
		for (Book book : books)
			link.books.put(book.getId(), book);
	}
}
