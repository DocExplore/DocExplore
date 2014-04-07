package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.interreg.docexplore.authoring.explorer.CollectionView;
import org.interreg.docexplore.authoring.explorer.DataLinkExplorer;
import org.interreg.docexplore.authoring.explorer.Explorer.Listener;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class CollectionEditorView extends CollectionView
{
	public CollectionEditorView(final DataLinkExplorer explorer)
	{
		super(explorer);
		
		final JButton newBook = new JButton(new AbstractAction("", ImageUtils.getIcon("add-24x24.png"))
		{
			@Override public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(CollectionEditorView.this, XMLResourceBundle.getBundledString("collectionAddBookMessage"),
					XMLResourceBundle.getBundledString("collectionDefaultBookLabel"));
				if (name == null)
					return;
				try {explorer.link.getLink().addBook(name);}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				explorer.explore("docex://");
			}
		});
		newBook.setToolTipText(XMLResourceBundle.getBundledString("generalToolbarCreate"));
		final JButton removeBook = new JButton(new AbstractAction("", ImageUtils.getIcon("remove-24x24.png"))
		{
			@Override public void actionPerformed(ActionEvent e)
			{
				if (selected.isEmpty())
					return;
				for (ViewItem item : selected)
					try {explorer.link.getLink().removeBook(((Book)item.data.object).getId());}
					catch (Exception ex) {ex.printStackTrace();}
				explorer.explore(explorer.curPath);
			}
		});
		removeBook.setToolTipText(XMLResourceBundle.getBundledString("generalToolbarRemove"));
		final JButton editBook = new JButton(new AbstractAction("", ImageUtils.getIcon("pencil-24x24.png"))
		{
			@Override public void actionPerformed(ActionEvent e)
			{
				if (selected.size() != 1)
					return;
				Book book = (Book)selected.iterator().next().data.object;
				String name = JOptionPane.showInputDialog(CollectionEditorView.this, XMLResourceBundle.getBundledString("collectionAddBookMessage"), book.getName());
				if (name == null)
					return;
				try
				{
					book.setName(name);
					explorer.explore(explorer.curPath);
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		});
		editBook.setToolTipText(XMLResourceBundle.getBundledString("generalToolbarEdit"));
		removeBook.setEnabled(false);
		editBook.setEnabled(false);
		addSelectionListener(new SelectionListener() {public void selectionChanged(ExplorerView view)
		{
			removeBook.setEnabled(selected.size() > 0);
			editBook.setEnabled(selected.size() == 1);
		}});
		explorer.addListener(new Listener() {@Override public void exploringChanged(Object explored)
		{
			if (explored instanceof DocExploreDataLink)
			{
				explorer.toolPanel.add(newBook);
				explorer.toolPanel.add(removeBook);
				explorer.toolPanel.add(editBook);
			}
			else
			{
				explorer.toolPanel.remove(newBook);
				explorer.toolPanel.remove(removeBook);
				explorer.toolPanel.remove(editBook);
			}
		}});
	}

//	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items)
//	{
//		if (source instanceof CollectionView && source != this)
//			return DropType.Anywhere;
//		if (source instanceof BookView)
//			return DropType.OnItem;
//		if (source instanceof FolderView)
//			return DropType.OnItem;
//		return DropType.None;
//	}
//
//	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception
//	{
//		if (source instanceof CollectionView && source != this)
//		{
//			Set<Book> newBookSet = new TreeSet<Book>();
//			for (ViewItem item : items)
//				if (item.object instanceof Book)
//					newBookSet.add(explorer.importer.add((Book)item.object, explorer.link, null));//explorer.tool.filter));
//			refreshSelection(newBookSet);
//		}
//		else if (source instanceof BookView)
//		{
//			ViewItem target = vim.itemAt(where.x, where.y);
//			if (target == null)
//				return;
//			Book book = (Book)target.object;
//			for (ViewItem item : items)
//				if (item.object instanceof Page)
//					explorer.importer.add((Page)item.object, book, null);//explorer.tool.filter);
//		}
//		else if (source instanceof FolderView)
//		{
//			ViewItem target = vim.itemAt(where.x, where.y);
//			if (target == null)
//				return;
//			Book book = (Book)target.object;
//			for (ViewItem item : items)
//				if (item.object instanceof File)
//			{
//				FileImageSource image = new FileImageSource((File)item.object);
//				if (!image.isValid())
//					continue;
//				Page page = book.appendPage(image);
//				DocExploreDataLink.getImageMini(page);
//			}
//		}
//	}
}
