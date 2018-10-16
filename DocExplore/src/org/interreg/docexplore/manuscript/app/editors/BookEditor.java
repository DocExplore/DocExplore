/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.manage.SelectPagesPanel;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.app.DocumentEvents;

@SuppressWarnings("serial")
public class BookEditor extends JPanel implements ConfigurationEditor
{
	public final ManuscriptEditor docEditor;
	private boolean readOnly = false;
	BookPagesEditor pages;
	BookCoverEditor cover;
	
	public BookEditor(ManuscriptEditor bookEditor)
	{
		super(new BorderLayout());
		
		this.docEditor = bookEditor;
		add(this.pages = new BookPagesEditor(this), BorderLayout.CENTER);
		JPanel coverPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		coverPanel.setOpaque(false);
		coverPanel.add(new JLabel("<html><b>"+Lang.s("cover")+"</b></html>"));
		coverPanel.add(docEditor.host.getAppHost().helpPanel.createHelpMessageButton(Lang.s("helpCover")));
		coverPanel.add(this.cover = new BookCoverEditor(this, 128));
		add(coverPanel, BorderLayout.NORTH);
		
		setBackground(Color.white);
		setReadOnly(readOnly);
		refresh();
	}
	
	@Override public Component getComponent() {return this;}

	@Override public void onActionRequest(String action, Object param)
	{
		if (action.equals("add"))
		{
			List<File> files = SelectPagesPanel.show();
			if (files != null && !files.isEmpty())
				docEditor.host.getAppHost().broadcastAction(DocumentEvents.addPages.event, new Object [] {docEditor.book, files});
		}
		else if (action.equals("delete"))
		{
			List<Page> pages = this.pages.getSelectedElements();
			if (!readOnly && pages != null && !pages.isEmpty())
				docEditor.host.getAppHost().broadcastAction(DocumentEvents.deletePages.event, pages);
		}
	}
	
	@Override public void onCloseRequest() {}

	@Override public void setReadOnly(boolean b)
	{
		this.readOnly = b;
		pages.setReadOnly(b);
		cover.setReadOnly(b);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleIncoming(Collection<Object> data, Object source, Page moveAfter) throws Exception
	{
		//add from filesystem
		if (source == null)
		{
			List<File> files = new ArrayList<File>(data.size());
			files.addAll((Collection)data);
			docEditor.host.getAppHost().broadcastAction(ManuscriptEditor.importFilesRequestEvent, new Object [] {moveAfter, files});
		}
		//move pages 
		if (source instanceof IconPanelElement && ((IconPanelElement)source).editor == pages)
		{
			List<Page> pages = new ArrayList<Page>(data.size());
			pages.addAll((Collection)data);
			docEditor.host.getAppHost().broadcastAction(ManuscriptEditor.movePagesRequestEvent, new Object [] {moveAfter, pages});
		}
		//import pages 
		if (source instanceof IconPanelElement && ((IconPanelElement)source).editor != pages && ((IconPanelElement)source).editor instanceof BookPagesEditor)
		{
			List<Page> pages = new ArrayList<Page>(data.size());
			pages.addAll((Collection)data);
			docEditor.host.getAppHost().broadcastAction(ManuscriptEditor.importPagesRequestEvent, new Object [] {moveAfter, pages, 
				((BookPagesEditor)((IconPanelElement)source).editor).bookEditor.docEditor.host.getAppHost().getLink()});
		}
		//import books
		if (source instanceof IconPanelElement && ((IconPanelElement)source).editor != pages && ((IconPanelElement)source).editor instanceof CollectionBooksEditor)
		{
			List<Book> books = (List)data;
			List<Page> pages = new ArrayList<Page>();
			for (Book book : books)
				if (!PosterUtils.isPoster(book))
					for (int i=1;i<=book.getLastPageNumber();i++)
						pages.add(book.getPage(i));
			docEditor.host.getAppHost().broadcastAction(ManuscriptEditor.importPagesRequestEvent, new Object [] {moveAfter, pages, 
				((CollectionBooksEditor)((IconPanelElement)source).editor).collectionEditor.host.getAppHost().getLink()});
		}
	}

	@Override public void refresh()
	{
		pages.refresh();
		cover.refresh();
	}
	
	@Override public boolean allowGoto() {return true;}
}
