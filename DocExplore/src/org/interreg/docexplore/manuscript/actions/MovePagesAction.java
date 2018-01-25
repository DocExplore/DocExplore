/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.actions;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.internationalization.Lang;
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
		return Lang.s("movePages");
	}
}
