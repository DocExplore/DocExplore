/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.SplashScreen;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;
import org.interreg.docexplore.util.ZipUtils;
import org.interreg.docexplore.util.history.HistoryManager;

@SuppressWarnings("serial")
public class AuthoringMenu extends JMenuBar implements HistoryManager.HistoryListener
{
	AuthoringToolFrame tool;
	File curFile = null;
	JCheckBoxMenuItem helpToggle;
	JMenuItem undoItem, redoItem;
	JMenu file;
	JMenuItem newItem, loadItem, saveItem, saveAsItem, exportItem, quitItem;
	LinkedList<String> recent;
	long lastLoad = System.currentTimeMillis();
	
	public AuthoringMenu(final AuthoringToolFrame authoringTool)
	{
		this.tool = authoringTool;
		
		this.recent = new LinkedList<String>();
		readRecent();
		this.file = new JMenu(Lang.s("generalMenuFile"));
		add(file);
		
		newItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuNew")) {public void actionPerformed(ActionEvent arg0) {newFile();}});
		loadItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuLoad")) {public void actionPerformed(ActionEvent arg0) {load();}});
		saveItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuSave")) {public void actionPerformed(ActionEvent arg0) {save();}});
		saveAsItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuSaveAs")) {public void actionPerformed(ActionEvent arg0) {saveAs();}});
//		exportItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuExport")+"...") {public void actionPerformed(ActionEvent arg0)
//		{
//			GuiUtils.blockUntilComplete(new ProgressRunnable()
//			{
//				public void run()
//				{
//					try {authoringTool.readerExporter.doExport(authoringTool.editor.link);}
//					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
//				}
//				public float getProgress() {return (float)authoringTool.readerExporter.progress[0];}
//			}, authoringTool.editor);
//		}});
		exportItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuExport")) {public void actionPerformed(ActionEvent arg0)
		{
			GuiUtils.centerOnComponent(authoringTool.exportDialog, authoringTool);
			authoringTool.exportDialog.setVisible(true);
		}});
//		webExportItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuWebExport")+"...") {public void actionPerformed(ActionEvent arg0)
//		{
//			GuiUtils.blockUntilComplete(new ProgressRunnable()
//			{
//				public void run()
//				{
//					try {authoringTool.webExporter.doExport(authoringTool.editor.link);}
//					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
//				}
//				public float getProgress() {return (authoringTool.webExporter.copyComplete ? .5f : 0f)+(float)(.5*authoringTool.webExporter.progress[0]);}
//			}, authoringTool.editor);
//		}});
		
		quitItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuQuit")) {public void actionPerformed(ActionEvent arg0)
			{authoringTool.quit();}});
		buildFileMenu();
		
		JMenu edit = new JMenu(Lang.s("generalMenuEdit"));
		this.undoItem = new JMenuItem();
		undoItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			try {authoringTool.historyManager.undo();}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		edit.add(undoItem);
		this.redoItem = new JMenuItem();
		redoItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			try {authoringTool.historyManager.redo();}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		edit.add(redoItem);
		JMenuItem viewHistory = new JMenuItem(Lang.s("generalMenuEditViewHistory"));
		viewHistory.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			authoringTool.historyDialog.setVisible(true);
		}});
		edit.add(viewHistory);
		
		edit.addSeparator();
		
		edit.add(new JMenuItem(new AbstractAction(Lang.s("fixChars")) {public void actionPerformed(ActionEvent arg0)
		{
			Object res = JOptionPane.showInputDialog(tool, Lang.s("fixCharsMsg"), Lang.s("fixChars"), 
				JOptionPane.QUESTION_MESSAGE, null, new Object [] {Lang.s("fixCharsWin"), Lang.s("fixCharsMac")}, 
				Lang.s("fixCharsWin"));
			if (res == null)
				return;
			try {convertPresentation(tool.defaultFile, res.equals(Lang.s("fixCharsWin")) ? "ISO-8859-1" : "x-MacRoman");}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			try {tool.editor.reset();}
			catch (Exception e) {e.printStackTrace();}
		}}));
		
		//TODO: remove!
