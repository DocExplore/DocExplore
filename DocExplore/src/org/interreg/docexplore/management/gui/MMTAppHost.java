/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.gui;

import java.util.Collection;

import org.interreg.docexplore.Startup;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.annotate.MMTAnnotationPanel;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.ActionRequestListener;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.manuscript.app.EditorSidePanel;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.editors.CollectionEditor;
import org.interreg.docexplore.manuscript.app.editors.ImageMetaDataEditor;

public class MMTAppHost extends ManuscriptAppHost implements ManuscriptAppHost.DocExploreActionListener, ManuscriptAppHost.AppListener
{
	MMTApp app;
	
	public MMTAppHost(MMTApp win, Startup startup)
	{
		super(startup, win);
		this.app = win;
		addAppListener(this);
		addDocExploreActionListener(DocumentEvents.collectionChanged.event, this);
	}
	
	@Override public EditorSidePanel buildSidePanelForEditor(DocumentPanelEditor editor)
	{
		return editor instanceof CollectionEditor ? null : new MMTAnnotationPanel(this);
	}
	@Override public void setMessage(String s) {app.statusBar.setMessage(s);}
	@Override public ActionRequestListener getActionRequestListener() {return app.manageComponent.handler;}
	@Override public DocumentPanelEditor getEditorForDocument(DocumentPanel panel, AnnotatedObject document, Object param)
	{
		try
		{
			if (document == null) return new MMTCollectionEditor(panel);
			if (document instanceof Book) return new MMTManuscriptEditor(panel, (Book)document, param);
			if (document instanceof Page) return new MMTPageEditor(panel, (Page)document);
			if (document instanceof Region) return new MMTPageEditor(panel, (Region)document);
			if (document instanceof MetaData && ((MetaData)document).getType().equals(MetaData.imageType)) return new ImageMetaDataEditor(panel, (MetaData)document);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}

	@Override public DocumentPanel getActiveDocument() {return app.getActiveTab();}
	@Override public void setActiveDocument(AnnotatedObject document)
	{
		int index = app.getIndexForDocument(document);
		if (index < 0)
			return;
		app.tabbedPane.setSelectedIndex(index);
		notifyActiveDocumentChanged();
	}
	@Override public DocumentPanel getDocument(AnnotatedObject document)
	{
		int index = app.getIndexForDocument(document);
		if (index < 0)
			return null;
		return (DocumentPanel)app.tabbedPane.getComponentAt(index);
	}
	@Override public void getDocuments(Collection<DocumentPanel> documents)
	{
		for (int i=app.tabbedPane.getTabCount()-1;i>=0;i--)
			documents.add((DocumentPanel)app.tabbedPane.getComponentAt(i));
	}
	@Override public DocumentPanel addDocument(AnnotatedObject document, Object param)
	{
		DocumentPanel panel = app.addTab(document, param);
		notifyActiveDocumentChanged();
		return panel;
	}
	@Override public void removeDocument(AnnotatedObject document) {app.removeTab(app.getIndexForDocument(document));}

	@Override public void refreshTabNames() {app.refreshTabNames();}

	@Override public void onAction(String action, Object param)
	{
		if (action.equals(DocumentEvents.collectionChanged.event))
			app.manageComponent.refresh();
	}

	@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
	{
		
	}

	@Override public void dataLinkChanged(DocExploreDataLink link)
	{
		app.tabbedPane.removeAll();
		app.resetComponents();
		if (link != null)
			addDocument(null);
	}
}
