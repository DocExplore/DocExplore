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
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.SplashScreen;
import org.interreg.docexplore.datalink.DataLink.DataLinkSource;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.connect.ConnectionBasedMenu;
import org.interreg.docexplore.management.connect.ConnectionHandler;
import org.interreg.docexplore.management.manage.DataLinkCleaner;
import org.interreg.docexplore.management.manage.MetaDataKeyManager;
import org.interreg.docexplore.management.manage.TagManager;
import org.interreg.docexplore.management.merge.ExportImportComponent;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.history.HistoryManager;

@SuppressWarnings("serial")
public class MainMenuBar extends JMenuBar implements HistoryManager.HistoryListener, MainWindow.MainWindowListener
{
	MainWindow win;
	ConnectionHandler connectionHandler;
	ConnectionBasedMenu connectMenu, importMenu;
	JMenuItem disconnect;
	JMenuItem undoItem, redoItem;
	
	public MainMenuBar(final MainWindow win)
	{
		this.win = win;
		
		JMenu fileMenu = new JMenu(XMLResourceBundle.getBundledString("generalMenuFile"));
		this.connectionHandler = new ConnectionHandler();
		this.connectMenu = new ConnectionBasedMenu(connectionHandler, XMLResourceBundle.getBundledString("generalMenuFileConnect")) {
			public void connectionSelected(DataLinkSource source) throws DataLinkException {MainMenuBar.this.win.setLink(source.getDataLink());}};
//		fileMenu.add(connectMenu);
		this.importMenu = new ConnectionBasedMenu(connectionHandler, XMLResourceBundle.getBundledString("generalMenuFileImport")) {
			public void connectionSelected(DataLinkSource source) throws DataLinkException
			{
//				MergeComponent mergeComp = new MergeComponent(MainMenuBar.this.win, source);
//				mergeComp.setVisible(true);
//				if (mergeComp.wasMerged())
//					MainMenuBar.this.win.resetComponents();
				JDialog importDialog = new JDialog((Frame)null, XMLResourceBundle.getBundledString("importTitle"), true);
				DocExploreDataLink right = new DocExploreDataLink();
				right.setLink(source.getDataLink());
				ExportImportComponent ieComp = new ExportImportComponent(win, win.getDocExploreLink(), right);
				ieComp.addListener(new ExportImportComponent.Listener() {public void bookChanged(Book book)
				{
					if (book.getLink().getLink().getSource().equals(win.getDocExploreLink().getLink().getSource()))
					{
						win.closeBooks(Collections.singletonList(book));
						try {win.getDocExploreLink().getBook(book.getId()).reload();}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					}
				}});
				importDialog.add(ieComp);
				importDialog.pack();
				GuiUtils.centerOnScreen(importDialog);
				importDialog.setVisible(true);
				win.resetComponents();
			}};
//		fileMenu.add(importMenu);
		
		this.disconnect = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuFileDisconnect"))
		{
			public void actionPerformed(ActionEvent e)
				{try {MainMenuBar.this.win.setLink(null);} catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}}
		});
//		fileMenu.add(disconnect);
		
//		fileMenu.addSeparator();
		
//		fileMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuFileMemory"))
//		{
//			ResourceMonitor monitor = null;
//			public void actionPerformed(ActionEvent e)
//			{
//				if (monitor == null)
//					monitor = new ResourceMonitor();
//				monitor.setVisible(true);
//			}
//		});
//		
//		fileMenu.addSeparator();
		
		fileMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuFileQuit"))
		{
			public void actionPerformed(ActionEvent e)
			{
				win.close();
			}
		});
		add(fileMenu);

		JMenu editMenu = new JMenu(XMLResourceBundle.getBundledString("generalMenuEdit"));
		this.undoItem = new JMenuItem();
		undoItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			try {MainMenuBar.this.win.historyManager.undo();}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		editMenu.add(undoItem);
		this.redoItem = new JMenuItem();
		redoItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			try {MainMenuBar.this.win.historyManager.redo();}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		editMenu.add(redoItem);
		JMenuItem viewHistory = new JMenuItem(XMLResourceBundle.getBundledString("generalMenuEditViewHistory"));
		viewHistory.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			win.historyDialog.setVisible(true);
		}});
		editMenu.add(viewHistory);
		add(editMenu);
		
		JMenu toolsMenu = new JMenu(XMLResourceBundle.getBundledString("generalMenuTools"));
