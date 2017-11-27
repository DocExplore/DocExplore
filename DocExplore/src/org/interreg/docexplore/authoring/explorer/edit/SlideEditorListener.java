package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.management.gui.DocumentPanel;
import org.interreg.docexplore.management.manage.ActionRequestListener;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.CropPageAction;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;

public class SlideEditorListener implements DocumentEditorHost, ActionRequestListener
{
	SlideEditor editor;
	SlideEditorView view;
	SlideEditorListener(SlideEditorView view) {this.view = view;}
	
	@Override public DocExploreDataLink getLink() {return view.explorer.link;}
	@Override public ActionRequestListener getActionListener() {return this;}
	@Override public void setMessage(String s) {}
	@Override public void onDocumentSwitched(AnnotatedObject document) {}
	@Override public DocumentPanel onDocumentEditorRequest(AnnotatedObject document) {return null;}
	@Override public void onCloseRequest() {}
	@Override public MetaData onAddAnnotationRequest() {return null;}
	@Override public void onAnalysisRequest(BufferedImage image) {}
	@Override public void onActionStateRequestCompleted()
	{
	}

	@Override public Book onAddBookRequest(String title, List<File> files, boolean poster) {return null;}
	@Override public void onDeleteBooksRequest(List<Book> books) {}
	@Override public List<Page> onAppendPagesRequest(Book book, List<File> files) {return null;}
	@Override public List<MetaData> onAppendPartsRequest(Book book, List<File> files) {return null;}
	@Override public void onDeletePagesRequest(List<Page> pages) {}
	@Override public void onDeletePartsRequest(Book book, List<MetaData> parts) {}
	@Override public void onMovePagesRequest(List<Page> pages, Page moveAfter) {}
	@Override public void onMovePartsRequest(Book book, MetaData part, int col, int row, boolean insertRow) {}
	@Override public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation) {return null;}
	@Override public void onTransposePartsRequest(Book book) {}
	
	@Override public void onCropPageRequest(Page page, int tlx, int tly, int brx, int bry)
	{
		try 
		{
			CropPageAction action = view.explorer.getActionProvider().cropPage(page, tlx, tly, brx, bry);
			view.explorer.tool.historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception {super.doAction(); editor.reloadPage();}
				public void undoAction() throws Exception {super.undoAction(); editor.reloadPage();}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	@Override public Region onAddRegionRequest(Page page, Point[] outline)
	{
		try
		{
			Region region = page.addRegion();
			region.setOutline(outline);
			view.explorer.regionsImported(view.curPage, Collections.singletonList(region));
			editor.switchDocument(region);
		} 
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	@Override public void onDeleteRegionRequest(Region region)
	{
		try 
		{
			DeleteRegionsAction deleteRegionsAction = view.explorer.getActionProvider().deleteRegion(region);
			view.explorer.tool.historyManager.submit(new WrappedAction(deleteRegionsAction)
			{
				public void doAction() throws Exception {super.doAction(); editor.reloadPage();}
				public void undoAction() throws Exception {super.undoAction(); editor.reloadPage();}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
