/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
import org.interreg.docexplore.management.image.PosterUtils;
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
				if (((ViewItem)comp).data.object instanceof Page)
					PreviewPanel.previewPage((Page)((ViewItem)comp).data.object, p.x, p.y);
				else if (((ViewItem)comp).data.object instanceof MetaData && ((MetaData)((ViewItem)comp).data.object).getType().equals(MetaData.imageType))
					try {PreviewPanel.previewImage(((MetaData)((ViewItem)comp).data.object).getImage(), p.x, p.y);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex, true);}
			}
		});
	}
	
	@Override public Component getViewComponent() {return scrollPane;}
	
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
		
		if (PosterUtils.isPoster(curBook))
		{
			List<MetaData> parts = curBook.getMetaDataListForKey(explorer.link.partKey);
			for (MetaData part : parts)
			{
				String [] pos = part.getMetaDataString(explorer.link.partPosKey).split(",");
				res.add(new ViewItem(explorer.partTerm+" "+pos[0]+","+pos[1], "", part));
			}
			//PosterUtils.getPosterParts(link, book)
		}
		
		return res;
	}

	@Override protected Icon getIcon(Object object)
	{
		try
		{
			MetaDataKey miniKey = explorer.link.getKey("mini", "");
			List<MetaData> minis = ((AnnotatedObject)object).getMetaDataListForKey(miniKey);
			return minis.isEmpty() ? null : new ImageIcon(minis.get(0).getImage());
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