//		JMenuItem alignItem = new JMenuItem(new AbstractAction("Alignement") {
//			public void actionPerformed(ActionEvent e)
//			{
//				try
//				{
//					JFrame win = new JFrame("Alignement");
//					win.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//					
//					LabeledImageViewer lie = new LabeledImageViewer();
//					EditorPanel panel = new EditorPanel(lie);
//					//panel.setImage(new AnalyzedImage(ImageIO.read(new File("C:\\sci\\align\\roi.PNG"))));
//					TranscriptionPanel tp = new TranscriptionPanel(panel);
//					//tp.setTranscription(StringUtils.readStream2(new FileInputStream("C:\\sci\\align\\trans.txt")));
//					
//					win.setLayout(new BorderLayout());
//					win.add(panel, BorderLayout.CENTER);
//					win.add(tp, BorderLayout.NORTH);
//					
//					win.pack();
//					win.setExtendedState(JFrame.MAXIMIZED_BOTH);
//					win.setVisible(true);
//				}
//				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
//			}});
//		alignItem.setEnabled(false);
//		toolsMenu.add(alignItem);
		
		if (!win.pluginManager.analysisPlugins.isEmpty())
			toolsMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("pluginAnalysisLabel"))
			{
				public void actionPerformed(ActionEvent e)
				{
					win.pluginManager.analysisPluginSetup.setVisible(true);
				}
			});
		toolsMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("cleanLinkLabel"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane.showConfirmDialog(win, 
					XMLResourceBundle.getBundledString("cleanLinkWarning"),
					XMLResourceBundle.getBundledString("cleanLinkLabel"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						GuiUtils.blockUntilComplete(new DataLinkCleaner(win), win);
			}
		});
		toolsMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("keyManagerLabel")+"...")
		{
			public void actionPerformed(ActionEvent e)
			{
				GuiUtils.blockUntilComplete(new ProgressRunnable()
				{
					float [] progress = {0};
					public void run()
					{
						try {MetaDataKeyManager.show(win, progress);}
						catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
					}
					public float getProgress() {return progress[0];}
				}, win);
				win.refreshTabs();
			}
		});
		toolsMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("tagManagerLabel")+"...")
		{
			public void actionPerformed(ActionEvent e)
			{
				GuiUtils.blockUntilComplete(new Runnable() {public void run()
				{
					try {TagManager.show(win);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}}, win);
				win.refreshTabs();
			}
		});
		add(toolsMenu);
		
