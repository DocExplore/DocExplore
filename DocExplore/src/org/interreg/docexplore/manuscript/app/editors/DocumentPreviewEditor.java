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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.util.history.ReversibleAction;

@SuppressWarnings("serial")
public class DocumentPreviewEditor extends DragDropPanel
{
	ManuscriptEditor docEditor;
	JLabel label;
	
	public DocumentPreviewEditor(ManuscriptEditor docEditor, int iconSize)
	{
		super(new BorderLayout());
		this.docEditor = docEditor;
		
		setOpaque(false);
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		add(label = new JLabel(), BorderLayout.CENTER);
		label.setPreferredSize(new Dimension(iconSize, iconSize));
		
		addMouseListener(new MouseAdapter() {@Override public void mouseReleased(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON3 && label.getIcon() != null)
				try {setPreview(null);}
				catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
	}
	
	public void setPreview(MetaData preview) throws Throwable
	{
		DocExploreDataLink link = docEditor.host.getAppHost().getLink();
		List<MetaData> parts = docEditor.book.getMetaDataListForKey(link.previewKey);
		ActionProvider provider = link.actionProvider();
		AddMetaDataAction add = preview == null ? null : provider.addMetaData(docEditor.book, preview);
		DeleteMetaDataAction delete = parts.isEmpty() ? null : provider.deleteMetaData(docEditor.book, parts.get(0));
		docEditor.host.getAppHost().historyManager.submit(new ReversibleAction()
		{
			@Override public void doAction() throws Exception
			{
				if (delete != null) delete.doAction();
				if (add != null) add.doAction();
				DocumentEvents.broadcastChanged(docEditor.host.getAppHost(), docEditor.book);
			}
			@Override public void undoAction() throws Exception
			{
				if (add != null) add.undoAction();
				if (delete != null) delete.undoAction();
				DocumentEvents.broadcastChanged(docEditor.host.getAppHost(), docEditor.book);
			}
			@Override public String description() {return preview != null ? Lang.s("addPreview") : Lang.s("deletePreview");}
		});
	}

	public void refresh()
	{
		try
		{
			DocExploreDataLink link = docEditor.host.getAppHost().getLink();
			List<MetaData> parts = docEditor.book.getMetaDataListForKey(link.previewKey);
			if (parts.isEmpty())
				label.setIcon(null);
			else label.setIcon(new ImageIcon(DocExploreDataLink.getImageMini(parts.get(0))));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Collection<Object> setDraggedData(int x, int y)
	{
		try
		{
			DocExploreDataLink link = docEditor.host.getAppHost().getLink();
			List<MetaData> parts = docEditor.book.getMetaDataListForKey(link.previewKey);
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

	@Override public void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor, 1));
		DocExploreDataLink link = docEditor.host.getAppHost().getLink();
		for (Object object : data)
		{
			if (object instanceof File) try
			{
				MetaData part = new MetaData(link, link.previewKey, MetaData.imageType, new FileInputStream((File)object));
				setPreview(part);
				break;
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			else if (object instanceof Page) try
			{
				Page page = (Page)object;
				MetaData part = new MetaData(link, link.previewKey, MetaData.imageType, page.getImage().getFile());
				page.unloadImage();
				setPreview(part);
				break;
			}
			catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			else if (object instanceof MetaData) try
			{
				MetaData part = new MetaData(link, link.previewKey, MetaData.imageType, ((MetaData)object).getValue());
				setPreview(part);
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
