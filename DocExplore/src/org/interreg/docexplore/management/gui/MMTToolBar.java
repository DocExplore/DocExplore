/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.app.AppToolBar;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.ToolbarButton;
import org.interreg.docexplore.manuscript.app.editors.AnalysisOperation;
import org.interreg.docexplore.util.ImageUtils;

public class MMTToolBar extends AppToolBar
{
	private static final long serialVersionUID = 656205720944428635L;
	
	MMTApp win;
	
	MMTToolBar(final MMTApp win)
	{
		//super(new LooseGridLayout(1, 0, 0, 0, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		super(win.host);
		
		this.win = win;
		//setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		win.host.addAppListener(new ManuscriptAppHost.AppListener()
		{
			public void dataLinkChanged(DocExploreDataLink link)
			{
				if (win.manageComponent.isShowing())
					win.removeLeftPanel(win.manageComponent);
				if (win.searchComponent.isShowing())
					win.removeLeftPanel(win.searchComponent);
			}
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document) {}
		});
		
		buildToolBar();
		addAccelerators(win.getRootPane());
	}
	
	@SuppressWarnings("serial")
	@Override protected void addNavigationButtons()
	{
		JButton browseButton = new JButton(Lang.s("manageCollectionLabel"));
		toolPanel.add(browseButton);
		browseButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) 
		{
			win.host.addDocument(null);
		}});
		addGap();
		addButton(new ToolbarButton(this, "import", "import-24x24.png", Lang.s("manageImportBookLabel"))
		{
			@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				setEnabled(document == null);
			}
		});
		addButton(new ToolbarButton(this, "export", "export-24x24.png", Lang.s("manageExportBookLabel"))
		{
			@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				setEnabled(document == null);
			}
		});
		addGap();
		
		super.addNavigationButtons();
	}
	@SuppressWarnings("serial")
	@Override protected void addEditImageButtons()
	{
		super.addEditImageButtons();
		addGap();
		if (win.host.plugins.analysisPlugins.size() > 0)
			addPageEditorOperationButton("analysis", "analysis-24x24.png", Lang.s("imageAnalysis"), AnalysisOperation.class);
		
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightPanel.setOpaque(false);
		final JTextField searchArea = new JTextField(20);
		rightPanel.add(searchArea);
		final JButton searchButton = new JButton(new AbstractAction("", ImageUtils.getIcon("search-24x24.png")) {public void actionPerformed(ActionEvent arg0)
		{
			String term = searchArea.getText().trim();
			if (term.length() == 0)
				return;
			win.searchComponent.doSearch(term);
		}}) {{setPreferredSize(new Dimension(36, 36)); setToolTipText(Lang.s("searchLabel"));}};
		searchArea.addKeyListener(new KeyAdapter() {public void keyReleased(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
				searchButton.doClick();
		}});
		rightPanel.add(searchButton);
		rightPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("advanced-search-24x24.png")) {public void actionPerformed(ActionEvent e)
		{
			JPopupMenu menu = new JPopupMenu();
			menu.add(new JMenuItem(new AbstractAction(Lang.s("searcByTag")) {public void actionPerformed(ActionEvent arg0)
			{
				try {win.searchComponent.doSearchByTag();}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}}));
			menu.add(new JMenuItem(new AbstractAction(Lang.s("searcByMD")) {public void actionPerformed(ActionEvent arg0)
			{
				try {win.searchComponent.doSearchByAnnotationName();}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}}));
			menu.show((JButton)e.getSource(), 0, 0);
		}}) {{setPreferredSize(new Dimension(36, 36)); setToolTipText(Lang.s("advancedLabel"));}});
		add(rightPanel, BorderLayout.EAST);
	}
}
