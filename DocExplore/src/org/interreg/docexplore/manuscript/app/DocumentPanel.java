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

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

/**
 * The top level panel handling an annotated object. It is comprised of a view panel 
 * and a meta data panel.
 * @author Burnett
 */
public class DocumentPanel extends JSplitPane implements DocumentEditorHost
{
	private static final long serialVersionUID = -3062167501953632763L;
	
	public final ManuscriptAppHost host;
	private AnnotatedObject document;
	private DocumentPanelEditor editor;
	public final EditorSidePanel sidePanel;
	private String message = "";
	
	public DocumentPanel(final ManuscriptAppHost win, AnnotatedObject document, Object param) throws Exception
	{
		super(JSplitPane.HORIZONTAL_SPLIT);
		
		this.host = win;
		this.document = document;
		this.editor = win.getEditorForDocument(this, document, param);
		this.sidePanel = editor.allowsSidePanel() ? win.buildSidePanelForEditor(editor) : null;
		
		JPanel viewPanel = new JPanel(new BorderLayout());
		
		this.setDividerLocation(.5);
		setLeftComponent(viewPanel);
		if (sidePanel != null)
		{
			setRightComponent(sidePanel.getComponent());
			sidePanel.setDocument(document);
		}
		
		this.setContinuousLayout(true);
		this.setResizeWeight(.5);
		//this.setOneTouchExpandable(true);
		
		viewPanel.add(editor.getComponent(), BorderLayout.CENTER);
		win.notifyActiveDocumentChanged();
		
		addComponentListener(new ComponentListener()
		{
			boolean showing = false;
			@Override public void componentShown(ComponentEvent arg0) {showing = true; shown();}
			@Override public void componentHidden(ComponentEvent arg0) {if (!showing) return; showing = false; hidden();}
			@Override public void componentResized(ComponentEvent arg0) {}
			@Override public void componentMoved(ComponentEvent arg0) {}
		});
	}
	
	public void shown() {onShow();}
	public void hidden() {onHide();}
	
	@Override public ManuscriptAppHost getAppHost() {return host;}
	
	public void onActionRequest(String action, Object param)
	{System.out.println(">>>"+action);
		if (action.equals(DocumentEvents.collectionChanged.event))
		{
			if (document == null)
				refresh();
		}
		else if (action.equals(DocumentEvents.bookDeleted.event))
		{
			Book book = (Book)param;
			if (document instanceof Book && document.getId() == book.getId() ||
				document instanceof Page && ((Page)document).getBook().getId() == book.getId() ||
				document instanceof Region && ((Region)document).getPage().getBook().getId() == book.getId())
					host.removeDocument(document);
		}
		else if (action.equals(DocumentEvents.bookChanged.event))
		{
			Book book = (Book)param;
			if (document instanceof Book && document.getId() == book.getId() ||
				document instanceof Page && ((Page)document).getBook().getId() == book.getId() ||
				document instanceof Region && ((Region)document).getPage().getBook().getId() == book.getId())
					refresh();
		}
		else if (action.equals(DocumentEvents.pageDeleted.event))
		{
			Page page = (Page)param;
			if (document instanceof Page && ((Page)document).getId() == page.getId() ||
				document instanceof Region && ((Region)document).getPage().getId() == page.getId())
					host.removeDocument(document);
		}
		else if (action.equals(DocumentEvents.pageChanged.event))
		{
			Page page = (Page)param;
			if (document instanceof Book && document.getId() == page.getBook().getId() ||
				document instanceof Page && ((Page)document).getId() == page.getId())
					refresh();
		}
		else if (action.equals(DocumentEvents.regionDeleted.event))
		{
			Region region = (Region)param;
			switchDocument(region.getPage());
		}
		else if (action.equals(DocumentEvents.regionChanged.event))
		{
			Region region = (Region)param;
			if (document instanceof Page && document.getId() == region.getPage().getId() || 
				document instanceof Region && ((Region)document).getId() == region.getId())
					refresh();
		}
		else if (action.equals(DocumentEvents.metadataAdded.event) 
			|| action.equals(DocumentEvents.metadataDeleted.event) 
			|| action.equals(DocumentEvents.metadataChanged.event))
		{
			MetaData annotation = (MetaData)param;
			if (document instanceof MetaData && document.getId() == annotation.getId() ||
				document != null && document.hasMetaDataId(annotation.getId()))
					refresh();	
		}
			
		try {editor.onActionRequest(action, param);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	private void onShow()
	{
		editor.onShow();
		if (sidePanel != null)
			sidePanel.onShow();
	}
	private void onHide()
	{
		editor.onHide();
		if (sidePanel != null)
			sidePanel.onHide();
	}
	
	public void onDocumentActive()
	{
		host.setMessage(message);
	}
	
	public void refresh()
	{
		editor.refresh();
		if (sidePanel != null)
			try {sidePanel.setDocument(document);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public void setMessage(String s)
	{
		this.message = s;
		if (host.getActiveDocument() == this)
			host.setMessage(message);
	}
	
	/**
	 * Returns the target of this panel.
	 * @return
	 */
	public AnnotatedObject getDocument() {return document;}
	public DocumentPanelEditor getEditor() {return editor;}

	@Override public void switchDocument(AnnotatedObject to)
	{
		DocumentPanel panel = host.getDocument(to);
		if (panel != null && panel != this)
		{
			host.removeDocument(document);
			host.setActiveDocument(to);
			return;
		}
		document = to;
		try {if (sidePanel != null) sidePanel.setDocument(document);}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		validate();
		invalidate();
		repaint();
		host.refreshTabNames();
		host.notifyActiveDocumentChanged();
	}
	@Override public DocumentPanel onDocumentEditorRequest(AnnotatedObject document) {return host.addDocument(document);}
	@Override public void onCloseRequest() {editor.onCloseRequest(); host.removeDocument(document);}
	@Override public MetaData onAddAnnotationRequest() {return sidePanel.addAnnotation();}
	@Override public void onAnalysisRequest(BufferedImage image) {host.plugins.analysisPluginSetup.addAnalysisInput(image);}
	@Override public void onActionStateRequestCompleted() {if (host.getActiveDocument() == this) host.notifyActiveDocumentChanged();}
}
