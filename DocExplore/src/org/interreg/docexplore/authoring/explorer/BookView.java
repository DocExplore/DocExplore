package org.interreg.docexplore.authoring.explorer;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;

@SuppressWarnings("serial")
public class BookView extends DataLinkView implements FilterPanel.Listener
{
	public BookView(DataLinkExplorer explorer) throws Exception
	{
		super(explorer);
		
		addMouseListener(new MouseAdapter()
		{
			@Override public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON3)
					return;
				Component comp = getComponentAt(e.getPoint());
				if (comp == null || !(comp instanceof ViewItem))
					return;
				Point p = new Point(e.getPoint());
				SwingUtilities.convertPointToScreen(p, BookView.this);
				PreviewPanel.previewPage((Page)((ViewItem)comp).data.object, p.x, p.y);
			}
		});
	}
	
	@Override public boolean canHandle(String path) throws Exception
	{
		return AnnotatedObject.resolveUri(explorer.link, path) instanceof Book;
	}

	public Book curBook = null;
	
	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		curBook = (Book)AnnotatedObject.resolveUri(explorer.link, path);
		explorer.notifyExploringChanged(curBook);
		
		Vector<ViewItem> res = new Vector<ViewItem>();
		int lastPage = curBook.getLastPageNumber();
		for (int i=1;i<=lastPage;i++)
		{
			Page page = curBook.getPage(i);
			if (filter == null || filter.visible(page))
			{
				String importedFrom = "";
				MetaDataKey key = page.getLink().getOrCreateKey("imported-from", "");
				List<MetaData> annotations = page.getMetaDataListForKey(key);
				if (annotations != null && !annotations.isEmpty())
				{
					importedFrom = " ("+annotations.get(0).getString()+")";
					page.unloadMetaData();
				}
				res.add(new ViewItem(explorer.pageTerm+" "+i, page.getRegions().size()+" regions"+importedFrom, page));
			}
			page.unloadAll(false);
		}
		System.gc();
		return res;
	}

	@Override protected Icon getIcon(Object object)
	{
		try
		{
			MetaDataKey miniKey = explorer.link.getKey("mini", "");
			return new ImageIcon(((Page)object).getMetaDataListForKey(miniKey).get(0).getImage());
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return null;
	}

	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items) {return DropType.None;}
	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception {}
	
	FilterPanel filter = null;
	public void setFilter(FilterPanel filter)
	{
		if (this.filter != null)
			this.filter.removeListener(this);
		this.filter = filter;
		if (filter != null)
			filter.addListener(this);
		filterChanged(null);
	}

	public void filterChanged(FilterPanel filter)
	{
		if (explorer.curView != this)
			return;
		explorer.explore(explorer.curPath);
	}
}