//		toolsMenu.add(new AbstractAction("Export VT")
//		{
//			public void actionPerformed(ActionEvent e) {try
//			{
//				File out = new File(DocExploreTool.getHomeDir(), "export_vt");
//				out.mkdirs();
//				DocExploreDataLink link = win.getDocExploreLink();
//				MetaDataKey key = link.getKey("Contenu");
//				int imageCnt = 0;
//				Map<Page, String> exportedPages = new TreeMap<Page, String>();
//				Map<String, List<Region>> regionsInClass = new TreeMap<String, List<Region>>();
//				for (int bookId : win.getDocExploreLink().getLink().getAllBookIds())
//				{
//					Book book = win.getDocExploreLink().getBook(bookId);
//					int lastPage = book.getLastPageNumber();
//					for (int i=1;i<=lastPage;i++)
//					{
//						Page page = book.getPage(i);
//						
//						String image = "image"+(imageCnt++)+".png";
//						ImageIO.write(page.getImage().getImage(), "PNG", new File(out, image));
//						page.unloadImage();
//						exportedPages.put(page, image);
//						
//						for (Region region : page.getRegions())
//						{
//							List<MetaData> mds = region.getMetaDataListForKey(key);
//							if (mds != null && mds.size() > 0)
//							{
//								String value = mds.get(0).getString();
//								String res = "";
//								for (int j=0;j<value.length();j++)
//								{
//									char c = value.charAt(j);
//									if (Character.isJavaIdentifierPart(c))
//										res += c;
//								}
//								List<Region> regions = regionsInClass.get(res);
//								if (regions == null)
//									regionsInClass.put(res, regions = new LinkedList<Region>());
//								regions.add(region);
//							}
//						}
//					}
//					System.out.println("Read "+book.getName());
//				}
//				
//				StringBuffer sb = new StringBuffer();
//				
//				System.out.println(regionsInClass.keySet().size()+" classes");
//				for (String content : regionsInClass.keySet())
//				{
//					sb.append(content).append("\n");
//					for (Region region : regionsInClass.get(content))
//					{
//						Page page = region.getPage();
//						String image = exportedPages.get(page);
//						
//						Point [] shape = region.getOutline();
//						int minx = shape[0].x, miny = shape[0].y, maxx = shape[0].x, maxy = shape[0].y;
//						for (int i=1;i<shape.length;i++)
//						{
//							minx = Math.min(minx, shape[i].x);
//							miny = Math.min(miny, shape[i].y);
//							maxx = Math.max(maxx, shape[i].x);
//							maxy = Math.max(maxy, shape[i].y);
//						}
//						sb.append("\t").append(image).append(" ").append(minx).append(" ").append(miny).append(" ").append(maxx).append(" ").append(maxy).append("\n");
//					}
//					System.out.println("Wrote "+(content.length() == 0 ? "Unknwon" : content));
//				}
//				StringUtils.writeFile(new File(out, "gt.txt"), sb.toString());
//			} catch (Exception ex) {ex.printStackTrace();}}
//		});
//		add(toolsMenu);
		
		JMenu helpMenu = new JMenu(XMLResourceBundle.getBundledString("generalMenuHelp"));
		if (Desktop.isDesktopSupported())
		{
			final Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN))
			{
				helpMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuHelpContents")) {
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							File doc = new File(DocExploreTool.getExecutableDir(), "MMT documentation.htm");
							desktop.open(doc);
						}
						catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex, true);}
					}});
				helpMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuHelpWebsite")) {
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							File link = new File(DocExploreTool.getExecutableDir(), "website.url");
							desktop.open(link);
						}
						catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex, true);}
					}});
			}
		}
		helpMenu.add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuHelpAbout")) {
			public void actionPerformed(ActionEvent e)
			{
				final JDialog splash = new JDialog((Frame)null, true);
				splash.setLayout(new BorderLayout());
				SplashScreen screen = new SplashScreen("logoMMT.png");
				splash.add(screen, BorderLayout.NORTH);
				screen.addMouseListener(new MouseAdapter() {@Override public void mouseReleased(MouseEvent e) {splash.setVisible(false);}});
				splash.setUndecorated(true);
				screen.setText("<html>DocExplore 2009-2014"
					+"<br/>Released under the CeCILL v2.1 license"
					+"</html>");
				splash.pack();
				splash.setAlwaysOnTop(true);
				GuiUtils.centerOnScreen(splash);
				splash.setVisible(true);
			}});
		add(helpMenu);
		
		historyChanged(win.historyManager);
		win.historyManager.addHistoryListener(this);
		dataLinkChanged(win.getDocExploreLink());
		win.addMainWindowListener(this);
	}

	public void historyChanged(HistoryManager manager)
	{
		String undoLabel = XMLResourceBundle.getBundledString("generalMenuEditUndo");
		String redoLabel = XMLResourceBundle.getBundledString("generalMenuEditRedo");
		
		if (manager.canUndo())
			undoLabel += " "+manager.getUndoableAction().description();
		if (manager.canRedo())
			redoLabel += " "+manager.getRedoableAction().description();
		
		undoItem.setText(undoLabel);
		redoItem.setText(redoLabel);
		undoItem.setEnabled(manager.canUndo());
		redoItem.setEnabled(manager.canRedo());
	}

	public void activeDocumentChanged(DocumentPanel panel, AnnotatedObject document) {}

	public void dataLinkChanged(DocExploreDataLink link)
	{
		connectMenu.setEnabled(!link.isLinked());
		disconnect.setEnabled(link.isLinked());
		importMenu.setEnabled(link.isLinked());
		
		if (link.isLinked()) 
			try {connectionHandler.addConnection(link.getWrappedSource());}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
