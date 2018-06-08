/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.merge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.gui.MMTApp;
import org.interreg.docexplore.management.manage.ManageComponent;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.app.DocumentActionHandler;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ExportImportComponent extends JPanel
{
	public static interface Listener
	{
		public void bookChanged(Book book);
	}
	
	MMTApp win;
	DocExploreDataLink left, right;
	ManageComponent leftComp, rightComp;
	
	public ExportImportComponent(MMTApp win, DocExploreDataLink left, DocExploreDataLink right) throws DataLinkException
	{
		super(new BorderLayout());
		
		this.left = left;
		this.right = right;
		
		this.leftComp = new ManageComponent(win.host, new DocumentActionHandler(left.getWrappedSource()), false, false);
		this.rightComp = new ManageComponent(win.host, new DocumentActionHandler(right.getWrappedSource()), false, false);
		leftComp.setSingleSelection();
		rightComp.setSingleSelection();
		
		JPanel leftCompPanel = new JPanel(new BorderLayout());
		JPanel rightCompPanel = new JPanel(new BorderLayout());
		leftCompPanel.add(leftComp, BorderLayout.CENTER);
		leftCompPanel.add(new JLabel(left.getWrappedSource().getDescription()), BorderLayout.NORTH);
		rightCompPanel.add(rightComp, BorderLayout.CENTER);
		rightCompPanel.add(new JLabel(right.getWrappedSource().getDescription()), BorderLayout.NORTH);
		add(leftCompPanel, BorderLayout.WEST);
		add(rightCompPanel, BorderLayout.EAST);
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		leftCompPanel.setPreferredSize(new Dimension(screen.width/4, screen.height/2));
		rightCompPanel.setPreferredSize(new Dimension(screen.width/4, screen.height/2));
		
		JPanel buttonPanel = new JPanel(new LooseGridLayout(0, 1));
		JButton importButton = new JButton(Lang.s("importImportFromLabel"), ImageUtils.getIcon("previous-24x24.png"));
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				Book right = rightComp.getSelectedBook();
				if (right == null)
					return;
				getConfirmation(right, leftComp.getSelectedBook());
			}});
		importButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		buttonPanel.add(importButton);
		
		JButton exportButton = new JButton(Lang.s("importExportToLabel"), ImageUtils.getIcon("next-24x24.png"));
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				Book left = leftComp.getSelectedBook();
				if (left == null)
					return;
				getConfirmation(left, rightComp.getSelectedBook());
			}});
		exportButton.setHorizontalTextPosition(SwingConstants.LEFT);
		buttonPanel.add(exportButton);
		
		add(buttonPanel, BorderLayout.CENTER);
	}
	
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	void notifyListeners(Book book)
	{
		for (Listener listener : listeners)
			listener.bookChanged(book);
	}
	
	void getConfirmation(final Book from, final Book to)
	{
		final JDialog confirm = new JDialog(win, true);
		confirm.setLayout(new BorderLayout());
		
		confirm.add(new JLabel(Lang.s("importActionMessage")), BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		final int [] choice = {0};
		buttonPanel.add(new JButton(
			new AbstractAction("<html>"+Lang.s("importAddMessage").replace("%b", from.getName())+"</html>") {
				public void actionPerformed(ActionEvent arg0)
					{choice[0] = 1; confirm.setVisible(false);}}));
		if (to != null)
			buttonPanel.add(new JButton(
				new AbstractAction("<html>"+
					Lang.s("importMergeMessage").replace("%b1", from.getName()).replace("%b2", to.getName())+"</html>") {
						public void actionPerformed(ActionEvent e)
							{choice[0] = 2; confirm.setVisible(false);}}));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("generalCancelLabel")) {
			public void actionPerformed(ActionEvent arg0)
				{confirm.setVisible(false);}}));
		confirm.add(buttonPanel, BorderLayout.SOUTH);
		
		confirm.pack();
		GuiUtils.centerOnScreen(confirm);
		confirm.setVisible(true);
		
		if (choice[0] == 0)
			return;
		
		final BookExporter exporter = new BookExporter();
		
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
		{
			public void run()
			{
				Book dest = to;
				try
				{
					if (choice[0] == 1)
						dest = exporter.add(from, from.getLink().getLink().getSource().equals(left.getWrappedSource()) ? right : left, null);
					else exporter.merge(from, to, null);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				
				notifyListeners(dest);
				
				leftComp.refresh();
				rightComp.refresh();
				
				validate();
				invalidate();
				repaint();
			}
			public float getProgress() {return exporter.progress;}
		}, this);
	}
}
