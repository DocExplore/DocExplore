/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
			CreateBookDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run() {host.getActionRequestListener().onAddBookRequest(title, files, false);}
				public float getProgress() {return (float)0;}
			}, host.getFrame());
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("createBookFromImagesLabel")+"</b><br>"+Lang.s("createBookFromImagesMessage")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("scroll-128x128.png")) {public void actionPerformed(ActionEvent arg0)
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
			CreateBookDialog.this.setVisible(false);
			GuiUtils.blockUntilComplete(new ProgressRunnable()
			{
				public void run() {host.getActionRequestListener().onAddBookRequest(title, files, true);}
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