//		edit.add(new JMenuItem(new AbstractAction("hack!")
//		{
//			public void actionPerformed(ActionEvent e) {try
//			{
//				BookEditorView be = null;
//				for (ExplorerView view : tool.editor.views)
//					if (view instanceof BookEditorView)
//						be = (BookEditorView)view;
//				Book book = be.curBook;
//				int lastPage = book.getLastPageNumber();
//				for (int pageNum = 1;pageNum <= lastPage;pageNum++)
//				{
//					Page page = book.getPage(pageNum);
//					Set<Region> regions = page.getRegions();
//					if (regions.size() > 2)
//					{
//						Region highest = null;
//						int max = -1;
//						for (Region region : regions)
//							for (Point point : region.getOutline())
//								if (max < 0 || point.y < max)
//									{max = point.y; highest = region;}
//						Region middle = null;
//						max = -1;
//						for (Region region : regions)
//							if (region != highest)
//								for (Point point : region.getOutline())
//									if (max < 0 || point.y < max)
//										{max = point.y; middle = region;}
//						if (regions.size() > 3)
//						{
//							max = -1;
//							Region newMiddle = null;
//							for (Region region : regions)
//								if (region != highest && region != middle)
//									for (Point point : region.getOutline())
//										if (max < 0 || point.y < max)
//											{max = point.y; newMiddle = region;}
//							middle = newMiddle;
//						}
//						MetaDataKey display = book.getLink().getKey("display", "");
//						for (Map.Entry<MetaDataKey, List<MetaData>> entry : highest.getMetaData().entrySet())
//							for (MetaData md : entry.getValue())
//								if (md.getType().equals(MetaData.textType))
//								{
//									String val = "<b>"+md.getString()+"</b>\n";
//									for (Map.Entry<MetaDataKey, List<MetaData>> entry2 : middle.getMetaData().entrySet())
//										for (MetaData md2 : entry2.getValue())
//											if (md2.getType().equals(MetaData.textType) && TextElement.getStyle(md, tool.styleManager) == TextElement.getStyle(md2, tool.styleManager))
//												val = val+"\n"+md2.getString();
//									md.setString(val);
//								}
//						boolean hasImage = false;
//						for (Map.Entry<MetaDataKey, List<MetaData>> entry2 : middle.getMetaData().entrySet())
//							for (MetaData md2 : entry2.getValue())
//								if (md2.getType().equals(MetaData.imageType))
//								{
//									MetaData imageMd = new MetaData(book.getLink(), display, md2.getType(), md2.getValue());
//									if (!hasImage)
//										BookImporter.insert(imageMd, highest, 0);
//									else BookImporter.insert(imageMd, highest, BookImporter.getHighestRank(highest)+1);
//									hasImage = true;
//								}
//						page.removeRegion(middle);
//					}
//				}
//			}
//			catch (Exception ex) {ex.printStackTrace();}System.out.println("done");}
//		}));
//		edit.add(new JMenuItem(new AbstractAction("hack!")
//		{
//			public void actionPerformed(ActionEvent e) {try
//			{
//				BookEditorView be = null;
//				for (ExplorerView view : tool.editor.views)
//					if (view instanceof BookEditorView)
//						be = (BookEditorView)view;
//				Book book = be.curBook;
//				int lastPage = book.getLastPageNumber();
//				MetaDataKey display = book.getLink().getKey("display", "");
//				for (int pageNum = 1;pageNum <= lastPage;pageNum++)
//				{
//					Page page = book.getPage(pageNum);
//					Set<Region> regions = page.getRegions();
//					for (Region region : regions)
//					{
//						List<MetaData> mds = region.getMetaDataListForKey(display);
//							for (MetaData md : mds)
//								if (md.getType().equals(MetaData.textType))
//								{
//									String val = md.getString().trim();
//									if (!val.startsWith("<i>") || !val.endsWith("</i>"))
//										continue;
//									TextElement.getStyleMD(md).setString("4");
//									md.setString(val.substring(3, val.length()-4));
//									System.out.println(md.getString());
//								}
//					}
//				}
//			}
//			catch (Exception ex) {ex.printStackTrace();}System.out.println("done");}
//		}));
//		edit.add(new JMenuItem(new AbstractAction("hack!")
//		{
//			public void actionPerformed(ActionEvent e) {try
//			{
//				BookEditorView be = null;
//				for (ExplorerView view : tool.editor.views)
//					if (view instanceof BookEditorView)
//						be = (BookEditorView)view;
//				Book book = be.curBook;
//				int lastPage = book.getLastPageNumber();
//				MetaDataKey display = book.getLink().getKey("display", "");
//				for (int pageNum = 1;pageNum <= lastPage;pageNum++)
//				{
//					Page page = book.getPage(pageNum);
//					Set<Region> regions = page.getRegions();
//					for (Region region : regions)
//					{
//						int max = BookImporter.getHighestRank(region);
//						for (int i=0;i<max;i++)
//						{
//							MetaData md1 = BookImporter.getAtRank(region, i);
//							if (md1 == null || !md1.getType().equals(MetaData.textType))
//								continue;
//							MetaData style1 = TextElement.getStyleMD(md1);
//							if (!style1.getString().equals("0"))
//								continue;
//							MetaData md2 = BookImporter.getAtRank(region, i+1);
//							if (md2 == null || !md2.getType().equals(MetaData.textType))
//								continue;
//							MetaData style2 = TextElement.getStyleMD(md2);
//							if (!style2.getString().equals("1"))
//								continue;
//							BookImporter.setRank(md1, i+1);
//							BookImporter.setRank(md2, i);
//							i++;
//						}
//					}
//				}
//			}
//			catch (Exception ex) {ex.printStackTrace();}System.out.println("done");}
//		}));
//		edit.add(new JMenuItem(new AbstractAction("hack!")
//		{
//			public void actionPerformed(ActionEvent e) {try
//			{
//				BookEditorView be = null;
//				for (ExplorerView view : tool.editor.views)
//					if (view instanceof BookEditorView)
//						be = (BookEditorView)view;
//				Book book = be.curBook;
//				MetaDataKey mini = book.getLink().getOrCreateKey("mini", "");
//				MetaDataKey dim = book.getLink().getOrCreateKey("dimension", "");
//				int lastPage = book.getLastPageNumber();
//				for (int pageNum = 1;pageNum <= lastPage;pageNum++)
//				{
//					Page page = book.getPage(pageNum);
//					List<MetaData> mds = page.getMetaDataListForKey(mini);
//					if (mds != null)
//						for (MetaData md : mds)
//							page.removeMetaData(md);
//					mds = page.getMetaDataListForKey(dim);
//					if (mds != null)
//						for (MetaData md : mds)
//							page.removeMetaData(md);
//					DocExploreDataLink.getImageDimension(page, true);
//					DocExploreDataLink.getImageMini(page, false);
//				}
//			}
//			catch (Exception ex) {ex.printStackTrace();}System.out.println("done");}
//		}));
		
		add(edit);
		
		JMenu view = new JMenu(Lang.s("generalMenuSettings"));
		add(view);
		
		JMenuItem styles = new JMenuItem(Lang.s("styleEdit")+"...", ImageUtils.getIcon("pencil-24x24.png"));
		styles.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {authoringTool.styleManager.styleDialog.setVisible(true);}});
		view.add(styles);
		
		helpToggle = new JCheckBoxMenuItem(new AbstractAction(Lang.s("viewHelpToggle")) {public void actionPerformed(ActionEvent arg0)
		{
			authoringTool.displayHelp = helpToggle.isSelected();
			authoringTool.repaint();
		}});
		helpToggle.setSelected(tool.startup.showHelp);
		view.add(helpToggle);
		
		JMenu helpMenu = new JMenu(Lang.s("generalMenuHelp"));
		if (Desktop.isDesktopSupported())
		{
			final Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN))
			{
				helpMenu.add(new AbstractAction(Lang.s("generalMenuHelpContents")) {
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							File doc = new File(DocExploreTool.getExecutableDir(), "MMT documentation.htm");
							desktop.open(doc);
						}
						catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex, true);}
					}});
				helpMenu.add(new AbstractAction(Lang.s("generalMenuHelpWebsite")) {
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
		helpMenu.add(new AbstractAction(Lang.s("generalMenuHelpAbout")) {
			public void actionPerformed(ActionEvent e)
			{
				final JDialog splash = new JDialog(tool, true);
				splash.setLayout(new BorderLayout());
				SplashScreen screen = new SplashScreen("logoAT.png");
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
		
		historyChanged(authoringTool.historyManager);
		authoringTool.historyManager.addHistoryListener(this);
	}
	private void buildFileMenu()
	{
		file.removeAll();
		file.add(newItem);
		file.add(loadItem);
		file.addSeparator();
		file.add(saveItem);
		file.add(saveAsItem);
		file.addSeparator();
		file.add(exportItem);
		//file.add(webExportItem);
		file.addSeparator();
		if (!recent.isEmpty())
		{
			for (String path : recent)
				file.add(new RecentItem(this, path));
			file.addSeparator();
		}
		file.add(quitItem);
	}
	
	boolean newFile() {return newFile(false);}
	boolean newFile(boolean noSave)
	{
		if (!noSave && !requestSave())
			return false;
		curFile = null;
		try {FileUtils.deleteDirectory(tool.defaultFile);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		DataLink link = new DataLinkFS2Source(tool.defaultFile.getAbsolutePath()).getDataLink();
		try
		{
			link.setProperty("autoWrite", false);
			tool.editor.link.setLink(link);
			tool.editor.reset();
			tool.editor.explore("docex://"+link.getAllBookIds().get(0));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		lastLoad = System.currentTimeMillis();
		return true;
	}
	
	boolean load() {return load(null);}
	boolean load(File file) {return load(file, false);}
	boolean load(File file, boolean noSave)
	{
		if (!noSave && !requestSave())
			return false;
		if (file == null)
		{
			file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getPresentationCategory());
			if (file == null)
				return false;
			curFile = file;
		}
		else curFile = file;
		addRecent(curFile.getAbsolutePath());
		writeRecent();
		
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			float [] progress = {0};
			public void run()
			{
				try
				{
					tool.editor.reset();
					//FileUtils.cleanDirectory(authoringTool.defaultFile);
					FileUtils.deleteDirectory(tool.defaultFile);
					tool.defaultFile.mkdirs();
					ZipUtils.unzip(curFile, tool.defaultFile, progress);
					DataLink link = new DataLinkFS2Source(tool.defaultFile.getAbsolutePath()).getDataLink();
					tool.editor.link.setLink(link);
					tool.editor.explore("docex://"+link.getAllBookIds().get(0));
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public float getProgress() {return (float)progress[0];}
		}, tool.editor);
		lastLoad = System.currentTimeMillis();
		return true;
	}
	boolean saveAs()
	{
		File file = DocExploreTool.getFileDialogs().saveFile(DocExploreTool.getPresentationCategory());
		if (file == null)
			return false;
		curFile = file;
		if (!curFile.getName().endsWith(".pres"))
			curFile = new File(curFile.getParent(), curFile.getName()+".pres");
		addRecent(curFile.getAbsolutePath());
		writeRecent();
		return save();
	}
	boolean save()
	{
		if (curFile == null)
			return saveAs();
		GuiUtils.blockUntilComplete(new ProgressRunnable()
		{
			float [] progress = {0};
			public void run()
			{
				try {ZipUtils.zip(tool.defaultFile, curFile, progress, 0);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public float getProgress() {return (float)progress[0];}
		}, tool.editor);
		tool.editor.link.notifyDataLinkChanged();
		lastLoad = System.currentTimeMillis();
		return true;
	}
	boolean requestSave()
	{
		if (curFile != null && lastModified(tool.defaultFile) <= lastLoad)
			return true;
		try {if (tool.editor.link.getBook(tool.editor.link.getLink().getAllBookIds().get(0)).pagesByNumber.isEmpty()) return true;}
		catch (Exception e) {e.printStackTrace(); return true;}
		
		int res = JOptionPane.showConfirmDialog(tool, 
			Lang.s("generalSaveMessage"), 
			Lang.s("generalMenuSave"), 
			JOptionPane.YES_NO_CANCEL_OPTION);
		
		if (res == JOptionPane.CANCEL_OPTION)
			return false;
		if (res == JOptionPane.YES_OPTION)
			return save();
		return true;
	}
	
	long lastModified(File file)
	{
		if (!file.isDirectory())
			return file.lastModified();
		long max = -1;
		for (File child : file.listFiles())
		{
			long mod = lastModified(child);
			if (max < 0 || mod > max)
				max = mod;
		}
		return max;
	}

	public void historyChanged(HistoryManager manager)
	{
		String undoLabel = Lang.s("generalMenuEditUndo");
		String redoLabel = Lang.s("generalMenuEditRedo");
		
		if (manager.canUndo())
			undoLabel += " "+manager.getUndoableAction().description();
		if (manager.canRedo())
			redoLabel += " "+manager.getRedoableAction().description();
		
		undoItem.setText(undoLabel);
		redoItem.setText(redoLabel);
		undoItem.setEnabled(manager.canUndo());
		redoItem.setEnabled(manager.canRedo());
	}
	
	static class RecentItem extends JMenuItem
	{
		public RecentItem(final AuthoringMenu menu, final String path)
		{
			super(new AbstractAction(path) {public void actionPerformed(ActionEvent e) {menu.load(new File(path));}});
		}
	}
	int recentLim = 10;
	void addRecent(String path)
	{
		for (Iterator<String> it=recent.iterator();it.hasNext();)
		{
			String test = it.next();
			if (test.equals(path))
				{it.remove(); break;}
		}
		recent.addFirst(path);
		while (recent.size() > recentLim)
			recent.removeLast();
		writeRecent();
		buildFileMenu();
	}
	void writeRecent()
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream (baos);  
			oos.writeObject(recent);  
			oos.flush();
			RandomAccessFile file = new RandomAccessFile(new File(DocExploreTool.getHomeDir(), "ATRecent"), "rw");
			file.setLength(0);
			file.write(baos.toByteArray());
			file.close();
			oos.close();
			
//			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("ATRecent"), false));
//			out.writeObject(recent);
//			out.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	@SuppressWarnings("unchecked")
	void readRecent()
	{
		File file = new File(DocExploreTool.getHomeDir(), "ATRecent");
		if (!file.exists())
			return;
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			recent = (LinkedList<String>)in.readObject();
			in.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	
	private void convertPresentation(File root, String from) throws Exception
	{
		for (File child : root.listFiles())
			if (child.getName().startsWith("book"))
		{
			File index = new File(child, "index.xml");
			StringUtils.writeFile(index, StringUtils.readFile(index, from), "UTF-8");
		}
		
		File mdDir = new File(root, "metadata");
		for (File child : mdDir.listFiles())
			if (child.getName().startsWith("metadata"))
		{
			File index = new File(child, "index.xml");
			if (index.exists() && StringUtils.readFile(index).contains("<Type>txt</Type>"))
			{
				File value = new File(child, "value");
				StringUtils.writeFile(value, StringUtils.readFile(value, from), "UTF-8");
			}
		}
	}
}
