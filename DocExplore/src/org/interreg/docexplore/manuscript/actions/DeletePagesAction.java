package org.interreg.docexplore.manuscript.actions;

import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;

public class DeletePagesAction extends UnreversibleAction
{
	public Book book;
	public List<Page> pages;
	
	public DeletePagesAction(List<Page> pages)
	{
		this.pages = pages;
		this.book = pages.get(0).getBook();
	}
	
	public void doAction() throws Exception
	{
		for (Page page : pages)
			try {page.getBook().removePage(page.getPageNumber());}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}

	public String description()
	{
		return pages.size() == 1 ? XMLResourceBundle.getBundledString("deletePage") : XMLResourceBundle.getBundledString("deletePages");
	}
	
	public void dispose()
	{
		book = null;
		pages = null;
	}
}
