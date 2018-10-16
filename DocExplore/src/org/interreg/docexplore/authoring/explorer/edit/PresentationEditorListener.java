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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddPosterPartsAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;
import org.interreg.docexplore.manuscript.app.ActionRequestListener;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

public class PresentationEditorListener implements DocumentEditorHost, ActionRequestListener
{
	PosterEditor editor;
	PresentationEditorListener() {}
	
	@Override public Book onAddBookRequest(String title, List<File> files, boolean poster) {return null;}
	@Override public void onDeleteBooksRequest(List<Book> books) {}
	@Override public List<Page> onAppendPagesRequest(Book book, List<File> files) {return null;}
	@Override public void onDeletePagesRequest(List<Page> pages) {}
	@Override public void onMovePagesRequest(List<Page> pages, Page moveAfter) {}
	@Override public void onCropPageRequest(AnnotatedObject object, int tlx, int tly, int brx, int bry) {}
	@Override public MetaData onAddAnnotationRequest(AnnotatedObject object, MetaData annotation) {return null;}
	@Override public Region onAddRegionRequest(Page page, Point[] outline) {return null;}
	@Override public void onDeleteRegionRequest(Region region) {}
	@Override public void onRotateMetaDataLeftRequest(MetaData annotation) {}
	@Override public void onRotateMetaDataRightRequest(MetaData annotation) {}
	@Override public void onHorizontalMirrorMetaDataRequest(MetaData annotation) {}
	@Override public void onVerticalMirrorMetaDataRequest(MetaData annotation) {}
	@Override public void onHorizontalMirrorPageRequest(Page page) {}
	@Override public void onVerticalMirrorPageRequest(Page page) {}
	@Override public void onRotatePageLeftRequest(Page page) {}
	@Override public void onRotatePageRightRequest(Page page) {}
	
	@Override public void onFillPosterHolesRequest(Book book)
	{
		try {getAppHost().historyManager.submit(new WrappedAction(getAppHost().getLink().actionProvider().fillPoster(book))
		{
			@Override public void doAction() throws Exception {super.doAction();}
			@Override public void undoAction() throws Exception {super.undoAction();}
		});}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	@Override public void onHorizontalMirrorPartsRequest(Book book)
	{
		try {if (checkForHoles(book)) getAppHost().historyManager.submit(new WrappedAction(getAppHost().getLink().actionProvider().horizontalMirror(book))
		{
			@Override public void doAction() throws Exception {super.doAction();}
			@Override public void undoAction() throws Exception {super.undoAction();}
		});}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	@Override public void onVerticalMirrorPartsRequest(Book book)
	{
		try {if (checkForHoles(book)) getAppHost().historyManager.submit(new WrappedAction(getAppHost().getLink().actionProvider().verticalMirror(book))
		{
			@Override public void doAction() throws Exception {super.doAction();}
			@Override public void undoAction() throws Exception {super.undoAction();}
		});}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	@Override public void onRotatePartsLeftRequest(Book book)
	{
		try {if (checkForHoles(book)) getAppHost().historyManager.submit(new WrappedAction(getAppHost().getLink().actionProvider().rotateLeft(book))
		{
			@Override public void doAction() throws Exception {super.doAction();}
			@Override public void undoAction() throws Exception {super.undoAction();}
		});}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	@Override public void onRotatePartsRightRequest(Book book)
	{
		try {if (checkForHoles(book)) getAppHost().historyManager.submit(new WrappedAction(getAppHost().getLink().actionProvider().rotateRight(book))
		{
			@Override public void doAction() throws Exception {super.doAction();}
			@Override public void undoAction() throws Exception {super.undoAction();}
		});}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
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
				action[0] = getAppHost().getLink().actionProvider().addParts(book, files);
				try {getAppHost().historyManager.submit(action[0]);}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
			public float getProgress() {return (float)action[0].progress;}
		}, editor);
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
				try {getAppHost().historyManager.submit(getAppHost().getLink().actionProvider().deleteParts(book, parts));}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}, editor);
	}
	
	@Override public void onMovePartsRequest(final Book book, final MetaData part, final int col, final int row, final boolean insertRow)
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				try {getAppHost().historyManager.submit(getAppHost().getLink().actionProvider().movePart(book, part, col, row, insertRow));}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}, editor);
	}
	
	@Override public void onAddEmptyPartRequest(final Book book, int col, int row, boolean insertRow)
	{
		throw new RuntimeException("Not implemented yet!");
	}
	
	private boolean checkForHoles(Book book) throws DataLinkException
	{
		if (PosterUtils.posterHasHoles(editor.host.getAppHost().getLink(), editor.getBook()))
		{
			JOptionPane.showMessageDialog(editor, Lang.s("imageFillPosterMessage"));
			return false;
		}
		return true;
	}

	@Override public void setMessage(String s) {}
	
	@Override public void switchDocument(AnnotatedObject document)
	{
		
	}

	@Override public DocumentPanel onDocumentEditorRequest(AnnotatedObject document)
	{
		if (document instanceof Page)
			editor.host.getAppHost().addDocument(document);
		return null;
	}

	@Override public void onCloseRequest() {}
	@Override public MetaData onAddAnnotationRequest() {return null;}
	@Override public void onAnalysisRequest(BufferedImage image) {}
	@Override public void onActionStateRequestCompleted() {}
	
	@Override public ManuscriptAppHost getAppHost() {return null;}

	@Override public void onAddRetroactiveAnnotationsRequest(AnnotatedObject object, List<MetaData> annotations) {}
}
