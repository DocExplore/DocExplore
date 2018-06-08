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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExplore;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.Startup.PluginConfig;
import org.interreg.docexplore.authoring.explorer.DataLinkExplorer;
import org.interreg.docexplore.authoring.explorer.Explorer;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.edit.ImportOptions;
import org.interreg.docexplore.authoring.explorer.edit.InfoElement;
import org.interreg.docexplore.authoring.explorer.edit.MetaDataEditor;
import org.interreg.docexplore.authoring.explorer.edit.SlideEditorView;
import org.interreg.docexplore.authoring.explorer.edit.StyleDialog;
import org.interreg.docexplore.authoring.explorer.edit.StyleManager;
import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.history.HistoryManager;
import org.interreg.docexplore.util.history.HistoryPanel;

@SuppressWarnings("serial")
public class AuthoringToolFrame extends JFrame
{
	public Startup startup;
	public JSplitPane splitPane;
	JPanel explorer;
	DataLinkExplorer linkExplorer;
	//FileExplorer fileExplorer;
	ReaderExporterOld readerExporter;
	WebExporterOld webExporter;
	public DataLinkExplorer editor;
	boolean regionMode = false;
	public MetaDataEditor mdEditor;
	AuthoringMenu menu;
	File defaultFile = new File(DocExploreTool.getHomeDir(), "Untitled");
	public boolean displayHelp;
	public HistoryManager historyManager;
	public JDialog historyDialog;
	//public FilterPanel filter;
	public ImportOptions importOptions;
	public StyleManager styleManager;
	public MetaDataClipboard clipboard;
	ExportDialogOld exportDialog;
	NameDialogOld nameDialog;
	JPanel rightPanel;
	
	public final List<MetaDataPlugin> plugins;
	public static final String defaultTitle = "Untitled";
	
	boolean recovery = false;
	
