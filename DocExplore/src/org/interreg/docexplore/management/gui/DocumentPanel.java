/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.gui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.annotate.AnnotationPanel;
import org.interreg.docexplore.management.image.BookEditor;
import org.interreg.docexplore.management.image.PosterPageEditor;
import org.interreg.docexplore.management.manage.ActionRequestListener;
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
	
	public final MainWindow win;
	private AnnotatedObject document;
	private DocumentEditor editor;
	public final AnnotationPanel annotationPanel;
	String message = "";
	
	public DocumentPanel(final MainWindow win, AnnotatedObject document) throws Exception
	{
		super(JSplitPane.HORIZONTAL_SPLIT);
		
		this.win = win;
		this.document = document;
		this.annotationPanel = new AnnotationPanel(win);
		
		JPanel viewPanel = new JPanel(new BorderLayout());
		
		this.setDividerLocation(.5);
		setLeftComponent(viewPanel);
		setRightComponent(annotationPanel);
		
		this.setContinuousLayout(true);
		this.setResizeWeight(.5);
		//this.setOneTouchExpandable(true);
		
		this.editor = getEditorForDocument(document);
		annotationPanel.setDocument(document);
		viewPanel.add(editor.getComponent(), BorderLayout.CENTER);
		win.notifyActiveDocumentChanged();
	}
	
	public void onActionRequest(String action)
	{
		try {editor.onActionRequest(action);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void onActionStateRequest(String action, boolean state)
	{
		try {editor.onActionStateRequest(action, state);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	private DocumentEditor getEditorForDocument(AnnotatedObject document) throws Exception
	{
		if (document instanceof Book) return new BookEditor(this, (Book)document);
		if (document instanceof Page) return new PosterPageEditor(this, (Page)document);
		if (document instanceof Region) return new PosterPageEditor(this, (Region)document);
		return null;
	}
	
	public void goTo(String s) throws Exception {editor.goTo(s);}
	
	void documentIsClosing()
	{
		annotationPanel.contractAllAnnotations();
		editor.onClose();
	}
	
	public void refresh() {editor.refresh();}
	
	public void setMessage(String s)
	{
		this.message = s;
		if (win.getActiveTab() == this)
			win.statusBar.setMessage(s);
	}
	
	/**
	 * Returns the target of this panel.
	 * @return
	 */
	public AnnotatedObject getDocument() {return document;}
	public DocumentEditor getEditor() {return editor;}

	@Override public DocExploreDataLink getLink() {return win.getDocExploreLink();}
	@Override public ActionRequestListener getActionListener() {return win.manageComponent.handler;}
	
	@Override public void onDocumentSwitched(AnnotatedObject document)
	{
		int open = win.getIndexForDocument(document);
		int index = win.getIndexForDocument(this.document);
		if (open >= 0 && open != index)
		{
			win.removeTab(index);
			win.tabbedPane.setSelectedIndex(win.getIndexForDocument(document));
		}
		documentIsClosing();
		this.document = document;
		try {annotationPanel.setDocument(document);}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		win.refreshTabNames();
		win.notifyActiveDocumentChanged();
	}
	@Override public DocumentPanel onDocumentEditorRequest(AnnotatedObject document) {return win.addTab(document);}
	@Override public void onCloseRequest() {win.removeTab(win.getIndexForDocument(document));}
	@Override public MetaData onAddAnnotationRequest() {return annotationPanel.addAnnotation();}
	@Override public void onAnalysisRequest(BufferedImage image) {win.pluginManager.analysisPluginSetup.addAnalysisInput(image);}
	@Override public void onActionStateRequestCompleted() {if (win.getActiveTab() == this) win.notifyActiveDocumentChanged();}
}
