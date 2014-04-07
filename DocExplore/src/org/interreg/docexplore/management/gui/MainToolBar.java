package org.interreg.docexplore.management.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.image.CropOperation;
import org.interreg.docexplore.management.image.FreeShapeOperation;
import org.interreg.docexplore.management.image.SquareOperation;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;

public class MainToolBar extends JPanel
{
	private static final long serialVersionUID = 656205720944428635L;
	
	MainWindow win;
	public final ToolbarButton prev, next, up, fit;
	JButton browseButton;
	JPanel toolPanel;
	public List<ToolbarToggleButton> roiButtons;
	
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
		
//		this.advancedButton = new JButton(ImageUtils.getIcon("search-view-48x48.png"));
//		advancedButton.setToolTipText(XMLResourceBundle.getBundledString("searchPanelLabel"));
//		add(advancedButton);
		
//		this.presButton = new JButton(IconUtils.getIcon("pres-48x48.png"));
//		presButton.setToolTipText(XMLResourceBundle.getBundledString("authoringLabel"));
//		leftTools.add(presButton);
		
		browseButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) 
		{
			if (win.manageComponent.isShowing())
				win.removeLeftPanel(win.manageComponent);
			else win.addLeftPanel(win.manageComponent, .25);
		}});
//		advancedButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
//		{
//			if (win.searchComponent.isShowing())
//				win.removeLeftPanel(win.searchComponent);
//			else
//			{
//				win.addLeftPanel(win.searchComponent, .25);
//				win.searchComponent.updateKeys();
//			}
//		}});
//		presButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) 
//		{
//			if (win.presComponent.isShowing())
//				win.removeLeftPanel(win.presComponent);
//			else win.addLeftPanel(win.presComponent, .25);
//		}});
		win.addMainWindowListener(new MainWindow.MainWindowListener()
		{
			public void dataLinkChanged(DocExploreDataLink link)
			{
				if (win.manageComponent.isShowing())
					win.removeLeftPanel(win.manageComponent);
				if (win.searchComponent.isShowing())
					win.removeLeftPanel(win.searchComponent);
			}
			public void activeDocumentChanged(AnnotatedObject document) {}
		});
		
		//toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
		toolPanel.add(new JLabel(" "));
		
		this.prev = new ToolbarButton("previous-24x24.png", XMLResourceBundle.getBundledString("imageToolbarPreviousHistoric"))
		{
			public void clicked()
			{
				DocumentPanel panel = win.getActiveTab();
				Page page = panel.document instanceof Page ? (Page)panel.document : 
					((Region)panel.document).getPage();
				try
				{
					panel.win.setActiveTabDocument(page.getBook().getPage(page.getPreviousPageNumber()));
					panel.viewerScrollPane.repaint();
				}
				catch(Exception e2)
				{
					ErrorHandler.defaultHandler.submit(e2);
				}
			}
			public void activeDocumentChanged(AnnotatedObject document)
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
		};
		win.addMainWindowListener(prev);
		toolPanel.add(prev);
		this.up = new ToolbarButton("up-24x24.png", XMLResourceBundle.getBundledString("imageToolbarUp"))
		{
			public void clicked()
			{
				DocumentPanel panel = win.getActiveTab();
				Page page = panel.document instanceof Page ? (Page)panel.document : ((Region)panel.document).getPage();
				try
				{
					panel.win.setActiveTabDocument(page.getBook());
					panel.viewerScrollPane.repaint();
				}
				catch(Exception e2) {ErrorHandler.defaultHandler.submit(e2);}
			}
			public void activeDocumentChanged(AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Page || document instanceof Region)
					setEnabled(true);
				else setEnabled(false);
			}
		};
		win.addMainWindowListener(up);
		toolPanel.add(up);
		this.next = new ToolbarButton("next-24x24.png", XMLResourceBundle.getBundledString("imageToolbarNextHistoric"))
		{
			public void clicked()
			{
				DocumentPanel panel = win.getActiveTab();
				Page page = panel.document instanceof Page ? (Page)panel.document : 
					panel.document instanceof Region ? ((Region)panel.document).getPage() : null;
				try
				{
					if (page != null)
						panel.win.setActiveTabDocument(page.getBook().getPage(page.getNextPageNumber()));
					else panel.win.setActiveTabDocument(((Book)panel.document).getPage(1));
				}
				catch(Exception e2) {ErrorHandler.defaultHandler.submit(e2);}
			}
			public void activeDocumentChanged(AnnotatedObject document)
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
		};
		win.addMainWindowListener(next);
		toolPanel.add(next);
		
		toolPanel.add(new JLabel(" "));
		
		final double zoomFactor = 1.5;
		win.addMainWindowListener((ToolbarButton)toolPanel.add(new ToolbarButton("zoom-in-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarZoomIn"))
		{
			public void clicked()
			{
				DocumentPanel panel = win.getActiveTab();
				Point point = panel.viewerScrollPane.getViewport().getViewPosition();
				panel.pageViewer.applyZoomShift(zoomFactor);
				point.x *= zoomFactor; point.y *= zoomFactor;
				panel.viewerScrollPane.getViewport().setViewPosition(point);
			}
		}));
		win.addMainWindowListener((ToolbarButton)toolPanel.add(new ToolbarButton("zoom-out-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarZoomOut"))
		{
			public void clicked()
			{
				DocumentPanel panel = win.getActiveTab();
				Point point = panel.viewerScrollPane.getViewport().getViewPosition();
				panel.pageViewer.applyZoomShift(1/zoomFactor);
				point.x /= zoomFactor; point.y /= zoomFactor;
				panel.viewerScrollPane.getViewport().setViewPosition(point);
			}
		}));
		
		toolPanel.add(this.fit = new ToolbarButton("fit-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarFit"))
		{
			public void clicked()
			{
				DocumentPanel panel = win.getActiveTab();
				panel.pageViewer.fit();
			}
		});
		win.addMainWindowListener(fit);
		
		toolPanel.add(new JLabel(" "));
		
		roiButtons = new LinkedList<ToolbarToggleButton>();
		
		ToolbarToggleButton addFree = new ToolbarToggleButton("add-free-roi-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarAddFreeRoi"))
		{
			public void toggled()
			{				
				for (ToolbarToggleButton button : roiButtons)
					if (button != this)
						button.setSelected(false);
				win.getActiveTab().pageViewer.setOperation(new FreeShapeOperation());
			}
			public void untoggled()
			{
				win.getActiveTab().pageViewer.setOperation(null);
			}
		};
		roiButtons.add((ToolbarToggleButton)toolPanel.add(addFree));
		win.addMainWindowListener(addFree);
		
		ToolbarToggleButton addRect = new ToolbarToggleButton("add-rect-roi-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarAddRectRoi"))
		{
			public void toggled()
			{				
				for (ToolbarToggleButton button : roiButtons)
					if (button != this)
						button.setSelected(false);
				win.getActiveTab().pageViewer.setOperation(new SquareOperation());
			}
			public void untoggled()
			{
				win.getActiveTab().pageViewer.setOperation(null);
			}
		};
		roiButtons.add((ToolbarToggleButton)toolPanel.add(addRect));
		win.addMainWindowListener(addRect);
		
		win.addMainWindowListener((ToolbarButton)toolPanel.add(new ToolbarButton("del-roi-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarDelRoi"))
		{
			public void clicked()
			{				
				DocumentPanel panel = win.getActiveTab();
				if (panel.document instanceof Region)
					panel.pageViewer.deleteRegion((Region)panel.document);
			}
			public void activeDocumentChanged(AnnotatedObject document)
			{
				setEnabled(document != null && document instanceof Region);
			}
		}));
		
		ToolbarToggleButton resize = new ToolbarToggleButton("resize-page-24x24.png",
			XMLResourceBundle.getBundledString("imageToolbarCrop"))
		{
			public void toggled()
			{				
				for (ToolbarToggleButton button : roiButtons)
					if (button != this)
						button.setSelected(false);
				win.getActiveTab().pageViewer.setOperation(new CropOperation());
			}
			public void untoggled()
			{
				win.getActiveTab().pageViewer.setOperation(null);
			}
		};
		roiButtons.add((ToolbarToggleButton)toolPanel.add(resize));
		win.addMainWindowListener(resize);
		
		toolPanel.add(new JLabel(" "));
		
//		if (win.pluginManager.analysisPlugins.size() > 0)
//		{
//			ToolbarToggleButton analysis = new ToolbarToggleButton("analysis-24x24.png",
//				XMLResourceBundle.getBundledString("imageAnalysis"))
//			{
//				public void toggled()
//				{				
//					for (ToolbarToggleButton button : roiButtons)
//						if (button != this)
//							button.setSelected(false);
//					win.getActiveTab().pageViewer.setOperation(new AnalysisOperation());
//				}
//				public void untoggled()
//				{
//					win.getActiveTab().pageViewer.setOperation(null);
//				}
//			};
//			roiButtons.add((ToolbarToggleButton)toolPanel.add(analysis));
//			win.addMainWindowListener(analysis);
//		}
		
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
	
	public void addRoiButton(ToolbarToggleButton button)
	{
		roiButtons.add((ToolbarToggleButton)toolPanel.add(button));
	}
	public void addButton(ToolbarButton button)
	{
		toolPanel.add(button);
	}
	public void addGap()
	{
		toolPanel.add(new JLabel(" "));
	}
	
	public void unselectRoiButtons()
	{
		for (ToolbarToggleButton button : roiButtons)
			button.setSelected(false);
	}
}
