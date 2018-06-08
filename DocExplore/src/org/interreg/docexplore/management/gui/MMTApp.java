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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.interreg.docexplore.Startup;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.manage.ManageComponent;
import org.interreg.docexplore.management.plugin.PluginManager;
import org.interreg.docexplore.management.search.SearchComponent;
import org.interreg.docexplore.management.search.SearchHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.AppStatusBar;
import org.interreg.docexplore.manuscript.app.DocumentActionHandler;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.util.GuiUtils;

public class MMTApp extends JFrame
{
	private static final long serialVersionUID = 6037278838028408126L;
	
	// Contains all opened documents
	JTabbedPane tabbedPane;
	
	public SearchComponent searchComponent;
	public ManageComponent manageComponent;
	
	public final MMTAppHost host;
//	public final ProcessDialog processDialog;
//	public ExportDialog exportDialog;
	public final MMTToolBar toolBar;
	AppStatusBar statusBar;
	MainMenuBar menuBar;
	JPanel centerPanel;
	ExecutorService service;
	
	public NotificationStack notifications;
	
	public MMTApp(Startup startup, PluginManager pluginManager) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		super(Lang.s("mmtTitle"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.host = new MMTAppHost(this, startup);
		this.service = Executors.newFixedThreadPool(4);
		
		startup.screen.setText("Initializing analysis plugins");
		pluginManager.initAnalysisPlugins(this);
		
		//UIManager.put("swing.boldMetal", Boolean.FALSE);

		this.tabbedPane = new JTabbedPane();
		tabbedPane.setBackground(Color.white);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().setBackground(Color.white);
		this.centerPanel = new JPanel(new BorderLayout());//new LooseGridLayout(1, 0, 1, 1, false, true, SwingConstants.LEFT, SwingConstants.TOP));
		this.centerPanel.setOpaque(false);
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		this.menuBar = new MainMenuBar(this);
		setJMenuBar(menuBar);
		
		startup.screen.setText("Initializing search");
		this.searchComponent = new SearchComponent(new SearchHandler(this));
		this.manageComponent = new ManageComponent(host, new DocumentActionHandler(host));
		
//		startup.screen.setText("Initializing processing");
//		this.processDialog = new ProcessDialog();
//		try {this.exportDialog = new ExportDialog(null);}
//		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		
		centerPanel.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		
		tabbedPane.addChangeListener(new ChangeListener() {public void stateChanged(ChangeEvent e) {host.notifyActiveDocumentChanged();}});
		
		startup.screen.setText("Initializing toolbar");
		this.toolBar = new MMTToolBar(this);
		this.getContentPane().add(toolBar, BorderLayout.NORTH);
		this.statusBar = new AppStatusBar(host);
		this.getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		this.notifications = new NotificationStack(this);
		getLayeredPane().add(notifications, JLayeredPane.MODAL_LAYER);
		
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				while (tabbedPane.getTabCount() > 0)
					((DocumentPanel)tabbedPane.getComponentAt(tabbedPane.getTabCount()-1)).onCloseRequest();
			}
		});
		
		pack();
	}
	
	public void resetComponents()
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				addLeftPanel(null, 0);
				manageComponent = new ManageComponent(host, new DocumentActionHandler(host));
				searchComponent = new SearchComponent(new SearchHandler(MMTApp.this));
