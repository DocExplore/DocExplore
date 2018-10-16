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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Page;

@SuppressWarnings("serial")
public class BookPagesEditor extends IconPanelEditor<Page>
{
	public final BookEditor bookEditor;
	private boolean readOnly;
	
	public BookPagesEditor(BookEditor bookEditor)
	{
		super(128);
		
		this.bookEditor = bookEditor;
		setReadOnly(readOnly);
		refresh();
	}
	
	public void setReadOnly(boolean b)
	{
		this.readOnly = b;
		dropsEnabled = !b;
	}

	@Override protected Collection<Page> getData()
	{
		try
		{
			int nPages = bookEditor.docEditor.book.getLastPageNumber();
			List<Page> pages = new ArrayList<Page>(nPages);
			for (int i=1;i<=nPages;i++)
				pages.add(bookEditor.docEditor.book.getPage(i));
			return pages;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return new ArrayList<Page>(0);
	}

	@Override protected void onIconInit(IconPanelElement<Page> icon)
	{
		try {icon.mini.setIcon(new ImageIcon(DocExploreDataLink.getImageMini(icon.data)));}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}

	@Override protected String labelFor(Page data)
	{
		String page = Lang.s("generalPage");
		int nRegions = 0;
		try {nRegions = data.getRegions().size();}
		catch (Exception e) {}
		return Character.toUpperCase(page.charAt(0))+page.substring(1)+" "+data.pageNum+" ("+nRegions+")";
	}

	@Override public Collection<Object> setDraggedData(int x, int y) {return null;}
	@Override public void onIncomingDrag(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		IconPanelElement<Page> icon = closestIcon(x, y);
		if (icon != null && !icon.isLeftOf(x, y) && !icon.isRightOf(x, y))
			icon = null;
		updateInsertionLocation(icon, icon != null ? icon.isRightOf(x, y) : false);
	}
	@Override public void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		updateInsertionLocation(null, false);
		IconPanelElement<Page> icon = closestIcon(x, y);
		if (icon == null && iconPanel.getComponentCount() > 0)
			return;
		if (icon != null && (!icon.isLeftOf(x, y) && !icon.isRightOf(x, y)))
			return;
		try {bookEditor.handleIncoming(data, source, icon == null ? null : icon.isLeftOf(x, y) ? icon.data : icon.data.pageNum == 1 ? null : icon.data.getBook().getPage(icon.data.pageNum-1));}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	@Override public void onDragExited(Collection<Object> data, DragDropPanel source)
	{
		updateInsertionLocation(null, false);
	}
	
	private IconPanelElement<Page> insertionIcon = null;
	private boolean insertionBefore = false;
	private void updateInsertionLocation(IconPanelElement<Page> icon, boolean before)
	{
		if (insertionIcon == icon && insertionBefore == before)
			return;
		this.insertionIcon = icon;
		this.insertionBefore = before;
		repaint();
	}
	@Override public void paintOver(Graphics2D g, int w, int h)
	{
		if (insertionIcon != null)
		{
			Rectangle rect = insertionIcon.getBounds();
			g.setColor(GuiConstants.actionColor);
			if (insertionBefore)
				g.fillRect(rect.x-10, rect.y, 10, rect.height);
			else g.fillRect(rect.x+rect.width, rect.y, 10, rect.height);
		}
	}

	@Override public boolean iconsAcceptDrops() {return false;}
	@Override public boolean iconsAcceptDrags() {return true;}
	@Override public void onIconOpened(IconPanelElement<Page> icon)
	{
		if (bookEditor.docEditor.canOpenPages)
			bookEditor.docEditor.host.onDocumentEditorRequest(icon.data);
	}
}
