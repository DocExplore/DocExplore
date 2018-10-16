/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddRegionsAction;
import org.interreg.docexplore.manuscript.actions.CropPageAction;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.manuscript.app.ActionRequestListener;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;

public class SlideEditorListener implements DocumentEditorHost, ActionRequestListener
{
	SlideEditor editor;
	SlideEditorListener() {}
	
	@Override public void setMessage(String s) {}
	@Override public void switchDocument(AnnotatedObject document) {}
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
	@Override public void onAddEmptyPartRequest(final Book book, int col, int row, boolean insertRow) {}
	@Override public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation) {return null;}
	@Override public void onFillPosterHolesRequest(Book book) {}
	@Override public void onHorizontalMirrorPartsRequest(Book book) {}
	@Override public void onVerticalMirrorPartsRequest(Book book) {}
	@Override public void onRotatePartsLeftRequest(Book book) {}
	@Override public void onRotatePartsRightRequest(Book book) {}
	@Override public void onRotateMetaDataLeftRequest(MetaData annotation) {}
	@Override public void onRotateMetaDataRightRequest(MetaData annotation) {}
	@Override public void onHorizontalMirrorMetaDataRequest(MetaData annotation) {}
	@Override public void onVerticalMirrorMetaDataRequest(MetaData annotation) {}
	
	@Override public void onHorizontalMirrorPageRequest(Page page) {}
	@Override public void onVerticalMirrorPageRequest(Page page) {}
	@Override public void onRotatePageLeftRequest(Page page) {}
	@Override public void onRotatePageRightRequest(Page page) {}
	
	@Override public void onCropPageRequest(AnnotatedObject object, int tlx, int tly, int brx, int bry)
	{
		try 
		{
			CropPageAction action = editor.getHost().getAppHost().getLink().actionProvider().cropPage(object, tlx, tly, brx, bry);
			editor.getHost().getAppHost().historyManager.submit(new WrappedAction(action)
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
			regionsImported(editor.getPage(), Collections.singletonList(region));
			editor.switchDocument(region);
		} 
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	@Override public void onDeleteRegionRequest(Region region)
	{
		try 
		{
			DeleteRegionsAction deleteRegionsAction =editor.getHost().getAppHost().getLink().actionProvider().deleteRegion(region);
			editor.getHost().getAppHost().historyManager.submit(new WrappedAction(deleteRegionsAction)
			{
				public void doAction() throws Exception {super.doAction(); editor.reloadPage();}
				public void undoAction() throws Exception {super.undoAction(); editor.reloadPage();}
			});
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	@Override public ManuscriptAppHost getAppHost() {return null;}

	@Override public void onAddRetroactiveAnnotationsRequest(AnnotatedObject object, List<MetaData> annotations) {}
	
	public void regionsImported(Page page, Collection<Region> regions)
	{
		try
		{
			final AddRegionsAction action = editor.getHost().getAppHost().getLink().actionProvider().addRegions(page, null);
			editor.getHost().getAppHost().historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception
				{
					action.cacheDir = cacheDir;
					if (action.regions.isEmpty()) 
						return; 
					super.doAction(); 
					editor.reloadPage();
				}
				public void undoAction() throws Exception {super.undoAction(); editor.reloadPage();}
			});
			action.regions.addAll(regions);
		}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
}