//				try {exportDialog = new ExportDialog(link);}
//				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				host.historyManager.reset(host.getLink().supportsHistory() ? 20 : -1);
				getContentPane().invalidate();
				getContentPane().validate();
				repaint();
			}
		}, tabbedPane);
	}
	
	
	
	public void addLeftPanel(Component panel, double relw)
	{
		Component prev = ((BorderLayout)centerPanel.getLayout()).getLayoutComponent(BorderLayout.WEST);
		if (prev != null && prev != panel)
			centerPanel.remove(prev);
		if (panel != null)
		{
			//panel.setPreferredSize(new Dimension((int)(relw*getWidth()), centerPanel.getHeight()-centerPanel.getInsets().bottom-centerPanel.getInsets().top));
			centerPanel.add(panel, BorderLayout.WEST);
		}
		centerPanel.validate();
		centerPanel.invalidate();
		repaint();
	}
	
	public void removeLeftPanel(Component panel)
	{
		centerPanel.remove(panel);
		centerPanel.validate();
		centerPanel.invalidate();
		repaint();
	}
	
	/**
	 * Remove a tab by searching its id into tabbedPane
	 * @param index
	 * @throws IllegalArgumentException if index = -1
	 */
	public void removeTab(int index) throws IllegalArgumentException
	{
		try
		{
			DocumentPanel panel = (DocumentPanel)tabbedPane.getComponentAt(index);
			panel.hidden();
			tabbedPane.removeTabAt(index);
		}
		catch(IndexOutOfBoundsException e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public DocumentPanel addTab(final AnnotatedObject document, Object param)
	{
		try
		{
			int index = getIndexForDocument(document);
			if (index < 0)
			{
				final DocumentPanel [] panel = {null};
				GuiUtils.blockUntilComplete(new Runnable() {@Override public void run()
				{
					try {panel[0] = new DocumentPanel(host, document, param);}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
				}}, MMTApp.this);
				if (panel[0] == null)
					return null;
				String title = getTabName(document);
				tabbedPane.add("", panel[0]);
				index = tabbedPane.getTabCount()-1;
				tabbedPane.setTabComponentAt(index, new DocumentTab(this, title));
				tabbedPane.setSelectedIndex(index);
				tabbedPane.validate();
				tabbedPane.repaint();
			}
			else tabbedPane.setSelectedIndex(index);
			return (DocumentPanel)tabbedPane.getComponentAt(index);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
		return null;
	}
	
	public void closeBooks(Collection<Book> books)
	{
		for (int i=tabbedPane.getTabCount()-1;i>=0;i--)
		{
			AnnotatedObject object = ((DocumentPanel)tabbedPane.getComponentAt(i)).getDocument();
			Book book = object instanceof Region ? ((Region)object).getPage().getBook() : 
				object instanceof Page ? ((Page)object).getBook() :
				object instanceof Book ? (Book)object : null;
			if (book != null)
				for (Book tbook : books)
					if (tbook.getId() == book.getId())
						{tabbedPane.removeTabAt(i); break;}
		}
	}
	
	public void closePages(Collection<Page> pages)
	{
		for (int i=tabbedPane.getTabCount()-1;i>=0;i--)
		{
			AnnotatedObject object = ((DocumentPanel)tabbedPane.getComponentAt(i)).getDocument();
			Page page = object instanceof Region ? ((Region)object).getPage() : object instanceof Page ? (Page)object : null;
			if (page != null && pages.contains(page))
				tabbedPane.removeTabAt(i);
		}
	}
	
	public void refreshTabNames()
	{
		for (int i=tabbedPane.getTabCount()-1;i>=0;i--)
		{
			AnnotatedObject document = ((DocumentPanel)tabbedPane.getComponentAt(i)).getDocument();
			((DocumentTab)tabbedPane.getTabComponentAt(i)).setTitle(getTabName(document));
		}
	}
	
	public void refreshTabs()
	{
		for (int i=tabbedPane.getTabCount()-1;i>=0;i--)
			try {((DocumentPanel)tabbedPane.getComponentAt(i)).refresh();}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public AnnotatedObject baseDocument(AnnotatedObject document) {return document == null ? null : document instanceof Region ? ((Region)document).getPage() : document;}
	public int getIndexForPanel(DocumentPanel panel)
	{
		for (int i=0;i<tabbedPane.getTabCount();i++)
			if (tabbedPane.getComponentAt(i) == panel)
				return i;
		return -1;
	}
	public int getIndexForDocument(AnnotatedObject document)
	{
		document = baseDocument(document);
		for (int i=0;i<tabbedPane.getTabCount();i++)
		{
			AnnotatedObject open = ((DocumentPanel)tabbedPane.getComponentAt(i)).getDocument();
			if (baseDocument(open) == document)
				return i;
		}
		return -1;
	}
	public DocumentPanel getPanelForIndex(int index) {return (DocumentPanel)tabbedPane.getComponentAt(index);}
	public DocumentPanel getPanelForPage(Page page)
	{
		for (int i=0;i<tabbedPane.getTabCount();i++)
		{
			AnnotatedObject doc = ((DocumentPanel)tabbedPane.getComponentAt(i)).getDocument();
			if (doc == page || (doc instanceof Region && ((Region)doc).getPage() == page))
				return (DocumentPanel)tabbedPane.getComponentAt(i);
		}
		return null;
	}
	
	public String getTabName(AnnotatedObject document)
	{
		String title = "???";
		if (document == null)
			title = Lang.s("generalCollection");
		if (document instanceof Page || document instanceof Region)
		{
			Page page = document instanceof Page ? (Page)document :
				((Region)document).getPage();
			title = page.getBook().getName()+" p"+page.getPageNumber();
			if (document instanceof Region)
				title += " (ROI)";
		}
		else if (document instanceof Book)
			title = ((Book)document).getName();
		else if (document instanceof MetaData)
			title = "MetaData "+document.getId();
		return title;
	}
	
	public DocumentPanel getActiveTab() {return (DocumentPanel)tabbedPane.getSelectedComponent();}
	
	public void close()
	{
		processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		setVisible(false);
	}
}
