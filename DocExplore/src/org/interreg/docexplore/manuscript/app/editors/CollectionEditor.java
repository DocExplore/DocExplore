/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.util.ImageUtils;

public class CollectionEditor extends JPanel implements DocumentPanelEditor
{
	private static final long serialVersionUID = -3584515946932650576L;
	
	public final DocumentEditorHost host;
	public final CollectionBooksEditor books;
	
	protected EditorHeader topPanel;
	
	@SuppressWarnings("serial")
	public CollectionEditor(final DocumentEditorHost host) throws DataLinkException
	{
		super(new BorderLayout());
		
		this.host = host;
		
		this.books = new CollectionBooksEditor(this);
		add(books, BorderLayout.CENTER);
		
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			books.onActionRequest("delete", null);
		}});
		
		topPanel = new EditorHeader();
		refreshTitle();
	}
	
	private void refreshTitle()
	{
		try
		{
			int nBooks = host.getAppHost().getLink().getLink().getAllBookIds().size();
			topPanel.setTitle(Lang.s("generalCollection"), nBooks+" "+Lang.s(nBooks == 1 ? "generalBook" : "generalBooks")); 
			topPanel.setTitleIcon(ImageUtils.getIcon("books-64x64.png"));
			add(topPanel, BorderLayout.NORTH);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	@Override public void refresh()
	{
		refreshTitle();
		books.refresh();
	}
	
	@Override public Component getComponent() {return this;}
	public void onActionRequest(String action) throws Exception {onActionRequest(action, null);}
	@Override public void onActionRequest(String action, Object param) throws Exception
	{
		books.onActionRequest(action, param);
	}
	@Override public void onShow() {}
	@Override public void onHide() {}
	@Override public void onCloseRequest() {}
	@Override public boolean allowsSidePanel() {return true;}
}
