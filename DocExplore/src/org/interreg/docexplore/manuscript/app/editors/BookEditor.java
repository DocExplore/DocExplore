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
