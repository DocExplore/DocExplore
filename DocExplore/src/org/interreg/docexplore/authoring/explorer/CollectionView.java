package org.interreg.docexplore.authoring.explorer;

import java.awt.Point;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class CollectionView extends DataLinkView
{
	public CollectionView(DataLinkExplorer explorer)
	{
		super(explorer);
	}
	
	Icon icon = ImageUtils.getIcon("book-48x48.png");
	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		if (!path.equals("docex://"))
			throw new Exception("Invalid collection path: '"+path+"'");
		
		explorer.notifyExploringChanged(explorer.link);
		Vector<ViewItem> res = new Vector<ViewItem>();
		List<Integer> ids = explorer.link.getLink().getAllBookIds();
		for (int id : ids)
		{
			Book book = explorer.link.getBook(id);
			res.add(new ViewItem(book.getName(), book.getLastPageNumber()+" pages", book));
		}
		return res;
	}
	
	@Override public boolean canHandle(String path)
	{
		return path.equals("docex://");
	}

	@Override protected Icon getIcon(Object object) {return icon;}
	
	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items) {return DropType.None;}
	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception {}
}
