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
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class CreateBookDialog extends JDialog
{
	public CreateBookDialog(final ManageComponent comp)
	{
		super((Frame)null, XMLResourceBundle.getBundledString("manageAddBookLabel"), true);
		
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
					String title = JOptionPane.showInputDialog(comp.win, XMLResourceBundle.getBundledString("manageInputTitleLabel"));
					if (title == null || title.trim().length() == 0)
						return;
					try {if (comp.findTitle(title) != null)
					{
						JOptionPane.showMessageDialog(comp.win, XMLResourceBundle.getBundledString("manageRenameExistsMessage"));
						return;
					}}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					comp.handler.onAddBookRequest(title, files, false);
				}
				public float getProgress() {return (float)0;}
			}, comp.win);
		}}));
		panel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("createBookFromImagesLabel")+"</b><br>"+XMLResourceBundle.getBundledString("createBookFromImagesMessage")+"</html>"));
		
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
					String title = JOptionPane.showInputDialog(comp.win, XMLResourceBundle.getBundledString("manageInputTitleLabel"));
					if (title == null || title.trim().length() == 0)
						return;
					try {if (comp.findTitle(title) != null)
					{
						JOptionPane.showMessageDialog(comp.win, XMLResourceBundle.getBundledString("manageRenameExistsMessage"));
						return;
					}}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					comp.handler.onAddBookRequest(title, files, true);
				}
				public float getProgress() {return 0;}
			}, comp.win);
		}}));
		panel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("createDocumentFromImagesLabel")+"</b><br>"+XMLResourceBundle.getBundledString("createDocumentFromImagesMessage")+"</html>"));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent arg0)
		{
			CreateBookDialog.this.setVisible(false);
		}}));
		top.add(buttons, BorderLayout.SOUTH);
		
		pack();
	}
}
