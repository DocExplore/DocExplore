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

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.util.history.ReversibleAction;

public class MovePartAction extends ReversibleAction
{
	DocExploreDataLink link;
	Book book;
	MetaData part;
	int row, col;
	int fromRow, fromCol;
	boolean insertRow, wasRowRemoved;
	
	public MovePartAction(DocExploreDataLink link, Book book, MetaData part, int col, int row, boolean insertRow)
	{
		this.link = link;
		this.book = book;
		this.part = part;
		this.col = col;
		this.row = row;
		this.insertRow = insertRow;
	}
	
	public void doAction() throws Exception
	{
		String [] pos = part.getMetaDataString(link.partPosKey).split(",");
		fromCol = Integer.parseInt(pos[0]);
		fromRow = Integer.parseInt(pos[1]);
		wasRowRemoved = PosterUtils.removeFromRow(link, book, fromCol, fromRow);
		PosterUtils.addToRow(link, book, part, !wasRowRemoved && fromRow == row && fromCol < col ? col-1 : col, wasRowRemoved && fromRow < row ? row-1 : row, insertRow);
		book.setMetaDataString(link.upToDateKey, "false");
	}

	public void undoAction() throws Exception
	{
		String [] pos = part.getMetaDataString(link.partPosKey).split(",");
		int col = Integer.parseInt(pos[0]);
		int row = Integer.parseInt(pos[1]);
		PosterUtils.removeFromRow(link, book, col, row);
		PosterUtils.addToRow(link, book, part, fromCol, fromRow, wasRowRemoved);
		book.setMetaDataString(link.upToDateKey, "false");
	}
	
	
	
	public void dispose()
	{
		book = null;
		part = null;
	}

	public String description()
	{
		return Lang.s("movePages");
	}
}
