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
