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
