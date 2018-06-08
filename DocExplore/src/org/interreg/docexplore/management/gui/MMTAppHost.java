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
