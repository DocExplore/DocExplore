package org.interreg.docexplore.manuscript.actions;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.history.ReversibleAction;

public class MovePagesAction extends ReversibleAction
{
	Book book;
	List<Page> pages;
	Page moveAfter;
	LinkedList<Pair<Integer, Integer>> moves = new LinkedList<Pair<Integer, Integer>>();
	
	public MovePagesAction(List<Page> pages, Page moveAfter)
	{
		this.book = pages.get(0).getBook();
		this.pages = pages;
		this.moveAfter = moveAfter;
	}
	
	public void doAction() throws Exception
	{
		while (moveAfter != null && pages.contains(moveAfter))
			moveAfter = moveAfter.getPageNumber() == 1 ? null : book.getPage(moveAfter.getPageNumber()-1);
		int movedPages = 0;
		moves.clear();
		for (Page page : pages)
		{
			int fromPage = (page).getPageNumber();
			int toPage = moveAfter == null ? movedPages+1 : moveAfter.getPageNumber()+movedPages+1;
			if (toPage > fromPage)
				toPage--;
			book.movePage(fromPage, toPage);
			moves.addFirst(new Pair<Integer, Integer>(fromPage, toPage));
			movedPages++;
		}
	}

	public void undoAction() throws Exception
	{
		for (Pair<Integer, Integer> pair : moves)
			book.movePage(pair.second, pair.first);
	}

	public void dispose()
	{
		book = null;
		pages = null;
		moveAfter = null;
		moves = null;
	}

	public String description()
	{
		return XMLResourceBundle.getBundledString("movePages");
	}
}
