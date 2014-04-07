package org.interreg.docexplore.util.history;

import java.awt.BorderLayout;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

public class HistoryManager
{
	File cacheDir;
	LinkedList<ReversibleAction> history;
	int limit;
	int cursor;
	
	JDialog progressDialog;
	JProgressBar progressBar;
	JLabel progressLabel;
	boolean actionDone;
	
	public static interface HistoryListener
	{
		public void historyChanged(HistoryManager manager);
	}
	List<HistoryListener> listeners;
	
	public HistoryManager(int limit, File cacheDir)
	{
		this.history = new LinkedList<ReversibleAction>();
		this.limit = limit;
		this.cursor = 0;
		this.listeners = new LinkedList<HistoryListener>();
		this.cacheDir = cacheDir;
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		for (File subDir : cacheDir.listFiles())
			if (subDir.isDirectory())
				deleteCacheDir(subDir);
		
		progressDialog = new JDialog(JOptionPane.getRootFrame(), true);
		progressDialog.setUndecorated(true);
		progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		progressLabel = new JLabel();
		progressDialog.add(progressLabel, BorderLayout.NORTH);
		progressDialog.add(progressBar, BorderLayout.SOUTH);
	}
	
	public void reset() {reset(-1);}
	public void reset(int limit)
	{
		for (ReversibleAction action : history)
			action.dispose();
		history.clear();
		for (File subDir : cacheDir.listFiles())
			if (subDir.isDirectory())
				deleteCacheDir(subDir);
		
		if (limit >= 0)
			this.limit = limit;
		this.cursor = 0;
		this.cacheId = 0;
		
		for (HistoryListener listener : listeners)
			listener.historyChanged(this);
	}
	
	int cacheId = 0;
	File getNewCacheDir()
	{
		File newCacheDir = null;
		do
		{
			newCacheDir = new File(cacheDir, "cache"+cacheId);
			cacheId = (cacheId+1)%(limit+1);
		}
		while (newCacheDir.exists());
		newCacheDir.mkdirs();
		return newCacheDir;
	}
	static void deleteCacheDir(File file)
	{
		if (!file.getName().startsWith("cache"))
			return;
		delete(file);
	}
	private static void delete(File file)
	{
		if (file.isDirectory())
			for (File child : file.listFiles())
				delete(child);
		file.delete();
	}
	
	public void addHistoryListener(HistoryListener listener) {listeners.add(listener);}
	public void removeHistoryListener(HistoryListener listener) {listeners.remove(listener);}
	void notifyListeners()
	{
		for (HistoryListener listener : listeners)
			listener.historyChanged(this);
	}
	
	public boolean canUndo() {return cursor > 0;}
	public boolean canRedo() {return cursor < history.size();}
	
	public ReversibleAction getUndoableAction()
	{
		if (canUndo())
			return history.get(cursor-1);
		return null;
	}
	public ReversibleAction getRedoableAction()
	{
		if (canRedo())
			return history.get(cursor);
		return null;
	}
	
	private void setupProgress(ReversibleAction action)
	{
		progressLabel.setText(action.description());
		progressDialog.pack();
		GuiUtils.centerOnScreen(progressDialog);
		actionDone = false;
	}
	private void followProgress(final ReversibleAction action)
	{
		new Thread() {public void run()
		{
			while (!actionDone)
			{
				progressBar.setValue((int)(100*action.progress()));
				try {Thread.sleep(50);}
				catch (Exception e) {}
			}
		}}.start();
		progressDialog.setVisible(true);
	}
	private void unsetupProgress()
	{
		while (!progressDialog.isVisible())
			try {Thread.sleep(50);}
			catch (Exception e) {}
		SwingUtilities.invokeLater(new Runnable() {public void run() {progressDialog.setVisible(false);}});
		actionDone = true;
	}
	
	public void doAction(final ReversibleAction action) throws Throwable
	{
		action.cacheDir = getNewCacheDir();
		
		setupProgress(action);
		final Throwable [] error = {null};
		new Thread() {public void run() {try {action.doAction();} catch (Throwable e) {error[0] = e;} unsetupProgress();}}.start();
		followProgress(action);
		if (error[0] != null) {deleteCacheDir(action.cacheDir); throw error[0];}
		
		while (history.size() > cursor)
			try
			{
				ReversibleAction old = history.removeLast();
				deleteCacheDir(old.cacheDir);
				old.dispose();
			}
			catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
		history.add(action);
		if (history.size() > limit)
			try
			{
				ReversibleAction old = history.removeFirst();
				deleteCacheDir(old.cacheDir);
				old.dispose();
			}
			catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
		else cursor++;
		
		notifyListeners();
	}
	
	public void undo() throws Exception
	{
		if (!canUndo())
			return;
		history.get(cursor-1).undoAction();
		cursor--;
		
		notifyListeners();
	}
	
	public void redo() throws Exception
	{
		if (!canRedo())
			return;
		history.get(cursor).doAction();
		cursor++;
		
		notifyListeners();
	}
}
