package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.util.List;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.actions.AddPagesAction;

public class AddFS2PagesAction extends AddPagesAction
{
	public AddFS2PagesAction(DocExploreDataLink link, Book book, List<File> files)
	{
		super(link, book, files);
	}

	DeleteFS2PagesAction reverse = null;
	public void doAction() throws Exception
	{
		if (reverse == null)
			super.doAction();
		else reverse.undoAction();
	}

	public void undoAction() throws Exception
	{
		if (reverse == null)
		{
			reverse = new DeleteFS2PagesAction(link, pages);
			reverse.cacheDir = cacheDir;
		}
		reverse.doAction();
	}
}
