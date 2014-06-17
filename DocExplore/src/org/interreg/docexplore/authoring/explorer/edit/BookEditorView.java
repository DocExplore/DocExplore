/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToggleButton;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.authoring.explorer.BookView;
import org.interreg.docexplore.authoring.explorer.CollectionView;
import org.interreg.docexplore.authoring.explorer.DataLinkExplorer;
import org.interreg.docexplore.authoring.explorer.Explorer.Listener;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.PageView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.authoring.explorer.file.FolderView;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddPagesAction;
import org.interreg.docexplore.manuscript.actions.DeletePagesAction;
import org.interreg.docexplore.manuscript.actions.MovePagesAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class BookEditorView extends BookView
{
	CoverManager coverManager;
	
	public BookEditorView(final DataLinkExplorer explorer) throws Exception
	{
		super(explorer);
		
		this.coverManager = new CoverManager();
		final JDialog coverDialog = new JDialog(explorer.tool, XMLResourceBundle.getBundledString("coverLabel"));
		//coverDialog.setAlwaysOnTop(true);
		coverDialog.add(coverManager);
		coverDialog.pack();
		GuiUtils.centerOnScreen(coverDialog);
		
		final JButton addPage = new JButton(new AbstractAction("", ImageUtils.getIcon("add-24x24.png"))
		{
			@Override public void actionPerformed(ActionEvent e)
			{
				final File [] files = DocExploreTool.getFileDialogs().openFiles(DocExploreTool.getImagesCategory());
				if (files == null)
					return;
				final List<Page> newPageSet = new LinkedList<Page>();
				GuiUtils.blockUntilComplete(new ProgressRunnable()
				{
					float progress = 0;
					public void run()
					{
						try
						{
							int cnt = 0;
							int insertIndex = items.size();
							for (File file : files)
							{
								FileImageSource image = new FileImageSource(file);
								if (!image.isValid())
									continue;
								Page page = curBook.insertPage((insertIndex++)+1, image);
								DocExploreDataLink.getImageMini(page);
								newPageSet.add(page);
								progress = (++cnt)*1f/files.length;
							}
							pagesImported(newPageSet);
						}
						catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
					}
					public float getProgress() {return progress;}
				}, BookEditorView.this);
				
				refreshSelection(newPageSet);
			}
		});
		final JButton removePage = new JButton(new AbstractAction("", ImageUtils.getIcon("remove-24x24.png"))
		{
			@Override public void actionPerformed(ActionEvent e)
			{
				if (selected.isEmpty())
					return;
				
				final List<Page> pages = new LinkedList<Page>();
				for (ViewItem item : selected)
					if (item.data.object instanceof Page)
						pages.add((Page)item.data.object);
				final DeletePagesAction deletePagesAction = explorer.getActionProvider().deletePages(pages);
				try
				{
					explorer.tool.historyManager.doAction(new WrappedAction(deletePagesAction)
					{
						public void doAction() throws Exception {super.doAction(); explorer.explore("docex://"+curBook.getId());}
						public void undoAction() throws Exception {super.undoAction(); explorer.explore("docex://"+curBook.getId());}
					});
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		});
		
		final JToggleButton setCover = new JToggleButton(new AbstractAction(XMLResourceBundle.getBundledString("coverLabel")) {public void actionPerformed(ActionEvent e)
		{
			JToggleButton button = (JToggleButton)e.getSource();
			coverDialog.setVisible(button.isSelected());
		}});
		coverDialog.addWindowListener(new WindowAdapter()
		{
			public void windowDeactivated(WindowEvent arg0)
			{
				setCover.setSelected(false);
			}
		});
		
		removePage.setToolTipText(XMLResourceBundle.getBundledString("generalToolbarRemovePage"));
		addPage.setToolTipText(XMLResourceBundle.getBundledString("generalToolbarAddPages"));
		explorer.addListener(new Listener() {@Override public void exploringChanged(Object explored)
		{
			if (explored instanceof Book)
			{
				explorer.toolPanel.add(addPage);
				explorer.toolPanel.add(removePage);
				explorer.toolPanel.add(setCover);
			}
			else
			{
				explorer.toolPanel.remove(addPage);
				explorer.toolPanel.remove(removePage);
				explorer.toolPanel.remove(setCover);
			}
		}});
		
		msg = XMLResourceBundle.getBundledString("helpBookMsg");
	}
	
	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		Book old = curBook;
		List<ViewItem> items = super.buildItemList(path);
		if (old != curBook)
			coverManager.setBook(curBook);
		return items;
	}

	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items)
	{
		if (source == null)
			return DropType.BetweenItems;
		if (source instanceof CollectionView)
			return DropType.BetweenItems;
		if (source instanceof BookView)
			return DropType.BetweenItems;
		if (source instanceof PageView)
			return DropType.OnItem;
		if (source instanceof FolderView)
			return DropType.BetweenItems;
		return DropType.None;
	}

	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception
	{
		if (source == null)
		{
			List<Page> newPageSet = new LinkedList<Page>();
			int insertIndex = vim.insertionIndex(where.x, where.y);
			for (ViewItem.Data item : items)
				if (item.object instanceof File)
			{
				FileImageSource image = new FileImageSource((File)item.object);
				if (!image.isValid())
					continue;
				Page page = curBook.insertPage((insertIndex++)+1, image);
				DocExploreDataLink.getImageMini(page);
				newPageSet.add(page);
			}
			pagesImported(newPageSet);
			refreshSelection(newPageSet);
		}
		else if ((source instanceof BookView && source != this) || source instanceof FolderView || source instanceof CollectionView)
		{
			List<Page> newPageSet = new LinkedList<Page>();
			if (source instanceof BookView && source != this)
			{
				ImportOptions importOptions = explorer.tool.importOptions;
				List<Page> sourceSet = new LinkedList<Page>();
				for (ViewItem.Data item : items)
					if (item.object instanceof Page)
						sourceSet.add((Page)item.object);
				if (!importOptions.showOptions(explorer.tool, ((BookView)source).explorer.link, sourceSet))
					return;
				
				int insertIndex = vim.insertionIndex(where.x, where.y);
				for (Page page : sourceSet)
					if (importOptions.keepPage(page))
						newPageSet.add(explorer.importer.add(page, curBook, (insertIndex++)+1, importOptions));
				
//				for (ViewItem item : items)
//					if (item.object instanceof Page)
//						newPageSet.add(explorer.importer.add((Page)item.object, curBook, (insertIndex++)+1, explorer.tool.filter));
			}
			else if (source instanceof FolderView)
			{
				int insertIndex = vim.insertionIndex(where.x, where.y);
				for (ViewItem.Data item : items)
					if (item.object instanceof File)
				{
					FileImageSource image = new FileImageSource((File)item.object);
					if (!image.isValid())
						continue;
					Page page = curBook.insertPage((insertIndex++)+1, image);
					DocExploreDataLink.getImageMini(page);
					newPageSet.add(page);
				}
			}
			else if (source instanceof CollectionView)
			{
				ImportOptions importOptions = explorer.tool.importOptions;
				List<Page> sourceSet = new LinkedList<Page>();
				for (ViewItem.Data item : items)
					if (item.object instanceof Book)
					{
						Book book = (Book)item.object;
						int lastPage = book.getLastPageNumber();
						for (int i=1;i<=lastPage;i++)
							sourceSet.add(book.getPage(i));
					}
				if (!importOptions.showOptions(explorer.tool, ((CollectionView)source).explorer.link, sourceSet))
					return;
				
				if (curBook.pagesByNumber.isEmpty() && curBook.getName().equals(XMLResourceBundle.getBundledString("collectionDefaultBookLabel")))
					for (ViewItem.Data item : items)
						if (item.object instanceof Book)
						{
							Book book = (Book)item.object;
							curBook.setName(book.getName());
							explorer.link.notifyDataLinkChanged();
							break;
						}
				
				int insertIndex = vim.insertionIndex(where.x, where.y);
				for (Page page : sourceSet)
					if (importOptions.keepPage(page))
						newPageSet.add(explorer.importer.add(page, curBook, (insertIndex++)+1, importOptions));
					
//				for (ViewItem item : items)
//					if (item.object instanceof Book)
//				{
//					Book book = (Book)item.object;
//					if (curBook.pagesByNumber.isEmpty() && curBook.getName().equals(XMLResourceBundle.getBundledString("collectionDefaultBookLabel")))
//					{
//						curBook.setName(book.getName());
//						explorer.link.notifyDataLinkChanged();
//					}
//					int lastPage = book.getLastPageNumber();
//					for (int i=1;i<=lastPage;i++)
//						newPageSet.add(explorer.importer.add(book.getPage(i), curBook, (insertIndex++)+1, explorer.tool.filter));
//				}
			}
			pagesImported(newPageSet);
			refreshSelection(newPageSet);
		}
		else if (source instanceof BookView && source == this)
		{
			List<Page> newPageSet = new LinkedList<Page>();
			for (ViewItem.Data item : items)
				if (item.object instanceof Page)
					newPageSet.add((Page)item.object);
			int insertIndex = vim.insertionIndex(where.x, where.y);
			
			if (newPageSet.size() == 1 && (newPageSet.get(0).pageNum == insertIndex || newPageSet.get(0).pageNum == insertIndex+1))
				return;
			
			Page moveAfter = insertIndex == 0 ? null : curBook.getPage(insertIndex);
			MovePagesAction action = explorer.getActionProvider().movePages(newPageSet, moveAfter);
			try
			{
				explorer.tool.historyManager.doAction(new WrappedAction(action)
				{
					public void doAction() throws Exception {super.doAction(); explorer.explore("docex://"+curBook.getId());}
					public void undoAction() throws Exception {super.undoAction(); explorer.explore("docex://"+curBook.getId());}
				});
			}
			catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			
			refreshSelection(newPageSet);
		}
		else if (source instanceof PageView)
		{
			ViewItem target = vim.itemAt(where.x, where.y);
			if (target == null)
				return;
			Page page = (Page)target.data.object;
			
			ImportOptions importOptions = explorer.tool.importOptions;
			List<Region> sourceSet = new LinkedList<Region>();
			for (ViewItem.Data item : items)
				if (item.object instanceof Region)
					sourceSet.add((Region)item.object);
			if (!importOptions.showOptionsForRegions(explorer.tool, ((PageView)source).explorer.link, sourceSet))
				return;
			
			List<Region> regions = new LinkedList<Region>();
			for (Region region : sourceSet)
				regions.add(explorer.importer.add(region, page, importOptions));
//			List<Region> regions = new LinkedList<Region>();
//			for (ViewItem item : items)
//				if (item.object instanceof Region)
//					regions.add(explorer.importer.add((Region)item.object, page, null));//explorer.tool.filter));
			explorer.regionsImported(page, regions);
		}
	}
	
	boolean isIn(Page page, Collection<ViewItem> items)
	{
		for (ViewItem item : items)
			if (item.data.object == page)
				return true;
		return false;
	}
	
	void pagesImported(List<Page> pages)
	{
		try
		{
			final AddPagesAction action = explorer.getActionProvider().addPages(curBook, null);
			explorer.tool.historyManager.doAction(new WrappedAction(action)
			{
				public void doAction() throws Exception
				{
					action.cacheDir = cacheDir;
					if (action.pages.isEmpty()) 
						return; 
					super.doAction(); 
					explorer.explore("docex://"+curBook.getId());
				}
				public void undoAction() throws Exception {super.undoAction(); explorer.explore("docex://"+curBook.getId());}
			});
			action.pages.addAll(pages);
		}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
}
