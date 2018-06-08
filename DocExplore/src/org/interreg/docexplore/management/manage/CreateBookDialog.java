package org.interreg.docexplore.management.manage;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class CreateBookDialog extends JDialog
{
	public CreateBookDialog(final ManuscriptAppHost host)
	{
		super((Frame)null, Lang.s("manageAddBookLabel"), true);
		
		JPanel top = new JPanel(new BorderLayout());
		setContentPane(top);
		
		JPanel panel = new JPanel(new LooseGridLayout(0, 2, 10, 10, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		top.add(panel, BorderLayout.CENTER);
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("book-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			CreateBookDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					List<File> files = SelectPagesPanel.show();
					if (files == null)
						return;
					String title = JOptionPane.showInputDialog(Lang.s("manageInputTitleLabel"), Lang.s("manageNewDocumentLabel"));
					if (title == null || title.trim().length() == 0)
						return;
					try {if (ManuscriptEditor.findTitle(host.getLink(), title) != null)
					{
						JOptionPane.showMessageDialog(host.getFrame(), Lang.s("manageRenameExistsMessage"));
						return;
					}}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					host.getActionRequestListener().onAddBookRequest(title, files, false);
				}
				public float getProgress() {return (float)0;}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("createBookFromImagesLabel")+"</b><br>"+Lang.s("createBookFromImagesMessage")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("scroll-128x128.png")) {public void actionPerformed(ActionEvent arg0)
		{
			CreateBookDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run()
				{
					List<File> files = SelectPagesPanel.show();
					if (files == null)
						return;
					String title = JOptionPane.showInputDialog(Lang.s("manageInputTitleLabel"), Lang.s("manageNewDocumentLabel"));
					if (title == null || title.trim().length() == 0)
						return;
					try {if (ManuscriptEditor.findTitle(host.getLink(), title) != null)
					{
						JOptionPane.showMessageDialog(host.getFrame(), Lang.s("manageRenameExistsMessage"));
						return;
					}}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					host.getActionRequestListener().onAddBookRequest(title, files, true);
				}
				public float getProgress() {return 0;}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("createDocumentFromImagesLabel")+"</b><br>"+Lang.s("createDocumentFromImagesMessage")+"</html>"));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(Lang.s("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent arg0)
		{
			CreateBookDialog.this.setVisible(false);
		}}));
		top.add(buttons, BorderLayout.SOUTH);
		
		pack();
	}
}
