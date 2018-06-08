package org.interreg.docexplore.manuscript.app;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.Clipboard;
import org.interreg.docexplore.management.plugin.PluginManager;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.util.history.HistoryManager;
import org.interreg.docexplore.util.history.HistoryPanel;

public abstract class ManuscriptAppHost
{
	public final HistoryManager historyManager;
	private JDialog historyDialog;
	public final Clipboard clipboard;
	public final PluginManager plugins;
	private DocExploreDataLink link;
	public HelpPanel helpPanel;
	public final JFrame frame;
	
	public ManuscriptAppHost(Startup startup, JFrame frame)
	{
		this.link = new DocExploreDataLink();
		this.historyManager = new HistoryManager(50, new File(DocExploreTool.getHomeDir(), ".mmt-cache"));
		this.historyDialog = new JDialog((Frame)null, Lang.s("generalHistory"));
		historyDialog.add(new HistoryPanel(historyManager));
		historyDialog.pack();
		this.clipboard = new Clipboard();
		this.plugins = startup == null ? null : new PluginManager(startup);
		this.frame = frame;
		this.helpPanel = frame != null ? new HelpPanel(frame) : null;
	}
	
	public Frame getFrame() {return frame;}
	
	public abstract ActionRequestListener getActionRequestListener();
	
	public abstract DocumentPanel getActiveDocument();
	public abstract void setActiveDocument(AnnotatedObject document);
	public abstract DocumentPanel getDocument(AnnotatedObject document);
	public abstract void getDocuments(Collection<DocumentPanel> documents);
	public DocumentPanel addDocument(final AnnotatedObject document) {return addDocument(document, null);}
	public abstract DocumentPanel addDocument(final AnnotatedObject document, Object param);
	public abstract void removeDocument(AnnotatedObject document);
	
	public void switchDocument(AnnotatedObject from, AnnotatedObject to)
	{
		DocumentPanel document = getDocument(from);
		if (document != null)
			document.switchDocument(to);
	}
	
	public abstract DocumentPanelEditor getEditorForDocument(DocumentPanel panel, AnnotatedObject document, Object param);
	public abstract EditorSidePanel buildSidePanelForEditor(DocumentPanelEditor editor);
	
	public abstract void setMessage(String s);
	public abstract void refreshTabNames();
	
	public DocExploreDataLink getLink() {return link;}
	public void setLink(DataLink link) throws DataLinkException
	{
		this.link.setLink(link);
		notifyDataLinkChanged();
	}
	
	public void showHistoryDialog()
	{
		historyDialog.setVisible(true);
	}
	
	public static interface AppListener
	{
		public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document);
		public void dataLinkChanged(DocExploreDataLink link);
	}
	List<AppListener> listeners = new LinkedList<AppListener>();
	public void addAppListener(AppListener listener) {listeners.add(listener);}
	public void removeMainWindowListener(AppListener listener) {listeners.remove(listener);}
	public void notifyActiveDocumentChanged()
	{
		DocumentPanel panel = getActiveDocument();
		AnnotatedObject document = panel != null ? panel.getDocument() : null;
		if (panel != null)
			panel.onDocumentActive();
		for (AppListener listener : listeners)
			listener.onActiveDocumentChanged(panel, document);
	}
	private void notifyDataLinkChanged()
	{
		for (AppListener listener : listeners)
			listener.dataLinkChanged(getLink());
	}
	
	public static interface DocExploreActionListener
	{
		public void onAction(String action, Object param);
	}
	private Map<String, List<DocExploreActionListener>> deActionListeners = new TreeMap<String, List<DocExploreActionListener>>();
	public void addDocExploreActionListener(String action, DocExploreActionListener listener)
	{
		List<DocExploreActionListener> listeners = deActionListeners.get(action);
		if (listeners == null)
			deActionListeners.put(action, listeners = new ArrayList<DocExploreActionListener>());
		listeners.add(listener);
	}
	public void removeDocExploreActionListener(String action, DocExploreActionListener listener)
	{
		List<DocExploreActionListener> listeners = deActionListeners.get(action);
		if (listeners != null)
			listeners.remove(listener);
	}
	
	public void broadcastAction(String action) {broadcastAction(action, null);}
	public void broadcastAction(String action, Object param)
	{
		DocumentEvents.process(this, action, param);
		List<DocExploreActionListener> listeners = deActionListeners.get(action);
		if (listeners != null)
			for (int i=0;i<listeners.size();i++)
				listeners.get(i).onAction(action, param);
	}
}
