package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.management.gui.DocumentPanel;
import org.interreg.docexplore.management.manage.ActionRequestListener;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddPosterPartsAction;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

public class PresentationEditorListener implements DocumentEditorHost, ActionRequestListener
{
	PosterEditor editor;
	PresentationEditorView view;
	PresentationEditorListener(PresentationEditorView view) {this.view = view;}
	
	@Override public Book onAddBookRequest(String title, List<File> files, boolean poster) {return null;}
	@Override public void onDeleteBooksRequest(List<Book> books) {}
	@Override public List<Page> onAppendPagesRequest(Book book, List<File> files) {return null;}
	@Override public void onDeletePagesRequest(List<Page> pages) {}
	@Override public void onMovePagesRequest(List<Page> pages, Page moveAfter) {}
	@Override public void onCropPageRequest(AnnotatedObject object, int tlx, int tly, int brx, int bry) {}
	@Override public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation) {return null;}
	@Override public Region onAddRegionRequest(Page page, Point[] outline) {return null;}
	@Override public void onDeleteRegionRequest(Region region) {}
	
	@Override public void onTransposePartsRequest(Book book)
	{
		
	}
	
	@Override public List<MetaData> onAppendPartsRequest(final Book book, final List<File> files)
	{
		if (files.isEmpty())
			return new ArrayList<MetaData>(0);
		final AddPosterPartsAction [] action = {null};
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			public void run()
			{
				action[0] = view.explorer.getActionProvider().addParts(book, files);
				try {view.explorer.tool.historyManager.submit(action[0]);}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
			public float getProgress() {return (float)action[0].progress;}
		}, editor);
		view.explorer.explore("docex://"+view.curBook.getId());
		return action[0].parts;
	}

	@Override public void onDeletePartsRequest(final Book book, final List<MetaData> parts)
	{
		if (parts.isEmpty())
			return;
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try {view.explorer.tool.historyManager.submit(view.explorer.getActionProvider().deleteParts(book, parts));}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}, editor);
		view.explorer.explore("docex://"+view.curBook.getId());
	}
	
	@Override public void onMovePartsRequest(final Book book, final MetaData part, final int col, final int row, final boolean insertRow)
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try {view.explorer.tool.historyManager.submit(view.explorer.getActionProvider().movePart(book, part, col, row, insertRow));}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}, editor);
		view.explorer.explore("docex://"+view.curBook.getId());
	}

	@Override public DocExploreDataLink getLink() {return view.explorer.link;}
	@Override public ActionRequestListener getActionListener() {return this;}

	@Override public void setMessage(String s) {}
	
	@Override public void onDocumentSwitched(AnnotatedObject document)
	{
		
	}

	@Override public DocumentPanel onDocumentEditorRequest(AnnotatedObject document)
	{
		if (document instanceof Page)
			view.explorer.explore(document.getCanonicalUri());
		return null;
	}

	@Override public void onCloseRequest() {}
	@Override public MetaData onAddAnnotationRequest() {return null;}
	@Override public void onAnalysisRequest(BufferedImage image) {}
	@Override public void onActionStateRequestCompleted() {}

}
