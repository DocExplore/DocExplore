/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class StitcherMenu extends JMenuBar
{
	Stitcher stitcher;
	JMenu file;
	File curFile = null;
	JMenuItem newItem, loadItem, saveItem, saveAsItem, importItem, quitItem;
	JMenuItem saveWipItem, deleteWipItem, close;
	LinkedList<String> recent;
	
	public StitcherMenu(final Stitcher stitcher)
	{
		this.stitcher = stitcher;
		
		this.recent = new LinkedList<String>();
		readRecent();
		this.file = new JMenu(Lang.s("generalMenuFile"));
		add(file);
		
		newItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuNew")) {public void actionPerformed(ActionEvent arg0) {newFile();}});
		loadItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuLoad")) {public void actionPerformed(ActionEvent arg0) {load();}});
		saveItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuSave")) {public void actionPerformed(ActionEvent arg0) {save();}});
		saveAsItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuSaveAs")) {public void actionPerformed(ActionEvent arg0) {saveAs();}});
		importItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuImport")) {public void actionPerformed(ActionEvent arg0) {importImages();}});
		quitItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuQuit")) {public void actionPerformed(ActionEvent arg0)
			{stitcher.quit();}});
		saveWipItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuSave")) {public void actionPerformed(ActionEvent arg0) {saveWip();}});
		deleteWipItem = new JMenuItem(new AbstractAction(Lang.s("generalMenuDelete")) {public void actionPerformed(ActionEvent arg0) {deleteWip();}});
		buildFileMenu();
		
		JMenu view = new JMenu("View");
		view.add(new JMenuItem(new AbstractAction("Fit") {@Override public void actionPerformed(ActionEvent e)
		{
			stitcher.view.fitView(.1);
		}}));
		view.add(new JCheckBoxMenuItem("Show detected groups", stitcher.showDetectedGroups) 
			{{addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {
				stitcher.showDetectedGroups = isSelected();
			}
		});}});
		add(view);
		
		JMenu tools = new JMenu("Tools");
		List<JCheckBoxMenuItem> detectors = new ArrayList<>();
		for (FeatureDetector detector : FeatureDetector.values())
			detectors.add(new JCheckBoxMenuItem(detector.name()) {{addChangeListener(new ChangeListener() {@Override public void stateChanged(ChangeEvent e)
			{
				if (isSelected())
				{
					for (JCheckBoxMenuItem item : detectors)
						if (item != e.getSource())
						{
							item.setSelected(false);
							item.setEnabled(true);
						}
					setEnabled(false);
					stitcher.detector = detector;
				}
			}});}});
		for (JCheckBoxMenuItem detector : detectors)
			tools.add(detector);
		tools.addSeparator();
		detectors.get(0).setSelected(true);
		tools.add(new JMenuItem(new AbstractAction("Detect layout") {@Override public void actionPerformed(ActionEvent e)
		{
			StitcherToolkit.detectLayout(stitcher.view);
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
			stitcher.renderEditor.init();
			stitcher.renderEditorWin.setVisible(true);
		}}) {{setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));}});
		add(tools);
	}
	
	private void buildFileMenu()
	{
		file.removeAll();
		if (stitcher.host == null)
		{
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
		else
		{
			file.add(saveWipItem);
			file.add(deleteWipItem);
		}
	}
	
	public void importImages()
	{
		File [] images = DocExploreTool.getFileDialogs().openFiles(DocExploreTool.getImagesCategory());
		if (images == null || images.length == 0)
			return;
		stitcher.toolkit.importFiles(images);
	}
	
	static class RecentItem extends JMenuItem
	{
		public RecentItem(final StitcherMenu menu, final String path)
		{
			super(new AbstractAction(path) {public void actionPerformed(ActionEvent e) {menu.load(new File(path));}});
		}
	}
	
	void saveWip()
	{
		GuiUtils.blockUntilComplete(new Runnable()
		{
			public void run()
			{
				ObjectOutputStream out = null;
				try
				{
					MetaData wip = null;
					List<MetaData> mds = stitcher.poster.getMetaDataListForKey(stitcher.host.getLink().stitchKey);
					if (!mds.isEmpty())
						wip = mds.get(0);
					else stitcher.poster.addMetaData(wip = new MetaData(stitcher.host.getLink(), stitcher.host.getLink().stitchKey, ""));
					ByteArrayOutputStream array = new ByteArrayOutputStream();
					out = new ObjectOutputStream(array);
					stitcher.write(out);
					ByteArrayInputStream in = new ByteArrayInputStream(array.toByteArray());
					wip.setValue(MetaData.textType, in);
					in.close();
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				if (out != null) try {out.close();} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}, stitcher.win);
	}
	void deleteWip()
	{
		try
		{
			List<MetaData> mds = stitcher.poster.getMetaDataListForKey(stitcher.host.getLink().stitchKey);
			if (!mds.isEmpty())
				stitcher.poster.removeMetaData(mds.get(0));
			stitcher.quit(false);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	boolean newFile() {return newFile(false);}
	boolean newFile(boolean noSave)
	{
		if (!noSave && !requestSave())
			return false;
		curFile = null;
		stitcher.clear();
		stitcher.view.modified = false;
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
		stitcher.view.modified = false;
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
		stitcher.view.modified = false;
		return true;
	}
	boolean requestSave()
	{
		if (curFile != null || !stitcher.view.modified)
			return true;
		
		int res = JOptionPane.showConfirmDialog(stitcher.win, 
			Lang.s("generalSaveMessage"), 
			Lang.s("generalMenuSave"), 
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
