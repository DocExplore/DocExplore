package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.actions.AddBookAction;

public class AddFS2BookAction extends AddBookAction
{
	public AddFS2BookAction(DocExploreDataLink link, String title, List<File> files)
	{
		super(link, title, files);
	}

	DeleteFS2BooksAction reverse = null;
	AddFS2PagesAction action = null;
	public void doAction() throws Exception
	{
		if (reverse == null)
		{
			book = new Book(link, title);
			action = new AddFS2PagesAction(link, book, files);
			action.cacheDir = cacheDir;
			action.doAction();
			failed = action.failed;
			action = null;
		}
		else reverse.undoAction();
	}

	public void undoAction() throws Exception
	{
		if (reverse == null)
		{
			reverse = new DeleteFS2BooksAction(link, Arrays.asList(book));
			reverse.cacheDir = cacheDir;
		}
		reverse.doAction();
	}

	public void dispose()
	{
		super.dispose();
		reverse = null;
	}

	public double progress()
	{
		AddFS2PagesAction action = this.action;
		if (action != null)
			return action.progress();
		return super.progress();
	}
}
