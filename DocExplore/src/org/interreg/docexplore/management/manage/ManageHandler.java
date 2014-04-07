package org.interreg.docexplore.management.manage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.interreg.docexplore.datalink.DataLink.DataLinkSource;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.DocumentPanel;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.actions.AddBookAction;
import org.interreg.docexplore.manuscript.actions.AddPagesAction;
import org.interreg.docexplore.manuscript.actions.DeleteBooksAction;
import org.interreg.docexplore.manuscript.actions.DeletePagesAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.util.Pair;


public class ManageHandler
{
	MainWindow win;
	DocExploreDataLink link;
	
	public ManageHandler(MainWindow win)
	{
		this.win = win;
		this.link = win.getDocExploreLink();
	}
	public ManageHandler(DataLinkSource source) throws DataLinkException
	{
		this.win = null;
		this.link = new DocExploreDataLink();
		link.setLink(source.getDataLink());
	}
	
	public List<Book> getBooks()
	{
		Vector<Book> books = new Vector<Book>();
		
		if (link.isLinked()) try
		{
			List<Integer> ids = link.getLink().getAllBookIds();
			for (int id : ids)
			{
				Book book = link.getBook(id);
				books.add(book);
			}
		}
		catch (DataLinkException e)
		{
			ErrorHandler.defaultHandler.submit(e);
		}
		return books;
	}

	public void pagesMoved(List<Page> pages, Page moveAfter)
	{
		try
		{
			final Book book = pages.get(0).getBook();
			win.historyManager.doAction(new WrappedAction(win.getActionProvider().movePages(pages, moveAfter))
			{
				public void doAction() throws Exception {super.doAction(); win.refreshTabNames(); refreshViewer(book);}
				public void undoAction() throws Exception {super.undoAction(); win.refreshTabNames(); refreshViewer(book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}

	public void addBook(String title, List<File> files)
	{
		final AddBookAction addBookAction = win.getActionProvider().addBook(title, files);
		try
		{
			win.historyManager.doAction(new WrappedAction(addBookAction)
			{
				public void doAction() throws Exception {super.doAction(); win.manageComponent.reload();}
				public void undoAction() throws Exception {win.closeBooks(Arrays.asList(addBookAction.book)); super.undoAction(); win.manageComponent.reload();}
			});
			if (!addBookAction.failed.isEmpty())
			{
				StringBuffer sb = new StringBuffer(XMLResourceBundle.getBundledString("manageFailedMessage"));
				for (Pair<Page, File> pair : addBookAction.failed)
					sb.append("\n   -").append(pair.second.getName());
				JOptionPane.showMessageDialog(win, sb.toString());
			}
		}
		catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
	}
	
	public void appendPages(final Book book, final List<File> files)
	{
		if (files == null)
			return;
		final AddPagesAction addPagesAction = win.getActionProvider().addPages(book, files);
		try
		{
			win.historyManager.doAction(new WrappedAction(addPagesAction)
			{
				public void doAction() throws Exception {super.doAction(); refreshViewer(book); win.refreshTabNames();}
				public void undoAction() throws Exception {win.closePages(addPagesAction.pages); super.undoAction(); refreshViewer(book); win.refreshTabNames();}
			});
			if (!addPagesAction.failed.isEmpty())
			{
				StringBuffer sb = new StringBuffer(XMLResourceBundle.getBundledString("manageFailedMessage"));
				for (Pair<Page, File> pair : addPagesAction.failed)
					sb.append("\n   -").append(pair.second.getName());
				JOptionPane.showMessageDialog(win, sb.toString());
			}
		}
		catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
	}
	
	public void pageOpened(Page page)
	{
		if (win != null)
			try {win.addTab(page);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void bookOpened(Book book)
	{
		if (win != null)
			try {win.addTab(book);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
//	public void pagesProcessed(List<Page> pages)
//	{
//		if (win != null)
//		{
//			win.processDialog.setInput(pages);
//			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	 
//			GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
//			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gconf);
//			win.processDialog.setSize(screenSize.width-(screenInsets.left+screenInsets.right) , screenSize.height-(screenInsets.bottom+screenInsets.top));
//			win.processDialog.redoLayout();
//			win.processDialog.setVisible(true);
//		}
//	}
//	
//	public void pagesExported(List<Page> pages)
//	{
//		if (win != null)
//		{
//			win.exportDialog.setInput(pages);
//			GuiUtils.centerOnScreen(win.exportDialog);
//			win.exportDialog.setVisible(true);
//		}
//	}
	
	public boolean booksDeleted(final List<Book> books)
	{
		StringBuffer sb = new StringBuffer("<html>");
		sb.append(XMLResourceBundle.getBundledString("manageDeleteBookMsg")).append("<br>");
		for (Book book : books)
			sb.append("&nbsp;&nbsp;&nbsp;- <b>").append(book.getName()).append("</b><br>");
		sb.append("</html>");
		if (JOptionPane.showConfirmDialog(win, sb.toString(), "", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
			return false;
		
		final DeleteBooksAction deleteBooksAction = win.getActionProvider().deleteBooks(books);
		try
		{
			win.historyManager.doAction(new WrappedAction(deleteBooksAction)
			{
				public void doAction() throws Exception {win.closeBooks(books); super.doAction(); win.manageComponent.reload();}
				public void undoAction() throws Exception {super.undoAction(); win.manageComponent.reload();}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		return true;
	}
	
	void refreshViewer(Book book)
	{
		DocumentPanel panel = win.getPanelForDocument(book);
		if (panel == null)
			return;
		try {panel.bookViewer.setDocument(book);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public boolean pagesDeleted(final List<Page> pages)
	{
		StringBuffer sb = new StringBuffer("<html>");
		sb.append(XMLResourceBundle.getBundledString("manageDeletePageMsg"));
		sb.append(" (").append(pages.size()).append(")");
		sb.append("</html>");
		if (JOptionPane.showConfirmDialog(win, sb.toString(), "", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
			return false;
		
		final DeletePagesAction deletePagesAction = win.getActionProvider().deletePages(pages);
		try
		{
			final Book book = pages.get(0).getBook();
			win.historyManager.doAction(new WrappedAction(deletePagesAction)
			{
				public void doAction() throws Exception {win.closePages(pages); super.doAction(); refreshViewer(book); win.refreshTabNames();}
				public void undoAction() throws Exception {super.undoAction(); refreshViewer(book); win.refreshTabNames();}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		return true;
	}
}
