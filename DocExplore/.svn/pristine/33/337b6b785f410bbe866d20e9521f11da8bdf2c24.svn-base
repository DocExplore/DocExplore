package org.interreg.docexplore.management.manage;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;

public class ManageMouseListener extends MouseAdapter
{
	ManageComponent manageComp;
	
	public ManageMouseListener(ManageComponent manageComp)
	{
		this.manageComp = manageComp;
	}
	
//	@SuppressWarnings({ "serial", "unchecked" })
	public void mouseClicked(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
		{
//			@SuppressWarnings("rawtypes")
//			final Vector<Book> books = new Vector(Arrays.asList(manageComp.bookList.getSelectedValues()));
//			JPopupMenu popMenu = new JPopupMenu();
//			popMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("manageAddBookLabel"))
//			{
//				public void actionPerformed(ActionEvent arg0)
//				{
//					List<File> files = SelectPagesPanel.show();
//					if (files == null)
//						return;
//					String title = JOptionPane.showInputDialog(manageComp.win, XMLResourceBundle.getBundledString("manageInputTitleLabel"));
//					if (title == null || title.length() == 0)
//						return;
//					manageComp.handler.addBook(title, files);
//					((CollectionNode)manageComp.bookList.getModel()).reload();
//					manageComp.bookList.repaint();
//				}
//			});
//			
//			if (books.size() > 0)
//				popMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("manageDeleteBookLabel")) {
//					public void actionPerformed(ActionEvent arg0)
//					{
//						if (manageComp.handler.booksDeleted(books))
//						{
//							((CollectionNode)manageComp.bookList.getModel()).reload();
//							manageComp.bookList.clearSelection();
//							manageComp.bookList.repaint();
//						}
//					}});
//				
//			if (books.size() == 1)
//			{
//				if (books.size() == 1)
//				{
//					popMenu.add(new AbstractAction(
//						XMLResourceBundle.getBundledString("manageProcessBookLabel")) {
//						public void actionPerformed(ActionEvent arg0) {
//							manageComp.handler.pagesProcessed(getPages(books.get(0)));}});
//					popMenu.add(new AbstractAction(
//						XMLResourceBundle.getBundledString("manageExportBookLabel")) {
//						public void actionPerformed(ActionEvent arg0)
//							{manageComp.handler.pagesExported(getPages(books.get(0)));}});
//				}
//			}
//			if (popMenu.getComponentCount() > 0)
//				popMenu.show(e.getComponent(), e.getX(), e.getY());
		}
		else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
		{
			int index = manageComp.bookList.locationToIndex(e.getPoint());
			if (index < 0 || !manageComp.bookList.getCellBounds(index, index).contains(e.getPoint()))
				return;
			manageComp.handler.bookOpened((Book)manageComp.bookList.getModel().getElementAt(index));
		}
	}
	
	List<Page> getPages(Book book)
	{
		Vector<Page> res = new Vector<Page>();
		try
		{
			int nPages = book.getLastPageNumber();
			res.ensureCapacity(nPages);
			for (int i=1;i<=nPages;i++)
				res.add(book.getPage(i));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return res;
	}
}
