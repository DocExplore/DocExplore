/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.fs2;

import java.util.ArrayList;
import java.util.List;

import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.actions.DeletePosterPartsAction;
import org.interreg.docexplore.util.Pair;

public class DeleteFS2PosterPartsAction extends DeletePosterPartsAction
{
	List<Pair<Pair<Integer, Integer>, Boolean>> deletions;
	
	public DeleteFS2PosterPartsAction(DocExploreDataLink link, Book book, MetaData annotation)
	{
		super(link, book, annotation);
	}
	public DeleteFS2PosterPartsAction(DocExploreDataLink link, Book book, List<MetaData> annotations)
	{
		super(link, book, annotations);
	}
	
	public void doAction() throws Exception
	{
		deletions = new ArrayList<Pair<Pair<Integer, Integer>,Boolean>>(parts.size());
		for (MetaData part : parts)
		{
			String [] pos = part.getMetaDataString(link.partPosKey).split(",");
			int col = Integer.parseInt(pos[0]);
			int row = Integer.parseInt(pos[1]);
			boolean rowRemoved = PosterUtils.removeFromRow(link, book, col, row);
			deletions.add(new Pair<Pair<Integer, Integer>, Boolean>(new Pair<Integer, Integer>(col, row), rowRemoved));
			book.removeMetaData(part);
		}
	}
	
	@Override public void undoAction() throws Exception
	{
		for (int i=parts.size()-1;i>=0;i--)
		{
			Pair<Pair<Integer, Integer>,Boolean> del = deletions.get(i);
			//System.out.println(del.first.first+" "+del.first.second+" "+del.second);
			PosterUtils.addToRow(link, book, parts.get(i), del.first.first, del.first.second, del.second);
			book.addMetaData(parts.get(i));
		}
	}

	public void dispose()
	{
		book = null;
		parts = null;
	}
}
