package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.CoverUtils;
import org.interreg.docexplore.manuscript.CoverUtils.Part;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;

@SuppressWarnings("serial")
public class BookCoverIcon extends DragDropPanel
{
	BookCoverEditor coverEditor;
	JLabel label;
	Part part;
	
	public BookCoverIcon(BookCoverEditor coverEditor, int size, Part part)
	{
		super(new BorderLayout());
		
		this.coverEditor = coverEditor;
		this.label = new JLabel();
		this.part = part;
		
		setOpaque(false);
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		add(label, BorderLayout.CENTER);
		label.setPreferredSize(new Dimension(size, size));
		
		addMouseListener(new MouseAdapter() {@Override public void mouseReleased(MouseEvent e)
		{
			if (!coverEditor.readOnly && e.getButton() == MouseEvent.BUTTON3 && label.getIcon() != null)
				try {coverEditor.setPart(null, part.ordinal());}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Collection<Object> setDraggedData(int x, int y)
	{
		try
		{
			DocExploreDataLink link = coverEditor.bookEditor.docEditor.host.getAppHost().getLink();
			List<MetaData> parts = coverEditor.bookEditor.docEditor.book.getMetaDataListForKey(part.getKey(link));
			if (!parts.isEmpty())
				return (Collection)parts;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}

	@Override public void onIncomingDrag(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		setBorder(BorderFactory.createLineBorder(GuiConstants.actionColor, 1));
	}

	@SuppressWarnings({ "rawtypes" })
	@Override public void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		DocExploreDataLink link = coverEditor.bookEditor.docEditor.host.getAppHost().getLink();
		for (Object object : data)
		{
			if (object instanceof File) try
			{
				MetaData part = new MetaData(link, this.part.getKey(link), MetaData.imageType, new FileInputStream((File)object));
				coverEditor.setPart(part, this.part.ordinal());
				break;
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			else if (object instanceof Page) try
			{
				Page page = (Page)object;
				MetaData part = new MetaData(link, this.part.getKey(link), MetaData.imageType, page.getImage().getFile());
				page.unloadImage();
				coverEditor.setPart(part, this.part.ordinal());
				break;
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			else if (object instanceof MetaData) try
			{
				MetaData part = new MetaData(link, this.part.getKey(link), MetaData.imageType, ((MetaData)object).getValue());
				coverEditor.setPart(part, this.part.ordinal());
				break;
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			else if (object instanceof Book && source instanceof IconPanelElement && ((IconPanelElement)source).editor instanceof CollectionBooksEditor) try
			{
				DocExploreDataLink sourceLink = ((CollectionBooksEditor)((IconPanelElement)source).editor).collectionEditor.host.getAppHost().getLink();
				MetaData [] images = CoverUtils.getCoverImages(sourceLink, (Book)object);
				coverEditor.bookEditor.docEditor.host.getAppHost().historyManager.submit(
					CoverUtils.importCover(coverEditor.bookEditor.docEditor.host.getAppHost(), coverEditor.bookEditor.docEditor.book, images));
				break;
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		}
	}

	@Override public void onDragExited(Collection<Object> data, DragDropPanel source)
	{
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
	}
}
