package org.interreg.docexplore.authoring;

import java.util.Collection;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.app.ActionRequestListener;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.manuscript.app.EditorSidePanel;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.editors.CollectionEditor;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;

public class CollectionExplorerHost extends ManuscriptAppHost
{
	CollectionExplorer app;
	
	public CollectionExplorerHost(CollectionExplorer app, ATAppHost parent)
	{
		super(null, null);
		this.helpPanel = parent.helpPanel;
		this.app = app;
	}
	
	@Override public ActionRequestListener getActionRequestListener() {return null;}

	@Override public DocumentPanel getActiveDocument() {return app.document;}
	@Override public void setActiveDocument(AnnotatedObject document) {}
	@Override public DocumentPanel getDocument(AnnotatedObject document)
	{
		if (app.isCurrentDocument(document))
			return app.document;
		return null;
	}

	@Override public void getDocuments(Collection<DocumentPanel> documents) {documents.add(app.document);}

	@Override public DocumentPanel addDocument(AnnotatedObject document, Object param)
	{
		DocumentPanel panel = app.setPanel(document, param);
		notifyActiveDocumentChanged();
		return panel;
	}

	@Override public void removeDocument(AnnotatedObject document) {if (app.isCurrentDocument(document)) app.removePanel();}

	@SuppressWarnings("serial")
	@Override public DocumentPanelEditor getEditorForDocument(DocumentPanel panel, AnnotatedObject document, Object param)
	{
		try
		{
			if (document == null) return new CollectionEditor(panel);
			if (document instanceof Book) return new ManuscriptEditor(panel, (Book)document, param) {
			{
				topPanel.titlePanel.add(new ExplorerToolBar(CollectionExplorerHost.this, true));
				configurationEditor.setReadOnly(true);
				canOpenPages = false;
			}};
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}

	@Override public EditorSidePanel buildSidePanelForEditor(DocumentPanelEditor editor) {return null;}
	@Override public void setMessage(String s) {}
	@Override public void refreshTabNames() {}
}
