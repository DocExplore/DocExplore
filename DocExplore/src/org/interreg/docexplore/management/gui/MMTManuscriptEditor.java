/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.IconToggleButton;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class MMTManuscriptEditor extends ManuscriptEditor
{
	protected boolean isLocked;
	
	public MMTManuscriptEditor(DocumentEditorHost host, Book book, Object param) throws DataLinkException
	{
		super(host, book, param);
		
		isLocked = true;
		configurationEditor.setReadOnly(true);
		
		final Icon unlocked = ImageUtils.getIcon("unlocked-32x32.png");
		final Icon locked = ImageUtils.getIcon("locked-32x32.png");
		final IconToggleButton lockedButton = new IconToggleButton(locked);
		lockedButton.setSelected(true);
		lockedButton.setToolTipText(Lang.s("imageLockTooltip"));
		lockedButton.setFocusable(false);
		lockedButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			if (lockedButton.isSelected())
				lockedButton.setIcon(locked);
			else
			{
				if (JOptionPane.showConfirmDialog(MMTManuscriptEditor.this, Lang.s("imageUnlockMessage"), 
					Lang.s("imageLockTooltip"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						lockedButton.setIcon(unlocked);
				else
				{
					lockedButton.setSelected(true);
					lockedButton.setIcon(locked);
				}
			}
			isLocked = lockedButton.isSelected();
			configurationEditor.setReadOnly(isLocked);
		}});
		topPanel.rightPanel.add(lockedButton);
		
		topPanel.titlePanel.add(host.getAppHost().helpPanel.createHelpMessageButton(
			PosterUtils.isPoster(book) ? PosterUtils.isInStitches(book) ? 
				(PosterUtils.isInEditing(book) ? 
					Lang.s("helpPosterStitchEditMsg") 
					: PosterUtils.isInRendering(book) ? Lang.s("helpPosterStitchRenderMsg") : Lang.s("helpPosterStitchMsg"))
				: Lang.s("helpMmtPosterMsg") 
				: Lang.s("helpMmtBookMsg")));
		
		JButton editButton = new IconButton("pencil-24x24.png", Lang.s("manageRenameLabel"));
		topPanel.titlePanel.add(editButton);
		editButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0) {rename();}});
	}
	
	protected void rename()
	{
		if (isLocked)
		{
			JOptionPane.showMessageDialog(MMTManuscriptEditor.this, Lang.s("imageDelUnlockMessage"));
			return;
		}
		
		String title = JOptionPane.showInputDialog(MMTManuscriptEditor.this, Lang.s("manageInputTitleLabel"), book.getName());
		if (title == null || title.trim().length() == 0 || title.equals(book.getName()))
			return;
		try
		{
			if (findTitle(host.getAppHost().getLink(), title) != null)
				{JOptionPane.showMessageDialog(MMTManuscriptEditor.this, Lang.s("manageRenameExistsMessage")); return;}
			book.setName(title);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		host.getAppHost().broadcastAction(DocumentEvents.bookChanged.event, book);
		host.getAppHost().broadcastAction(DocumentEvents.collectionChanged.event);
	}

	@Override public void onActionRequest(String action, Object param) throws Exception
	{
		if (action.equals("add") && isLocked)
			JOptionPane.showMessageDialog(MMTManuscriptEditor.this, Lang.s("imageDelUnlockMessage"));
		else if (action.equals("delete") && isLocked)
			JOptionPane.showMessageDialog(MMTManuscriptEditor.this, Lang.s("imageDelUnlockMessage"));
		else super.onActionRequest(action, param);
	}
}
