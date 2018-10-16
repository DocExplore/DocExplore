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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.authoring.explorer.edit.ImportOptions;
import org.interreg.docexplore.authoring.explorer.edit.StyleDialog;
import org.interreg.docexplore.authoring.explorer.edit.StyleManager;
import org.interreg.docexplore.authoring.rois.RegionSidePanel;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.ActionRequestListener;
import org.interreg.docexplore.manuscript.app.DocumentActionHandler;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.manuscript.app.EditorSidePanel;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;
import org.interreg.docexplore.manuscript.app.editors.ImageMetaDataEditor;
import org.interreg.docexplore.manuscript.app.editors.PosterPageEditor;

public class ATAppHost extends ManuscriptAppHost implements ManuscriptAppHost.DocExploreActionListener, ManuscriptAppHost.AppListener
{
	public final ATApp app;
	CollectionExplorer explorer;
	public final MetaDataClipboard clipboard;
	public final StyleManager styles;
	public final ImportOptions importOptions;
	public final BookImporter importer;
	public final ReaderExporter readerExporter;
	public final WebExporter webExporter;
	public final ExportDialog exportDialog;
	public final NameDialog nameDialog;
	
	public ATAppHost(ATApp win, Startup startup)
	{
		super(startup, win);
		this.app = win;
		this.clipboard = new MetaDataClipboard(app, new File(DocExploreTool.getHomeDir(), ".at-clipboard"));
		this.styles = new StyleManager() {@Override public void stylesChanged(StyleDialog dialog) {broadcastAction("styles-changed");}};
		this.importOptions = new ImportOptions(styles);
		this.importer = new BookImporter();
		this.readerExporter = new ReaderExporter(this, styles);
		this.webExporter = new WebExporter(this, styles);
		this.exportDialog = new ExportDialog(this);
		this.nameDialog = new NameDialog(this);
		addAppListener(this);
		addDocExploreActionListener(DocumentEvents.collectionChanged.event, this);
		
		try {this.explorer = new CollectionExplorer(win.libLink.getLink(), this);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	@Override public EditorSidePanel buildSidePanelForEditor(DocumentPanelEditor editor)
	{
		if (editor instanceof ManuscriptEditor)
			return explorer;
		if (editor instanceof PosterPageEditor)
			return new RegionSidePanel(this);
		return null;
	}
	@Override public void setMessage(String s) {app.statusBar.setMessage(s);}
	@Override public ActionRequestListener getActionRequestListener() {return app.documentActionHandler;}
	@Override public DocumentPanelEditor getEditorForDocument(DocumentPanel panel, AnnotatedObject document, Object param)
	{
		try
		{
			if (document instanceof Book) return new ATManuscriptEditor(this, panel, (Book)document, param);
			if (document instanceof Page) return new ATPageEditor(panel, (Page)document);
			if (document instanceof Region) return new ATPageEditor(panel, (Region)document);
			if (document instanceof MetaData && ((MetaData)document).getType().equals(MetaData.imageType)) return new ImageMetaDataEditor(panel, (MetaData)document);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}

	@Override public DocumentPanel getActiveDocument() {return app.document;}
	@Override public void setActiveDocument(AnnotatedObject document) {}
	public DocumentPanel getDocument(AnnotatedObject document)
	{
		if (app.isCurrentDocument(document))
			return app.document;
		return null;
	}
	@Override public void getDocuments(Collection<DocumentPanel> documents)
	{
		documents.add(app.document);
	}
	@Override public DocumentPanel addDocument(AnnotatedObject document, Object param)
	{
		DocumentPanel panel = app.setPanel(document, param);
		notifyActiveDocumentChanged();
		return panel;
	}
	@Override public void removeDocument(AnnotatedObject document) {if (app.isCurrentDocument(document)) app.removePanel();}
	
	@Override public void dataLinkChanged(DocExploreDataLink link)
	{
		app.documentActionHandler = new DocumentActionHandler(this);
		app.documentPanel.removeAll();
		app.resetComponents();
		
		try
		{
			link.setProperty("autoWrite", false);
			List<Integer> books = link.getLink().getAllBookIds();
			if (books.isEmpty())
			{
				Book book = new Book(link, Lang.s("collectionDefaultBookLabel"));
				books = new LinkedList<Integer>();
				books.add(book.getId());
			}
			app.setPanel(link.getBook(books.get(0)), null);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	@Override public void refreshTabNames() {}
	@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document) {}

	@Override public void onAction(String action, Object param)
	{
		
	}
}
