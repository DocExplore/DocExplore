package org.interreg.docexplore.management.gui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.annotate.AnnotationPanel;
import org.interreg.docexplore.management.image.BookViewer;
import org.interreg.docexplore.management.image.PageViewer;
import org.interreg.docexplore.management.image.PageViewer.ImageOperation;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.AddRegionsAction;
import org.interreg.docexplore.manuscript.actions.CropPageAction;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;

/**
 * The top level panel handling an annotated object. It is comprised of a view panel 
 * and a meta data panel.
 * @author Burnett
 */
public class DocumentPanel extends JSplitPane {

	private static final long serialVersionUID = -3062167501953632763L;
	
	public final MainWindow win;
	AnnotatedObject document;
	public final AnnotationPanel annotationPanel;
	
	public final BookViewer bookViewer;
	public final PageViewer pageViewer;
	public final JScrollPane viewerScrollPane;
	
	public DocumentPanel(final MainWindow win, final DocExploreDataLink link) throws DataLinkException
	{
		super(JSplitPane.HORIZONTAL_SPLIT);
		
		this.win = win;
		this.document = null;
		this.bookViewer = new BookViewer(win);
		this.pageViewer = new PageViewer();
		pageViewer.addListener(new PageViewer.Listener()
		{
			public void regionRemoved(Region region)
			{
				try
				{
					final Page page = region.getPage();
					DeleteRegionsAction action = win.getActionProvider().deleteRegion(region);
					win.historyManager.doAction(new WrappedAction(action)
					{
						public void doAction() throws Exception {super.doAction(); reloadViewer(page);}
						public void undoAction() throws Exception {super.undoAction(); reloadViewer(page);}
					});
					win.setActiveTabDocument(page); 
				}
				catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public void regionAdded(final Page page, Point [] outline)
			{
				try 
				{
					AddRegionsAction action = win.getActionProvider().addRegion(page, outline);
					win.historyManager.doAction(new WrappedAction(action)
					{
						public void doAction() throws Exception {super.doAction(); reloadViewer(page);}
						public void undoAction() throws Exception {super.undoAction(); reloadViewer(page); win.setActiveTabDocument(page);}
					});
					win.setActiveTabDocument(action.regions.get(0));
				}
				catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public void pageCropped(final Page page, int tlx, int tly, int brx, int bry)
			{
				try 
				{
					CropPageAction action = win.getActionProvider().cropPage(page, tlx, tly, brx, bry);
					win.historyManager.doAction(new WrappedAction(action)
					{
						public void doAction() throws Exception {super.doAction(); reloadViewer(page);}
						public void undoAction() throws Exception {super.undoAction(); reloadViewer(page);}
					});
				}
				catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public void objectSelected(AnnotatedObject object) {try {win.setActiveTabDocument(object);} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}}
			public void analysisRequested(BufferedImage image) {win.pluginManager.analysisPluginSetup.addAnalysisInput(image);}
			public void regionAnnotationRequested(Region region) {annotationPanel.addAnnotation();}
			public void operationSet(ImageOperation operation)
			{
				if (operation == PageViewer.defaultOperation)
					win.toolBar.unselectRoiButtons();
				win.statusBar.setMessage(operation.getMessage());
			}
		});
		
		JPanel viewPanel = new JPanel(new BorderLayout());
		this.viewerScrollPane = new JScrollPane(pageViewer);
		viewerScrollPane.setWheelScrollingEnabled(true);
		viewerScrollPane.getVerticalScrollBar().setUnitIncrement(15);
    	viewPanel.add(viewerScrollPane, BorderLayout.CENTER);
		
		annotationPanel = new AnnotationPanel(win);
		
		this.setDividerLocation(.5);
		setLeftComponent(viewPanel);
		setRightComponent(annotationPanel);
		
		this.setContinuousLayout(true);
		this.setResizeWeight(.5);
		//this.setOneTouchExpandable(true);
	}
	
	private void reloadViewer(Page page) throws DataLinkException
	{
		DocumentPanel panel = win.getPanelForPage(page);
		if (panel == null)
			return;
		panel.pageViewer.reload();
	}
	
	void documentIsClosing(AnnotatedObject next)
	{
		annotationPanel.contractAllAnnotations();
		if (document == null)
			return;
		if (next != null && DocExploreDataLink.isSamePage(document, next))
			return;
		
		if (document instanceof Page || document instanceof Region)
		{
			Page page = document instanceof Page ? (Page)document : ((Region)document).getPage();
			page.unloadRegions();
			page.unloadImage();
			page.unloadMetaData();
		}
		else if (document instanceof Book)
		{
			Book book = (Book)document;
			book.unloadMetaData();
		}
		//System.out.println("unload "+document.getCanonicalUri());
	}
	
	/**
	 * Sets the target of this panel and updates its sub panels.
	 * @param document An annotated object.
	 * @throws DataLinkException
	 */
	public void fillPanels(AnnotatedObject document) throws DataLinkException
	{
		if (this.document == document)
			return;
		setDocument(document);
	}
	void refresh() throws DataLinkException
	{
		setDocument(document);
	}
	void setDocument(AnnotatedObject document) throws DataLinkException
	{
		this.document = document;
		
		annotationPanel.setDocument(document);
		boolean isBook = document instanceof Book;
		if (isBook)
		{
			if (viewerScrollPane.getViewport().getView() != bookViewer)
				viewerScrollPane.setViewportView(bookViewer);
			bookViewer.setDocument(document);
		}
		else
		{
			if (viewerScrollPane.getViewport().getView() != pageViewer)
				viewerScrollPane.setViewportView(pageViewer);
			pageViewer.setDocument(document);
		}
		win.notifyActiveDocumentChanged();
	}
	
	/**
	 * Returns the target of this panel.
	 * @return
	 */
	public AnnotatedObject getDocument() {return document;}
}


