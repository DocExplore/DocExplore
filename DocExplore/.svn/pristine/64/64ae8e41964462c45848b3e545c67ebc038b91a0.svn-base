package org.interreg.docexplore.manuscript.actions;

import java.io.File;
import java.util.List;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.Pair;

public class AddBookAction extends UnreversibleAction
{
	public DocExploreDataLink link;
	public String title;
	public List<File> files;
	public Book book = null;
	public List<Pair<Page, File>> failed = null;
	
	public AddBookAction(DocExploreDataLink link, String title, List<File> files)
	{
		this.link = link;
		this.title = title;
		this.files = files;
	}
	
	AddPagesAction action = null;
	public void doAction() throws Exception
	{
		book = new Book(link, title);
		action = new AddPagesAction(link, book, files);
		action.doAction();
		failed = action.failed;
		action = null;
	}

	public String description()
	{
		return XMLResourceBundle.getBundledString("addBook");
	}
	
	public void dispose()
	{
		files = null;
		book = null;
	}
	
	public double progress()
	{
		AddPagesAction action = this.action;
		if (action != null)
			return action.progress();
		return super.progress();
	}
}