	public AuthoringToolFrame(final DocExploreDataLink link, final Startup startup) throws Exception
	{
		super("Authoring Tool");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.startup = startup;
		this.displayHelp = startup.showHelp;
		this.exportDialog = new ExportDialogOld(this);
		this.nameDialog = new NameDialogOld(this);
		
		startup.screen.setText("Initializing history");
		this.historyManager = new HistoryManager(50, new File(DocExploreTool.getHomeDir(), ".at-cache"));
		this.historyDialog = new JDialog(this, Lang.s("generalHistory"));
		historyDialog.add(new HistoryPanel(historyManager));
		historyDialog.pack();
		
		setJMenuBar(this.menu = new AuthoringMenu(this));
		
		startup.screen.setText("Initializing plugins");
		plugins = new Vector<MetaDataPlugin>();
		List<PluginConfig> pluginConfigs = startup.filterPlugins(MetaDataPlugin.class);
		for (PluginConfig config : pluginConfigs)
		{
			MetaDataPlugin plugin = null;
			if ((plugin = (MetaDataPlugin)config.clazz.newInstance()) != null)
			{
				plugins.add(plugin);
				plugin.setHost(config.jarFile, config.dependencies);
				System.out.println("Loaded plugin '"+plugin.getName()+"' ("+plugin.getClass().getSimpleName()+")");
			}
		}
		
		startup.screen.setText("Initializing styles");
		this.styleManager = new StyleManager()
		{
			public void stylesChanged(StyleDialog dialog)
			{
				refreshMenu();
				try {writeMD();}
				catch (Exception e) {e.printStackTrace();}
				if (mdEditor != null)
					try {mdEditor.reload();}
					catch (Exception e) {e.printStackTrace();}
			}
		};
		
		startup.screen.setText("Initializing filters");
		//filter = new FilterPanel(link);
		this.importOptions = new ImportOptions(styleManager);
		
		startup.screen.setText("Creating explorer data link");
		this.linkExplorer = new DataLinkExplorer(this, link, null);
		
		//linkExplorer.toolPanel.add(filter);
		//linkExplorer.setFilter(filter);
		
		startup.screen.setText("Creating explorer");
		//this.fileExplorer = new FileExplorer(this);
		
		this.explorer = new JPanel(new BorderLayout());
		explorer.add(new JLabel("<html><font style=\"font-size:16\">"+Lang.s("generalLibraryLabel")+"</font></html>"), BorderLayout.NORTH);
		explorer.add(linkExplorer, BorderLayout.CENTER);
		//explorer.addTab(XMLResourceBundle.getBundledString("generalFilesLabel"), fileExplorer);
		
		startup.screen.setText("Creating editor data link");
		recovery = defaultFile.exists();
		DataLink fslink = new DataLinkFS2Source(defaultFile.getAbsolutePath()).getDataLink();
		fslink.setProperty("autoWrite", false);
		final DocExploreDataLink editorLink = new DocExploreDataLink();
		final JLabel titleLabel = new JLabel();
		editorLink.addListener(new DocExploreDataLink.Listener() {public void dataLinkChanged(DataLink link)
		{
			String bookName = "";
			try
			{
				List<Integer> books = editorLink.getLink().getAllBookIds();
				if (!books.isEmpty())
				{
					Book book = editorLink.getBook(books.get(0));
					bookName = book.getName();
					MetaDataKey key = editorLink.getOrCreateKey("styles", "");
					List<MetaData> mds = book.getMetaDataListForKey(key);
					MetaData md = null;
					if (mds.isEmpty())
					{
						md = new MetaData(editorLink, key, "");
						book.addMetaData(md);
					}
					else md = mds.get(0);
					styleManager.setMD(md);
				}
			}
			catch (Exception e) {e.printStackTrace();}
			
			String linkTitle = menu.curFile == null ? null : menu.curFile.getAbsolutePath();
			titleLabel.setText("<html><font style=\"font-size:14\">"+Lang.s("generalPresentationLabel")+" : <b>"+bookName+"</b>" +
				(linkTitle != null ? " ("+linkTitle+")" : "")+"</font></html>");
			setTitle(Lang.s("frameTitle")+" "+(linkTitle != null ? linkTitle : ""));
			historyManager.reset(-1);
			repaint();
		}});
		editorLink.setLink(fslink);
		
		startup.screen.setText("Creating editor");
		this.editor = new DataLinkExplorer(this, editorLink, new BookImporter());
		for (ExplorerView view : editor.views)
			if (view instanceof SlideEditorView)
				{this.mdEditor = new MetaDataEditor(((SlideEditorView)view).editor);}
		editor.addListener(new Explorer.Listener()
		{
			@Override public void exploringChanged(Object object)
			{
				try
				{
					boolean isRegion = object instanceof Region;
					mdEditor.setDocument(null);
					if (isRegion)
						mdEditor.setDocument((Region)object);
					if (isRegion != regionMode)
					{
						rightPanel.removeAll();
						Component right = isRegion ? mdEditor : explorer;
						rightPanel.add(right, BorderLayout.CENTER);
						right.revalidate();
						right.repaint();
					}
					regionMode = isRegion;
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		});
		final JButton editBook = new JButton(new AbstractAction("", ImageUtils.getIcon("pencil-24x24.png"))
		{
			@Override public void actionPerformed(ActionEvent e)
			{
				try
				{
//					Book book = editorLink.getBook(editorLink.getLink().getAllBookIds().get(0));
//					String name = JOptionPane.showInputDialog(AuthoringToolFrame.this, XMLResourceBundle.getBundledString("collectionAddBookMessage"), book.getName());
//					if (name == null)
//						return;
//					book.setName(name);
					GuiUtils.centerOnComponent(nameDialog, AuthoringToolFrame.this);
					Book book = editorLink.getBook(editorLink.getLink().getAllBookIds().get(0));
					if (nameDialog.show(editorLink, book))
					{
						String bookName = "";
						bookName = book.getName();
						String linkTitle = menu.curFile == null ? null : menu.curFile.getAbsolutePath();
						titleLabel.setText("<html><font style=\"font-size:14\">"+Lang.s("generalPresentationLabel")+" : <b>"+bookName+"</b>" +
							(linkTitle != null ? " ("+linkTitle+")" : "")+"</font></html>");
						setTitle(Lang.s("frameTitle")+" "+(linkTitle != null ? linkTitle : ""));
						editor.refreshPath();
					}
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		});
		editBook.setToolTipText(Lang.s("generalToolbarEdit"));
		
		this.splitPane = new JSplitPane();
		splitPane.setContinuousLayout(false);
		JPanel editorPanel = new JPanel(new BorderLayout(0, 0));
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		titlePanel.add(titleLabel);
		titlePanel.add(editBook);
		//editorPanel.add(titlePanel, BorderLayout.NORTH);
		
//		editorPanel.add(editor, BorderLayout.CENTER);
//		splitPane.setLeftComponent(editorPanel);
		splitPane.setLeftComponent(editor);
		
		rightPanel = new JPanel(new BorderLayout(0, 0));
		splitPane.setRightComponent(rightPanel);
		rightPanel.add(explorer, BorderLayout.CENTER);
		
		getContentPane().setLayout(new BorderLayout());
		add(titlePanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		
		this.clipboard = new MetaDataClipboard(this, new File(DocExploreTool.getHomeDir(), ".at-clipboard"));
		
		startup.screen.setText("Initializing exporter");
		this.readerExporter = new ReaderExporterOld(this);
		this.webExporter = new WebExporterOld(this);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e) {quit();}
			public void windowOpened(WindowEvent e) {splitPane.setDividerLocation(getWidth()/2);}
		});
		this.oldSize = getWidth();
		addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				if (oldSize > 0)
					splitPane.setDividerLocation(splitPane.getDividerLocation()*getWidth()/oldSize);
				oldSize = getWidth();
			}
		});
	}
	int oldSize = 0;
	
	public boolean metaDataTypeIsPluggedIn(String type)
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.getType().equals(type))
				return true;
		return false;
	}
	public MetaDataPlugin getPluginForType(String type)
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.getType().equals(type))
				return plugin;
		return null;
	}
	public InfoElement createInfoElement(MetaDataEditor editor, MetaData md, int width) throws DataLinkException
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.getType().equals(md.getType()))
				return plugin.createInfoElement(editor, md, width);
		return null;
	}
	public boolean canPreview(Object object)
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.canPreview(object))
				return true;
		return false;
	}
	public String getFileType(File file)
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.canPreview(file))
				return plugin.getFileType();
		return null;
	}
	public Icon getIcon(Object object)
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.canPreview(object))
				return plugin.createIcon(object);
		return null;
	}
	public Icon getMetaDataIcon(MetaData md)
	{
		for (MetaDataPlugin plugin : plugins)
			if (md.getType().equals(plugin.getType()))
				return plugin.createIcon(md);
		return null;
	}
	public void exportMetaData(MetaData md, StringBuffer xml, File bookDir, int id, ExportOptions options, int exportType)
	{
		for (MetaDataPlugin plugin : plugins)
			if (md.getType().equals(plugin.getType()))
				{plugin.exportMetaData(md, xml, bookDir, id, options, exportType); break;}
	}
	public PreviewPanel createPreview(Object object, int mx, int my)
	{
		for (MetaDataPlugin plugin : plugins)
			if (plugin.canPreview(object))
				return plugin.getPreview(object, mx, my);
		return null;
	}
	
	void quit()
	{
		if (!menu.requestSave())
			return;
		if (mdEditor != null)
			try {mdEditor.setDocument(null);}
			catch (Exception e) {e.printStackTrace();}
		try {FileUtils.deleteDirectory(defaultFile);}
		catch (Exception e) {e.printStackTrace();}
		startup.shutdown();
		setVisible(false);
		
		DocExplore.main(new String [0]);
	}
}
