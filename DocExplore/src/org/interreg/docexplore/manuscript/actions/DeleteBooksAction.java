package org.interreg.docexplore.manuscript.actions;

import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Book;

public class DeleteBooksAction extends UnreversibleAction
{
	public List<Book> books;
	
	public DeleteBooksAction(List<Book> books)
	{
		this.books = books;
	}
	
	public void doAction() throws Exception
	{
		for (Book book : books)
			try {book.remove();}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}

	public String description()
	{
		return books.size() == 1 ? XMLResourceBundle.getBundledString("deleteBook") : XMLResourceBundle.getBundledString("deleteBooks");
	}

	public void dispose()
	{
		books = null;
	}
}
