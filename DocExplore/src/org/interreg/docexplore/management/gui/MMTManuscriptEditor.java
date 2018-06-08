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
