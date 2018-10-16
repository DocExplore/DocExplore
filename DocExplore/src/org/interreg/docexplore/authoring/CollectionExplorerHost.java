/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
