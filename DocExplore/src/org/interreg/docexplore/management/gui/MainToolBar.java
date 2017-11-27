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
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.gui.image.EditorView;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.image.AnalysisOperation;
import org.interreg.docexplore.management.image.CropOperation;
import org.interreg.docexplore.management.image.FreeShapeROIOperation;
import org.interreg.docexplore.management.image.PageEditor;
import org.interreg.docexplore.management.image.PosterUtils;
import org.interreg.docexplore.management.image.RectROIOperation;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;

public class MainToolBar extends JPanel implements ToolbarButton.ToolbarButtonListener, ToolbarToggleButton.ToolbarToggleButtonListener
{
	private static final long serialVersionUID = 656205720944428635L;
	
	MainWindow win;
	public final ToolbarButton prev, next, up;
	JButton browseButton;
	JPanel toolPanel;
	
	@SuppressWarnings("serial")
	MainToolBar(final MainWindow win)
	{
		//super(new LooseGridLayout(1, 0, 0, 0, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		super(new BorderLayout());
		this.win = win;
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		toolPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
		
		this.browseButton = new JButton(XMLResourceBundle.getBundledString("manageCollectionLabel"), ImageUtils.getIcon("down-11x11.png"));
		toolPanel.add(browseButton);
		
		browseButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) 
		{
			if (win.manageComponent.isShowing())
				win.removeLeftPanel(win.manageComponent);
			else win.addLeftPanel(win.manageComponent, .25);
		}});

		win.addMainWindowListener(new MainWindow.MainWindowListener()
		{
			public void dataLinkChanged(DocExploreDataLink link)
			{
				if (win.manageComponent.isShowing())
					win.removeLeftPanel(win.manageComponent);
				if (win.searchComponent.isShowing())
					win.removeLeftPanel(win.searchComponent);
			}
			public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document) {}
		});
		
		//toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
		toolPanel.add(new JLabel(" "));
		
		addButton(this.prev = new ToolbarButton(this, "prev", "previous-24x24.png", XMLResourceBundle.getBundledString("imageToolbarPreviousHistoric"))
		{
			public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Page || document instanceof Region)
				{
					Page page = document instanceof Page ? (Page)document : document instanceof Region ? ((Region)document).getPage() : null;
					setEnabled(page != null && page.getPageNumber() > 1);
				}
				else setEnabled(false);
			}
		});
		addButton(this.up = new ToolbarButton(this, "up", "up-24x24.png", XMLResourceBundle.getBundledString("imageToolbarUp"))
		{
			public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Page || document instanceof Region)
					setEnabled(true);
				else setEnabled(false);
			}
		});
		addButton(this.next = new ToolbarButton(this, "next", "next-24x24.png", XMLResourceBundle.getBundledString("imageToolbarNextHistoric"))
		{
			public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Page || document instanceof Region)
				{
					Page page = document instanceof Page ? (Page)document : document instanceof Region ? ((Region)document).getPage() : null;
					try {setEnabled(page.getPageNumber() < page.getBook().getLastPageNumber());}
					catch(Exception e2) {ErrorHandler.defaultHandler.submit(e2);}
				}
				else if (document instanceof Book) setEnabled(true);
				else setEnabled(false);
			}
		});
		
		addGap();
		
		addButton(new ToolbarButton(this, "fit", "fit-24x24.png", XMLResourceBundle.getBundledString("imageToolbarFit"))
		{
			@Override public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				setEnabled(document != null && (document instanceof Region || document instanceof Page));
			}
		});
		
		addGap();
		
		addButton(new ToolbarButton(this, "add-pages", "add-file-24x24.png", XMLResourceBundle.getBundledString("manageAppendPagesLabel"))
		{
			@Override public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				setEnabled(document != null && document instanceof Book);
			}
		});
		addButton(new ToolbarButton(this, "remove-pages", "delete-file-24x24.png", XMLResourceBundle.getBundledString("manageDeletePageLabel"))
		{
			@Override public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && ((Book)document).getLastPageNumber() > 0);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		
		addGap();
		
		addPageEditorOperationButton("add-free-roi", "add-free-roi-24x24.png", XMLResourceBundle.getBundledString("imageToolbarAddFreeRoi"), FreeShapeROIOperation.class);
		addPageEditorOperationButton("add-rect-roi", "add-rect-roi-24x24.png", XMLResourceBundle.getBundledString("imageToolbarAddRectRoi"), RectROIOperation.class);
		addButton(new ToolbarButton(this, "remove-roi", "del-roi-24x24.png", XMLResourceBundle.getBundledString("imageToolbarDelRoi"))
		{
			public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				setEnabled(document != null && document instanceof Region);
			}
		});
		
		addPageEditorOperationButton("crop", "resize-page-24x24.png", XMLResourceBundle.getBundledString("imageToolbarCrop"), CropOperation.class);
		
		addGap();
		
		addButton(new ToolbarButton(this, "transpose", "transpose-24x24.png", XMLResourceBundle.getBundledString("imageToolbarTranspose"))
		{
			public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null || !(document instanceof Book))
					setEnabled(false);
				else try {setEnabled(PosterUtils.isPoster((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		
		addGap();
		
		if (win.pluginManager.analysisPlugins.size() > 0)
			addPageEditorOperationButton("analysis", "analysis-24x24.png", XMLResourceBundle.getBundledString("imageAnalysis"), AnalysisOperation.class);
		
		add(toolPanel, BorderLayout.WEST);
		
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JTextField searchArea = new JTextField(20);
		rightPanel.add(searchArea);
		final JButton searchButton = new JButton(new AbstractAction("", ImageUtils.getIcon("search-24x24.png")) {public void actionPerformed(ActionEvent arg0)
		{
			String term = searchArea.getText().trim();
			if (term.length() == 0)
				return;
			win.searchComponent.doSearch(term);
		}}) {{setPreferredSize(new Dimension(36, 36)); setToolTipText(XMLResourceBundle.getBundledString("searchLabel"));}};
		searchArea.addKeyListener(new KeyAdapter() {public void keyReleased(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
				searchButton.doClick();
		}});
		rightPanel.add(searchButton);
		rightPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("advanced-search-24x24.png")) {public void actionPerformed(ActionEvent e)
		{
			JPopupMenu menu = new JPopupMenu();
			menu.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("searcByTag")) {public void actionPerformed(ActionEvent arg0)
			{
				try {win.searchComponent.doSearchByTag();}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}}));
			menu.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("searcByMD")) {public void actionPerformed(ActionEvent arg0)
			{
				try {win.searchComponent.doSearchByAnnotationName();}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}}));
			menu.show((JButton)e.getSource(), 0, 0);
		}}) {{setPreferredSize(new Dimension(36, 36)); setToolTipText(XMLResourceBundle.getBundledString("advancedLabel"));}});
		add(rightPanel, BorderLayout.EAST);
	}
	
	public void addPageEditorOperationButton(String action, String iconName, String tooltip, final Class<? extends EditorView.Operation<?>> clazz)
	{
		addPageEditorOperationButton(action, ImageUtils.getIcon(iconName), tooltip, clazz);
	}
	@SuppressWarnings("serial")
	public void addPageEditorOperationButton(String action, Icon icon, String tooltip, final Class<? extends EditorView.Operation<?>> clazz)
	{
		ToolbarToggleButton button = new ToolbarToggleButton(this, action, icon, tooltip)
		{
			@Override public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (panel != null && panel.getEditor() instanceof PageEditor)
				{
					setEnabled(true);
					setSelected(((PageEditor)panel.getEditor()).getOperation().getClass() == clazz);
				}
				else setEnabled(false);
			}
		};
		addToggleButton(button);
	}
	
	public void addToggleButton(ToolbarToggleButton button) {toolPanel.add(button); win.addMainWindowListener(button);}
	public void addButton(ToolbarButton button) {toolPanel.add(button); win.addMainWindowListener(button);}
	public void addGap() {toolPanel.add(new JLabel(" "));}
	
	@Override public void onToolbarButton(ToolbarButton button)
	{
		if (button.action != null)
			win.broadcastAction(button.action);
	}
	@Override public void onToolbarToggleButton(ToolbarToggleButton button, boolean selected)
	{
		if (button.action != null)
			win.broadcastActionState(button.action, selected);
	}
}
