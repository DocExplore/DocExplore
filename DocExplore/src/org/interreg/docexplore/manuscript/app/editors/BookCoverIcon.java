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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.FileDialogs;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.CoverUtils;
import org.interreg.docexplore.manuscript.CoverUtils.Part;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;

@SuppressWarnings("serial")
public class BookCoverIcon extends DragDropPanel
{
	BookCoverEditor coverEditor;
	JLabel label;
	Part part;
	
	public BookCoverIcon(BookCoverEditor coverEditor, int size, Part part)
	{
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		this.coverEditor = coverEditor;
		this.label = new JLabel();
		this.part = part;
		
		setOpaque(false);
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		add(label);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(size, size));
		
		addMouseListener(new MouseAdapter() {@Override public void mouseReleased(MouseEvent e)
		{
			if (coverEditor.readOnly)
				return;
			if (e.getButton() == MouseEvent.BUTTON3 && label.getIcon() != null)
				try {coverEditor.setPart(null, part.ordinal());}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
			{
				try
				{
					MetaData data = getData();
					if (data != null)
						coverEditor.bookEditor.docEditor.host.onDocumentEditorRequest(data);
					else
					{
						FileDialogs files = DocExploreTool.getFileDialogs();
						File file = files.openFile(DocExploreTool.getImagesCategory());
						if (file == null)
							return;
						DocExploreDataLink link = coverEditor.bookEditor.docEditor.host.getAppHost().getLink();
						MetaData part = new MetaData(link, BookCoverIcon.this.part.getKey(link), MetaData.imageType, new FileInputStream(file));
						coverEditor.setPart(part, BookCoverIcon.this.part.ordinal());
					}
				}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		}});
	}
	
	public MetaData getData() throws Exception
	{
		DocExploreDataLink link = coverEditor.bookEditor.docEditor.host.getAppHost().getLink();
		MetaDataKey key = part.getKey(link);
		List<MetaData> parts = coverEditor.bookEditor.docEditor.book.getMetaDataListForKey(key);
		if (parts.isEmpty())
			return null;
		else return parts.get(0);
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
