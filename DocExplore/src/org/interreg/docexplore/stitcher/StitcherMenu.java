package org.interreg.docexplore.stitcher;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

import com.sun.glass.events.KeyEvent;

@SuppressWarnings("serial")
public class StitcherMenu extends JMenuBar
{
	Stitcher stitcher;
	JMenu file;
	File curFile = null;
	JMenuItem newItem, loadItem, saveItem, saveAsItem, importItem, quitItem;
	LinkedList<String> recent;
	
	public StitcherMenu(final Stitcher stitcher)
	{
		this.stitcher = stitcher;
		
		this.recent = new LinkedList<String>();
		readRecent();
		this.file = new JMenu(XMLResourceBundle.getBundledString("generalMenuFile"));
		add(file);
		
		newItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuNew")) {public void actionPerformed(ActionEvent arg0) {newFile();}});
		loadItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuLoad")) {public void actionPerformed(ActionEvent arg0) {load();}});
		saveItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuSave")) {public void actionPerformed(ActionEvent arg0) {save();}});
		saveAsItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuSaveAs")) {public void actionPerformed(ActionEvent arg0) {saveAs();}});
		importItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuImport")) {public void actionPerformed(ActionEvent arg0) {importImages();}});
		quitItem = new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuQuit")) {public void actionPerformed(ActionEvent arg0)
			{stitcher.quit();}});
		buildFileMenu();
		
		JMenu view = new JMenu("View");
		view.add(new JMenuItem(new AbstractAction("Fit") {@Override public void actionPerformed(ActionEvent e)
		{
			stitcher.view.fitView(.1);
		}}));
		add(view);
		
		JMenu tools = new JMenu("Tools");
		tools.add(new JMenuItem(new AbstractAction("Detect layout") {@Override public void actionPerformed(ActionEvent e)
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
			{
				float [] progress = {0};
				@Override public void run()
				{
					new LayoutDetector(stitcher.fragmentSet).process(progress);
					stitcher.view.repaint();
				}
				@Override public float getProgress() {return progress[0];}
			}, stitcher.view);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Consolidate") {@Override public void actionPerformed(ActionEvent e)
		{
			GuiUtils.blockUntilComplete(new Runnable()
			{
				@Override public void run()
				{
					if (stitcher.view.selected == null)
						return;
					new LayoutDetector(stitcher.fragmentSet).consolidate(stitcher.view.selected);
					stitcher.view.repaint();
				}
			}, stitcher.view);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));}});
		tools.add(new JMenuItem(new AbstractAction("Render") {@Override public void actionPerformed(ActionEvent e)
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
			{
				float [] progress = {0};
				@Override public void run()
				{
					new Renderer().render(stitcher.fragmentSet, "render", new File("C:\\Users\\aburn\\Desktop\\tmp"), progress);
				}
				@Override public float getProgress() {return progress[0];}
			}, stitcher.view);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));}});
		add(tools);
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
		file.add(importItem);
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
	
	public void importImages()
	{
		File [] images = DocExploreTool.getFileDialogs().openFiles(DocExploreTool.getImagesCategory());
		if (images == null || images.length == 0)
			return;
		stitcher.importFragments(images);
	}
	
	static class RecentItem extends JMenuItem
	{
		public RecentItem(final StitcherMenu menu, final String path)
		{
			super(new AbstractAction(path) {public void actionPerformed(ActionEvent e) {menu.load(new File(path));}});
		}
	}
	
	boolean newFile() {return newFile(false);}
	boolean newFile(boolean noSave)
	{
		if (!noSave && !requestSave())
			return false;
		curFile = null;
		stitcher.clear();
		stitcher.modified = false;
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
			file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getStitchingCategory());
			if (file == null)
				return false;
			curFile = file;
		}
		else curFile = file;
		addRecent(curFile.getAbsolutePath());
		writeRecent();
		
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				ObjectInputStream in = null;
				try
				{
					in = new ObjectInputStream(new FileInputStream(curFile));
					stitcher.read(in);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				if (in != null) try {in.close();} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}, stitcher.win);
		stitcher.modified = false;
		return true;
	}
	boolean saveAs()
	{
		File file = DocExploreTool.getFileDialogs().saveFile(DocExploreTool.getStitchingCategory());
		if (file == null)
			return false;
		curFile = file;
		if (!curFile.getName().toLowerCase().endsWith(".stch"))
			curFile = new File(curFile.getParent(), curFile.getName()+".stch");
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
				ObjectOutputStream out = null;
				try
				{
					out = new ObjectOutputStream(new FileOutputStream(curFile));
					stitcher.write(out);
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				if (out != null) try {out.close();} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public float getProgress() {return (float)progress[0];}
		}, stitcher.win);
		stitcher.modified = false;
		return true;
	}
	boolean requestSave()
	{
		if (curFile != null || !stitcher.modified)
			return true;
		
		int res = JOptionPane.showConfirmDialog(stitcher.win, 
			XMLResourceBundle.getBundledString("generalSaveMessage"), 
			XMLResourceBundle.getBundledString("generalMenuSave"), 
			JOptionPane.YES_NO_CANCEL_OPTION);
		
		if (res == JOptionPane.CANCEL_OPTION)
			return false;
		if (res == JOptionPane.YES_OPTION)
			return save();
		return true;
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
			RandomAccessFile file = new RandomAccessFile(new File(DocExploreTool.getHomeDir(), "StitcherRecent"), "rw");
			file.setLength(0);
			file.write(baos.toByteArray());
			file.close();
			oos.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
	}
	@SuppressWarnings("unchecked")
	void readRecent()
	{
		File file = new File(DocExploreTool.getHomeDir(), "StitcherRecent");
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
}
