/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app;

import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.interreg.docexplore.datalink.DataLink.DataLinkSource;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddBookAction;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.AddPagesAction;
import org.interreg.docexplore.manuscript.actions.AddPosterPartsAction;
import org.interreg.docexplore.manuscript.actions.AddRegionsAction;
import org.interreg.docexplore.manuscript.actions.CropPageAction;
import org.interreg.docexplore.manuscript.actions.DeleteBooksAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.actions.DeletePagesAction;
import org.interreg.docexplore.manuscript.actions.DeletePosterPartsAction;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;
import org.interreg.docexplore.manuscript.actions.FillPosterAction;
import org.interreg.docexplore.manuscript.actions.RetroactiveAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.util.Pair;


public class DocumentActionHandler implements ActionRequestListener
{
	ManuscriptAppHost win;
	DocExploreDataLink link;
	
	public DocumentActionHandler(ManuscriptAppHost win)
	{
		this.win = win;
		this.link = win.getLink();
	}
	public DocumentActionHandler(DataLinkSource source) throws DataLinkException
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

	public Book onAddBookRequest(String title, List<File> files, boolean poster)
	{
		final AddBookAction addBookAction = link.actionProvider().addBook(title, files, poster);
		try
		{
			win.historyManager.submit(new WrappedAction(addBookAction)
			{
				public void doAction() throws Exception {super.doAction(); 
					win.broadcastAction(DocumentEvents.bookAdded.event, addBookAction.book); 
					win.broadcastAction(DocumentEvents.collectionChanged.event);}
				public void undoAction() throws Exception {super.undoAction(); 
					win.broadcastAction(DocumentEvents.bookDeleted.event, addBookAction.book); 
					win.broadcastAction(DocumentEvents.collectionChanged.event);}
			});
			if (!addBookAction.failed.isEmpty())
			{
				StringBuffer sb = new StringBuffer(Lang.s("manageFailedMessage"));
				for (Pair<AnnotatedObject, File> pair : addBookAction.failed)
					sb.append("\n   -").append(pair.second.getName());
				JOptionPane.showMessageDialog(win.getFrame(), sb.toString());
			}
			return addBookAction.book;
		}
		catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
		return null;
	}
	public void onDeleteBooksRequest(final List<Book> books)
	{
		StringBuffer sb = new StringBuffer("<html>");
		sb.append(Lang.s("manageDeleteBookMsg")).append("<br>");
		for (Book book : books)
			sb.append("&nbsp;&nbsp;&nbsp;- <b>").append(book.getName()).append("</b><br>");
		sb.append("</html>");
		if (JOptionPane.showConfirmDialog(win.getFrame(), sb.toString(), "", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
			return;
		
		final DeleteBooksAction deleteBooksAction = link.actionProvider().deleteBooks(books);
		try
		{
			win.historyManager.submit(new WrappedAction(deleteBooksAction)
			{
				public void doAction() throws Exception {super.doAction(); 
					for (Book book : books) win.broadcastAction(DocumentEvents.bookDeleted.event, book);
					win.broadcastAction(DocumentEvents.collectionChanged.event);}
				public void undoAction() throws Exception {super.undoAction(); 
					for (Book book : books) win.broadcastAction(DocumentEvents.bookAdded.event, book); 
					win.broadcastAction(DocumentEvents.collectionChanged.event);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public List<Page> onAppendPagesRequest(final Book book, List<File> files)
	{
		final AddPagesAction addPagesAction = link.actionProvider().addPages(book, files);
		try
		{
			win.historyManager.submit(new WrappedAction(addPagesAction)
			{
				public void doAction() throws Exception {super.doAction(); for (Page page : addPagesAction.pages) 
					win.broadcastAction(DocumentEvents.pageAdded.event, page); 
					win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); 
					for (Page page : addPagesAction.pages) win.broadcastAction(DocumentEvents.pageDeleted.event, page); 
					win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
			if (!addPagesAction.failed.isEmpty())
			{
				StringBuffer sb = new StringBuffer(Lang.s("manageFailedMessage"));
				for (Pair<AnnotatedObject, File> pair : addPagesAction.failed)
					sb.append("\n   -").append(pair.second.getName());
				JOptionPane.showMessageDialog(win.getFrame(), sb.toString());
			}
			return addPagesAction.pages;
		}
		catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
		return null;
	}
	public List<MetaData> onAppendPartsRequest(final Book book, List<File> files)
	{
		final AddPosterPartsAction addPartsAction = link.actionProvider().addParts(book, files);
		try
		{
			win.historyManager.submit(new WrappedAction(addPartsAction)
			{
				public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
			if (!addPartsAction.failed.isEmpty())
			{
				StringBuffer sb = new StringBuffer(Lang.s("manageFailedMessage"));
				for (Pair<AnnotatedObject, File> pair : addPartsAction.failed)
					sb.append("\n   -").append(pair.second.getName());
				JOptionPane.showMessageDialog(win.getFrame(), sb.toString());
			}
			return addPartsAction.parts;
		}
		catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
		return null;
	}
	public void onDeletePagesRequest(final List<Page> pages)
	{
		StringBuffer sb = new StringBuffer("<html>");
		sb.append(Lang.s("manageDeletePageMsg"));
		sb.append(" (").append(pages.size()).append(")");
		sb.append("</html>");
		if (JOptionPane.showConfirmDialog(win.getFrame(), sb.toString(), "", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
			return;
		
		final DeletePagesAction deletePagesAction = link.actionProvider().deletePages(pages);
		try
		{
			final Book book = pages.get(0).getBook();
			win.historyManager.submit(new WrappedAction(deletePagesAction)
			{
				public void doAction() throws Exception {super.doAction(); 
					for (Page page : pages) win.broadcastAction(DocumentEvents.pageDeleted.event, page); 
					win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); 
					for (Page page : pages) win.broadcastAction(DocumentEvents.pageAdded.event, page); 
					win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onDeletePartsRequest(final Book book, List<MetaData> parts)
	{
		StringBuffer sb = new StringBuffer("<html>");
		sb.append(Lang.s("manageDeletePageMsg"));
		sb.append(" (").append(parts.size()).append(")");
		sb.append("</html>");
		if (JOptionPane.showConfirmDialog(win.getFrame(), sb.toString(), "", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION)
			return;
		
		final DeletePosterPartsAction deletePartsAction = link.actionProvider().deleteParts(book, parts);
		try
		{
			win.historyManager.submit(new WrappedAction(deletePartsAction)
			{
				public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void onMovePagesRequest(List<Page> pages, Page moveAfter)
	{
		try
		{
			final Book book = pages.get(0).getBook();
			win.historyManager.submit(new WrappedAction(link.actionProvider().movePages(pages, moveAfter))
			{
				public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onMovePartsRequest(final Book book, MetaData part, int col, int row, boolean insertRow)
	{
		try
		{
			win.historyManager.submit(new WrappedAction(link.actionProvider().movePart(book, part, col, row, insertRow))
			{
				public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onAddEmptyPartRequest(final Book book, int col, int row, boolean insertRow)
	{
		try
		{
			final AddMetaDataAction addPart = link.actionProvider().addMetaData(book, new MetaData(link, link.partKey, "empty"));
			MetaData [][] parts = PosterUtils.getBaseTilesArray(link, book);
			PosterUtils.setPartPos(link, addPart.annotations.get(0), 0, parts.length == 0 ? 0 : parts[0].length);
			win.historyManager.submit(new WrappedAction(link.actionProvider().movePart(book, addPart.annotations.get(0), col, row, insertRow))
			{
				public void doAction() throws Exception {addPart.doAction(); super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); addPart.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onFillPosterHolesRequest(final Book book)
	{
		try 
		{
			win.historyManager.submit(new FillPosterAction(link, book)
			{
				public void doAction() throws Exception {super.doAction();; win.broadcastAction(DocumentEvents.bookChanged.event, book);}
				public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onHorizontalMirrorPartsRequest(final Book book)
	{
		try {if (checkForHoles(book)) win.historyManager.submit(new WrappedAction(link.actionProvider().horizontalMirror(book))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			@Override public String description() {return Lang.s("mirrorParts");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onVerticalMirrorPartsRequest(final Book book)
	{
		try {if (checkForHoles(book)) win.historyManager.submit(new WrappedAction(link.actionProvider().verticalMirror(book))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			@Override public String description() {return Lang.s("mirrorParts");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onRotatePartsLeftRequest(final Book book)
	{
		try {if (checkForHoles(book)) win.historyManager.submit(new WrappedAction(link.actionProvider().rotateLeft(book))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			@Override public String description() {return Lang.s("rotateParts");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onRotatePartsRightRequest(final Book book)
	{
		try {if (checkForHoles(book)) win.historyManager.submit(new WrappedAction(link.actionProvider().rotateRight(book))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.bookChanged.event, book);}
			@Override public String description() {return Lang.s("rotateParts");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onRotateMetaDataLeftRequest(MetaData annotation)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().rotateLeft(annotation))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			@Override public String description() {return Lang.s("rotateImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onRotateMetaDataRightRequest(MetaData annotation)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().rotateRight(annotation))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			@Override public String description() {return Lang.s("rotateImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onHorizontalMirrorMetaDataRequest(MetaData annotation)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().horizontalMirror(annotation))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			@Override public String description() {return Lang.s("mirrorImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onVerticalMirrorMetaDataRequest(MetaData annotation)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().verticalMirror(annotation))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.metadataChanged.event, annotation);}
			@Override public String description() {return Lang.s("mirrorImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onHorizontalMirrorPageRequest(Page page)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().horizontalMirror(page))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			@Override public String description() {return Lang.s("mirrorImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onVerticalMirrorPageRequest(Page page)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().verticalMirror(page))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			@Override public String description() {return Lang.s("mirrorImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onRotatePageLeftRequest(Page page)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().rotateLeft(page))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			@Override public String description() {return Lang.s("rotateImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onRotatePageRightRequest(Page page)
	{
		try {win.historyManager.submit(new WrappedAction(link.actionProvider().rotateRight(page))
		{
			public void doAction() throws Exception {super.doAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			public void undoAction() throws Exception {super.undoAction(); win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			@Override public String description() {return Lang.s("rotateImage");}
		});}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	private boolean checkForHoles(Book book) throws DataLinkException
	{
		if (PosterUtils.posterHasHoles(link, book))
		{
			JOptionPane.showMessageDialog(win.getFrame(), Lang.s("imageFillPosterMessage"));
			return false;
		}
		return true;
	}
	
	public Region onAddRegionRequest(final Page page, Point [] outline)
	{
		try 
		{
			final AddRegionsAction action = link.actionProvider().addRegion(page, outline);
			win.historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception {super.doAction(); 
					win.broadcastAction(DocumentEvents.regionAdded.event, action.regions.get(0)); 
					win.broadcastAction(DocumentEvents.pageChanged.event, page);}
				public void undoAction() throws Exception {super.undoAction(); 
					win.broadcastAction(DocumentEvents.regionDeleted.event, action.regions.get(0)); 
					win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			});
			return action.regions.get(0);
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	public void onDeleteRegionRequest(final Region region)
	{
		try
		{
			final Page page = region.getPage();
			DeleteRegionsAction action = link.actionProvider().deleteRegion(region);
			win.historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception {super.doAction(); 
					win.broadcastAction(DocumentEvents.regionDeleted.event, region); 
					win.broadcastAction(DocumentEvents.pageChanged.event, page);}
				public void undoAction() throws Exception {super.undoAction(); 
					win.broadcastAction(DocumentEvents.regionAdded.event, region); 
					win.broadcastAction(DocumentEvents.pageChanged.event, page);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void onCropPageRequest(final AnnotatedObject object, int tlx, int tly, int brx, int bry)
	{
		try 
		{
			CropPageAction action = link.actionProvider().cropPage(object, tlx, tly, brx, bry);
			win.historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception {super.doAction(); 
					if (object instanceof Page) win.broadcastAction(DocumentEvents.pageChanged.event, (Page)object); 
					else win.broadcastAction(DocumentEvents.metadataChanged.event, (MetaData)object);}
				public void undoAction() throws Exception {super.undoAction(); 
					if (object instanceof Page) win.broadcastAction(DocumentEvents.pageChanged.event, (Page)object); 
					else win.broadcastAction(DocumentEvents.metadataChanged.event, (MetaData)object);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public MetaData onAddAnnotationRequest(final AnnotatedObject object, final MetaData annotation)
	{
		final AddMetaDataAction addMetDataAction = link.actionProvider().addMetaData(object, annotation);
		try
		{
			win.historyManager.submit(new WrappedAction(addMetDataAction)
			{
				public void doAction() throws Exception {super.doAction();
					DocumentEvents.broadcastChanged(win, object);
					win.broadcastAction(DocumentEvents.metadataAdded.event, annotation);}
				public void undoAction() throws Exception {super.undoAction();
					DocumentEvents.broadcastChanged(win, object);
					win.broadcastAction(DocumentEvents.metadataDeleted.event, annotation);}
			});
			return addMetDataAction.annotations.get(0);
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	public void onAddRetroactiveAnnotationsRequest(final AnnotatedObject object, final List<MetaData> annotations)
	{
		final AddMetaDataAction addMetDataAction = link.actionProvider().addMetaDatas(object, annotations);
		try
		{
			win.historyManager.submit(new RetroactiveAction(addMetDataAction)
			{
				public void doAction() throws Exception {super.doAction(); 
					DocumentEvents.broadcastChanged(win, object);}
				public void undoAction() throws Exception {super.undoAction(); 
					DocumentEvents.broadcastChanged(win, object);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onDeleteAnnotationRequest(final AnnotatedObject object, final MetaData annotation)
	{
		final DeleteMetaDataAction deleteMetDataAction = link.actionProvider().deleteMetaData(object, annotation);
		try
		{
			win.historyManager.submit(new WrappedAction(deleteMetDataAction)
			{
				public void doAction() throws Exception {super.doAction(); 
					DocumentEvents.broadcastChanged(win, object);	
					win.broadcastAction(DocumentEvents.metadataDeleted.event, annotation);}
				public void undoAction() throws Exception {super.undoAction();
					DocumentEvents.broadcastChanged(win, object);
					win.broadcastAction(DocumentEvents.metadataAdded.event, annotation);}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
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
}
