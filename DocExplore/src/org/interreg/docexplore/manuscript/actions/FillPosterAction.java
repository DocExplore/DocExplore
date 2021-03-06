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

public class FillPosterAction extends ReversibleAction
{
	DocExploreDataLink link;
	Book book;
	MetaData [][] parts = null;
	
	public FillPosterAction(DocExploreDataLink link, Book book)
	{
		this.link = link;
		this.book = book;
	}
	
	public void doAction() throws Exception
	{
		this.parts = PosterUtils.getBaseTilesArray(link, book);
		for (int i=0;i<parts.length;i++)
			for (int j=0;j<parts[i].length;j++)
				if (parts[i][j] == null)
				{
					MetaData part = new MetaData(link, link.partKey, "empty");
					book.addMetaData(part);
					PosterUtils.setPartPos(link, part, i, j);
				}
		book.setMetaDataString(link.upToDateKey, "false");
	}

	public void undoAction() throws Exception
	{
		MetaData [][] parts = PosterUtils.getBaseTilesArray(link, book);
		for (int i=0;i<parts.length;i++)
			for (int j=0;j<parts[i].length;j++)
				if (this.parts[i][j] == null && parts[i][j] != null)
					book.removeMetaData(parts[i][j]);
		book.setMetaDataString(link.upToDateKey, "false");
	}
	
	public void dispose()
	{
		book = null;
		parts = null;
	}

	public String description()
	{
		return Lang.s("fillPoster");
	}
}
