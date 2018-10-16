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

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.CoverUtils.Part;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.util.history.ReversibleAction;

@SuppressWarnings("serial")
public class BookCoverEditor extends JPanel
{
	BookEditor bookEditor;
	BookCoverIcon [] parts = new BookCoverIcon [Part.values().length];
	boolean readOnly = false;
	
	public BookCoverEditor(BookEditor bookEditor, int iconSize)
	{
		super(new LooseGridLayout(3, 5, 10, 10, false, false, SwingConstants.CENTER, SwingConstants.CENTER, false, false));
		this.bookEditor = bookEditor;
		
		add(buildLabel(""));
		add(buildLabel(Lang.s("coverFront")));
		add(buildLabel(Lang.s("coverInnerFront")));
		add(buildLabel(Lang.s("coverInnerBack")));
		add(buildLabel(Lang.s("coverBack")));
		
		add(buildLabel(Lang.s("coverOpaque")));
		add(buildIcon(iconSize, Part.Front));
		add(buildIcon(iconSize, Part.FrontInner));
		add(buildIcon(iconSize, Part.BackInner));
		add(buildIcon(iconSize, Part.Back));
		
		add(buildLabel(Lang.s("coverTransparent")));
		add(buildIcon(iconSize, Part.FrontTrans));
		add(buildIcon(iconSize, Part.FrontInnerTrans));
		add(buildIcon(iconSize, Part.BackInnerTrans));
		add(buildIcon(iconSize, Part.BackTrans));
		
		setOpaque(false);
	}
	
	public void setReadOnly(boolean b)
	{
		this.readOnly = b;
		for (int i=0;i<parts.length;i++)
			parts[i].dropsEnabled = !b;
	}
	
	private JLabel buildLabel(String text)
	{
		return new JLabel(text);
	}
	private BookCoverIcon buildIcon(int size, Part part)
	{
		return parts[part.ordinal()] = new BookCoverIcon(this, size, part);
	}
	
	void setPart(MetaData part, int index) throws Throwable
	{
		DocExploreDataLink link = bookEditor.docEditor.host.getAppHost().getLink();
		List<MetaData> parts = bookEditor.docEditor.book.getMetaDataListForKey(Part.values()[index].getKey(link));
		ActionProvider provider = link.actionProvider();
		AddMetaDataAction add = part == null ? null : provider.addMetaData(bookEditor.docEditor.book, part);
		DeleteMetaDataAction delete = parts.isEmpty() ? null : provider.deleteMetaData(bookEditor.docEditor.book, parts.get(0));
		bookEditor.docEditor.host.getAppHost().historyManager.submit(new ReversibleAction()
		{
			@Override public void doAction() throws Exception
			{
				if (delete != null) delete.doAction();
				if (add != null) add.doAction();
				DocumentEvents.broadcastChanged(bookEditor.docEditor.host.getAppHost(), bookEditor.docEditor.book);
			}
			@Override public void undoAction() throws Exception
			{
				if (add != null) add.undoAction();
				if (delete != null) delete.undoAction();
				DocumentEvents.broadcastChanged(bookEditor.docEditor.host.getAppHost(), bookEditor.docEditor.book);
			}
			@Override public String description() {return part != null ? Lang.s("addCover") : Lang.s("deleteCover");}
		});
	}

	public void refresh()
	{
		try
		{
			for (int i=0;i<parts.length;i++)
			{
				MetaData data = parts[i].getData();
				if (data == null)
					parts[i].label.setIcon(null);
				else parts[i].label.setIcon(new ImageIcon(DocExploreDataLink.getImageMini(data)));
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
