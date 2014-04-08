/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.plugin.analysis.AnalysisPlugin;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.reader.plugin.ClientPlugin;
import org.interreg.docexplore.reader.plugin.InputPlugin;
import org.interreg.docexplore.reader.plugin.ServerPlugin;
import org.interreg.docexplore.util.Pair;
import org.interreg.docexplore.util.StringUtils;

@SuppressWarnings("serial")
public class GeneralConfigPanel extends JPanel
{
	static class PluginPanel extends JPanel
	{
		GeneralConfigPanel config;
		JCheckBox usePlugin;
		//JTextField jarFile;//, mainClass;
		String jarName;
		
		public PluginPanel(final GeneralConfigPanel config, String pluginXml)
			{this(config, 
				StringUtils.getBoolean(StringUtils.getTagContent(pluginXml, "use")), 
				StringUtils.getTagContent(pluginXml, "jar"));}
		
		public PluginPanel(final GeneralConfigPanel config, boolean use, String jarName)
		{
			super(new LooseGridLayout(0, 3, 5, 5, false, true, SwingConstants.LEFT, SwingConstants.TOP, true, false));
			
			this.config = config;
			this.jarName = jarName;
			setBorder(BorderFactory.createEtchedBorder());
			boolean exists = new File(DocExploreTool.getPluginDir(), jarName).exists();
			usePlugin = new JCheckBox(jarName+(exists ? "" : " ("+XMLResourceBundle.getBundledString("cfgMissingFileLabel")+")"));
			usePlugin.setEnabled(exists);
			add(usePlugin);
//			add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgRemoveLabel")) {public void actionPerformed(ActionEvent e)
//			{
//				config.pluginsPanel.remove(PluginPanel.this);
//				config.pluginPanels.remove(PluginPanel.this);
//				((Window)config.getTopLevelAncestor()).pack();
//			}}));
//			add(new JLabel());
//			
//			add(new JLabel(XMLResourceBundle.getBundledString("cfgJarFileLabel"))); add(new JLabel(jarName));
//			add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgBrowseLabel")) {public void actionPerformed(ActionEvent e)
//			{
//				File newJar = config.browsePlugins();
//				if (newJar != null)
//					jarFile.setText(newJar.getName());
//			}}));
			
//			mainClass = new JTextField(40);
//			add(new JLabel(XMLResourceBundle.getBundledString("cfgMainClassLabel"))); add(mainClass);
//			add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgBrowseLabel")) {public void actionPerformed(ActionEvent e)
//			{
//				String clazz = config.browseClasses(new File(DocExploreTool.root, jarFile.getText()));
//				if (clazz != null)
//					mainClass.setText(clazz);
//			}}));
			
//			if (jarName != null) jarFile.setText(jarName);
//			if (className != null) mainClass.setText(className);
			usePlugin.setSelected(use);
		}
	}
	
	File configFile;
	
	JCheckBox safeMode;
	
//	JCheckBox useAutoConnect;
//	JComboBox autoConnectType;
//	JPanel autoConnectSubPanel, fsSubPanel, dbSubPanel;
//	JTextField acFSPath;
//	JTextField acDBPath, acDBHost, acDBDatabase, acDBUser, acDBPassword;
	
	JTextField displayWidth, displayHeight;
	JCheckBox displayHelp, displayFullscreen, displayNativeCursor;
	JComboBox displayLang;
	
	JPanel pluginsPanel;
	List<PluginPanel> pluginPanels = new LinkedList<PluginPanel>();
	
	public GeneralConfigPanel(final File config) throws Exception
	{
		this.configFile = config;
		
		final String xml = config.exists() ? StringUtils.readFile(config) : "<config></config>";
		setLayout(new LooseGridLayout(0, 1, 5, 5, true, true, SwingConstants.LEFT, SwingConstants.TOP));
		
//		JPanel ac = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, true, SwingConstants.LEFT, SwingConstants.TOP));
//		ac.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("cfgACLabel")));
//		ac.add(useAutoConnect = new JCheckBox(XMLResourceBundle.getBundledString("cfgUseACLabel")));
//		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		typePanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgTypeLabel")));
//		typePanel.add(autoConnectType = new JComboBox(new Object [] {XMLResourceBundle.getBundledString("cfgFileLabel"), XMLResourceBundle.getBundledString("cfgMysqlLabel")}));
//		ac.add(typePanel);
//		ac.add(autoConnectSubPanel = new JPanel(new BorderLayout()));
//		
//		fsSubPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, true, SwingConstants.LEFT, SwingConstants.TOP));
//		fsSubPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgPathLabel"))); fsSubPanel.add(acFSPath = new JTextField(40));
//		
//		dbSubPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, true, SwingConstants.LEFT, SwingConstants.TOP));
//		dbSubPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgPathLabel"))); dbSubPanel.add(acDBPath = new JTextField(40));
//		dbSubPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgHostLabel"))); dbSubPanel.add(acDBHost = new JTextField(40));
//		dbSubPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgDBLabel"))); dbSubPanel.add(acDBDatabase = new JTextField(40));
//		dbSubPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgUserLabel"))); dbSubPanel.add(acDBUser = new JTextField(40));
//		dbSubPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgPasswordLabel"))); dbSubPanel.add(acDBPassword = new JTextField(40));
//		
//		Set<ConnectionHandler.PastConnection> cons = new ConnectionHandler().connections;
//		if (cons != null)
//		{
//			Vector<Object> recentCons = new Vector<Object>();
//			for (ConnectionHandler.PastConnection con : cons)
//				if (con.source instanceof DataLinkMySQLSource || con.source instanceof DataLinkFS2Source)
//					recentCons.add(con);
//			
//			if (!recentCons.isEmpty())
//			{
//				recentCons.insertElementAt(XMLResourceBundle.getBundledString("cfgCurrentLabel"), 0);
//				JPanel recentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//				recentPanel.add(new JLabel(XMLResourceBundle.getBundledString("cfgRecentLabel")));
//				JComboBox recentBox = new JComboBox(recentCons);
//				recentPanel.add(recentBox);
//				ac.add(recentPanel);
//				
//				recentBox.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e)
//				{
//					if (e.getStateChange() != ItemEvent.SELECTED)
//						return;
//					if (e.getItem().equals(XMLResourceBundle.getBundledString("cfgCurrentLabel")))
//						fillAutoConnect(xml);
//					else fillAutoConnect((ConnectionHandler.PastConnection)e.getItem());
//				}});
//			}
//		}
		
		JPanel display = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, true, SwingConstants.LEFT, SwingConstants.TOP));
		display.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("cfgDisplayLabel")));
		JPanel displayDims = new JPanel(new LooseGridLayout(0, 2, 5, 5, false, true, SwingConstants.LEFT, SwingConstants.TOP));
		displayDims.add(new JLabel(XMLResourceBundle.getBundledString("cfgWidthLabel"))); displayDims.add(displayWidth = new JTextField(5));
		displayDims.add(new JLabel(XMLResourceBundle.getBundledString("cfgHeightLabel"))); displayDims.add(displayHeight = new JTextField(5));
		displayDims.add(new JLabel(XMLResourceBundle.getBundledString("cfgLanguageLabel"))); displayDims.add(displayLang = new JComboBox(new Object [] {
			XMLResourceBundle.getBundledString("cfgEnglishLabel"), XMLResourceBundle.getBundledString("cfgFrenchLabel")}));
		display.add(displayHelp = new JCheckBox(XMLResourceBundle.getBundledString("cfgHelpLabel")));
		display.add(displayFullscreen = new JCheckBox(XMLResourceBundle.getBundledString("cfgFullscreenLabel")));
		display.add(displayNativeCursor = new JCheckBox(XMLResourceBundle.getBundledString("cfgUseCursorLabel")));
		display.add(displayDims);
		
		JPanel topPluginsPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, true, SwingConstants.LEFT, SwingConstants.TOP));
		topPluginsPanel.setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("cfgPluginsLabel")));
		pluginsPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		JScrollPane scrollPane = new JScrollPane(pluginsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		topPluginsPanel.add(scrollPane);
		
		JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgAddPluginLabel")) {public void actionPerformed(ActionEvent e)
		{
			try
			{
				JOptionPane.showMessageDialog(GeneralConfigPanel.this, XMLResourceBundle.getBundledString("cfgAddPluginMessage").replace("%pdir", DocExploreTool.getPluginDir().getAbsolutePath()));
				if (System.getProperty("os.name").toLowerCase().contains("win"))
					Runtime.getRuntime().exec(new String [] {"explorer", DocExploreTool.getPluginDir().getAbsolutePath()});
				else if (System.getProperty("os.name").toLowerCase().contains("mac"))
					Runtime.getRuntime().exec(new String [] {"open", DocExploreTool.getPluginDir().getAbsolutePath()});
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
//			File jarFile = browsePlugins();
//			if (jarFile == null)
//				return;
//			PluginPanel pluginPanel = new PluginPanel(GeneralConfigPanel.this, "yes", jarFile.getName(), "");
//			pluginPanels.add(pluginPanel);
//			pluginsPanel.add(pluginPanel);
//			((Window)getTopLevelAncestor()).pack();
		}}));
		addPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgRefreshPluginsLabel")) {public void actionPerformed(ActionEvent e)
		{
			refreshPlugins(xml);
			((Window)getTopLevelAncestor()).pack();
		}}));
		topPluginsPanel.add(addPanel);
		
//		setAutoConnectSubPanel(fsSubPanel);
		
//		add(ac);
		add(display);
		add(topPluginsPanel);
		
		add(safeMode = new JCheckBox(XMLResourceBundle.getBundledString("cfgSafeModeLabel")));
		
//		autoConnectType.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e)
//		{
//			if (e.getStateChange() != ItemEvent.SELECTED)
//				return;
//			if (e.getItem().equals(XMLResourceBundle.getBundledString("cfgFileLabel")))
//				setAutoConnectSubPanel(fsSubPanel);
//			else setAutoConnectSubPanel(dbSubPanel);
//			Window top = (Window)getTopLevelAncestor();
//			if (top != null)
//				top.pack();
//		}});
		
		if (xml != null)
			fill(xml);
	}
	
	private void refreshPlugins(String xml)
	{
		Map<String, Boolean> plugins = new TreeMap<String, Boolean>();
		for (String jarFile : DocExploreTool.getPluginDir().list())
			if (jarFile.endsWith(".jar"))
				plugins.put(jarFile, false);
		for (String pluginXml : StringUtils.getTagsContent(xml, "plugin"))
			plugins.put(StringUtils.getTagContent(pluginXml, "jar"), StringUtils.getBoolean(StringUtils.getTagContent(pluginXml, "use")));
		for (PluginPanel panel : pluginPanels)
			plugins.put(panel.jarName, panel.usePlugin.isSelected());
		pluginPanels.clear();
		pluginsPanel.removeAll();
		for (Map.Entry<String, Boolean> pair : plugins.entrySet())
		{
			PluginPanel panel = new PluginPanel(this, pair.getValue(), pair.getKey());
			pluginsPanel.add(panel);
			pluginPanels.add(panel);
		}
	}
	
	String browseClasses(File file)
	{
		try
		{
			List<Class<?>> metaDataPlugins = new LinkedList<Class<?>>();
			List<Class<?>> analysisPlugins = new LinkedList<Class<?>>();
			List<Class<?>> clientPlugins = new LinkedList<Class<?>>();
			List<Class<?>> serverPlugins = new LinkedList<Class<?>>();
			List<Class<?>> inputPlugins = new LinkedList<Class<?>>();
			
			List<URL> urls = Startup.extractDependencies(file.getName().substring(0, file.getName().length()-4), file.getName());
			urls.add(file.toURI().toURL());
			URLClassLoader loader = new URLClassLoader(urls.toArray(new URL [] {}), this.getClass().getClassLoader());
			
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (!entry.getName().endsWith(".class") || entry.getName().indexOf('$') > 0)
					continue;
				String className = entry.getName().substring(0, entry.getName().length()-6).replace('/', '.');
				Class<?> clazz = null;
				try {clazz = loader.loadClass(className); System.out.println("Reading "+className);}
				catch (NoClassDefFoundError e) {System.out.println("Couldn't read "+className);}
				if (clazz == null)
					continue;
				
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
					continue;
				if (MetaDataPlugin.class.isAssignableFrom(clazz))
					metaDataPlugins.add(clazz);
				if (AnalysisPlugin.class.isAssignableFrom(clazz))
					analysisPlugins.add(clazz);
				if (ClientPlugin.class.isAssignableFrom(clazz))
					clientPlugins.add(clazz);
				if (ServerPlugin.class.isAssignableFrom(clazz))
					serverPlugins.add(clazz);
				if (InputPlugin.class.isAssignableFrom(clazz))
					inputPlugins.add(clazz);
			}
			jarFile.close();
			
			@SuppressWarnings("unchecked")
			Pair<String, String> [] classes = new Pair [metaDataPlugins.size()+analysisPlugins.size()+clientPlugins.size()+serverPlugins.size()+inputPlugins.size()];
			if (classes.length == 0)
				throw new Exception("Invalid plugin (no entry points were found).");
			
			int cnt = 0;
			for (Class<?> clazz : metaDataPlugins)
				classes[cnt++] = new Pair<String, String>(clazz.getName(), "MetaData plugin") {public String toString() {return first+" ("+second+")";}};
			for (Class<?> clazz : analysisPlugins)
				classes[cnt++] = new Pair<String, String>(clazz.getName(), "Analysis plugin") {public String toString() {return first+" ("+second+")";}};
			for (Class<?> clazz : clientPlugins)
				classes[cnt++] = new Pair<String, String>(clazz.getName(), "Reader client plugin") {public String toString() {return first+" ("+second+")";}};
			for (Class<?> clazz : serverPlugins)
				classes[cnt++] = new Pair<String, String>(clazz.getName(), "Reader server plugin") {public String toString() {return first+" ("+second+")";}};
			for (Class<?> clazz : inputPlugins)
				classes[cnt++] = new Pair<String, String>(clazz.getName(), "Reader input plugin") {public String toString() {return first+" ("+second+")";}};
			@SuppressWarnings("unchecked")
			Pair<String, String> res = (Pair<String, String>)JOptionPane.showInputDialog(this, 
				"Please select an entry point for the plugin:", "Plugin entry point", JOptionPane.QUESTION_MESSAGE, null, classes, classes[0]);
			if (res != null)
				return res.first;
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	
	File browsePlugins()
	{
		try
		{
			File pluginDir = configFile.getParentFile();
			File file = DocExploreTool.getFileDialogs().openFile(DocExploreTool.getPluginCategory());
			
			File from = null, dest = null;
			while (from == null)
			{
				if (file == null)
					break;
				dest = new File(pluginDir, file.getName());
				if (!dest.exists() || file.equals(dest) || JOptionPane.showConfirmDialog(this, 
					XMLResourceBundle.getBundledString("cfgReplacePluginMessage"), XMLResourceBundle.getBundledString("cfgAddPluginLabel"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						from = file;
			}
			if (from == null)
				return null;
			
			if (!file.equals(dest))
			{
				FileUtils.copyFile(from, dest);
				//ByteUtils.copyFile(from, dest);
			}
			return dest;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	
//	void fillAutoConnect(ConnectionHandler.PastConnection con)
//	{
//		if (con.source instanceof DataLinkMySQLSource)
//		{
//			DataLinkMySQLSource source = (DataLinkMySQLSource)con.source;
//			autoConnectType.setSelectedItem(XMLResourceBundle.getBundledString("cfgMysqlLabel"));
//			acDBHost.setText(source.url);
//			acDBDatabase.setText(source.database);
//			acDBUser.setText(source.user);
//			acDBPassword.setText(source.password);
//		}
//		else if (con.source instanceof DataLinkFS2Source)
//		{
//			DataLinkFS2Source source = (DataLinkFS2Source)con.source;
//			autoConnectType.setSelectedItem(XMLResourceBundle.getBundledString("cfgFileLabel"));
//			acFSPath.setText(source.file);
//		}
//	}
//	void fillAutoConnect(String xml)
//	{
//		String autoConnect = StringUtils.getTagContent(xml, "autoconnect");
//		if (autoConnect != null)
//		{
//			String use = StringUtils.getTagContent(autoConnect, "use");
//			if (use != null && StringUtils.getBoolean(use))
//				useAutoConnect.setSelected(true);
//			String type = StringUtils.getTagContent(autoConnect, "type");
//			if (type != null && type.equals("mysql"))
//			{
//				autoConnectType.setSelectedItem(XMLResourceBundle.getBundledString("cfgMysqlLabel"));
//				String path = StringUtils.getTagContent(autoConnect, "path");
//				String host = StringUtils.getTagContent(autoConnect, "host");
//				String db = StringUtils.getTagContent(autoConnect, "database");
//				String user = StringUtils.getTagContent(autoConnect, "user");
//				String pass = StringUtils.getTagContent(autoConnect, "password");
//				if (path != null) acDBPath.setText(path);
//				if (host != null) acDBHost.setText(host);
//				if (db != null) acDBDatabase.setText(db);
//				if (user != null) acDBUser.setText(user);
//				if (pass != null) acDBPassword.setText(pass);
//			}
//			else if (type != null && type.equals("file"))
//			{
//				autoConnectType.setSelectedItem(XMLResourceBundle.getBundledString("cfgFileLabel"));
//				String path = StringUtils.getTagContent(autoConnect, "path");
//				if (path != null) acFSPath.setText(path);
//			}
//		}
//	}
	
	public void fill(String xml)
	{
//		fillAutoConnect(xml);
		
		String display = StringUtils.getTagContent(xml, "display");
		if (display != null)
		{
			String width = StringUtils.getTagContent(display, "width"), height = StringUtils.getTagContent(display, "height");
			String lang = StringUtils.getTagContent(display, "lang");
			String fs = StringUtils.getTagContent(display, "fullscreen");
			String cursor = StringUtils.getTagContent(display, "useNativeCursor");
			String help = StringUtils.getTagContent(display, "help");
			if (width != null) displayWidth.setText(width);
			if (height != null) displayHeight.setText(height);
			if (lang != null && lang.toLowerCase().contains("fr"))
				displayLang.setSelectedItem(XMLResourceBundle.getBundledString("cfgFrenchLabel"));
			else
			{
				displayLang.setSelectedItem(Locale.getDefault().getLanguage().toLowerCase().contains("fr") ? XMLResourceBundle.getBundledString("cfgFrenchLabel") :
					XMLResourceBundle.getBundledString("cfgEnglishLabel"));
			}
			displayHelp.setSelected(help != null && StringUtils.getBoolean(help));
			displayFullscreen.setSelected(fs != null && StringUtils.getBoolean(fs));
			displayNativeCursor.setSelected(cursor == null ? true : StringUtils.getBoolean(cursor));
		}
		else
		{
			displayNativeCursor.setSelected(true);
			displayLang.setSelectedItem(Locale.getDefault().getLanguage().toLowerCase().contains("fr") ? XMLResourceBundle.getBundledString("cfgFrenchLabel") :
				XMLResourceBundle.getBundledString("cfgEnglishLabel"));
		}
		
		refreshPlugins(xml);
//		List<String> pluginXmls = StringUtils.getTagsContent(xml, "plugin");
//		for (String pluginXml : pluginXmls)
//		{
//			PluginPanel pluginPanel = new PluginPanel(this, pluginXml);
//			pluginPanels.add(pluginPanel);
//			pluginsPanel.add(pluginPanel);
//		}
		
		String safe = StringUtils.getTagContent(xml, "safe");
		safeMode.setSelected(safe != null && StringUtils.getBoolean(safe));
	}
	
//	void setAutoConnectSubPanel(JPanel subPanel)
//	{
//		autoConnectSubPanel.removeAll();
//		autoConnectSubPanel.add(subPanel);
//	}
	
	void write(File config, ServerConfigPanel readerConfig) throws Exception
	{
		final String xml = config.exists() ? StringUtils.readFile(config) : "<config></config>";
		
		StringBuffer sb = new StringBuffer();
		sb.append("<config>\n\t<autoconnect>");
//		sb.append("\n\t\t<use>").append(useAutoConnect.isSelected() ? "yes" : "no").append("</use>\n\t\t");
//		if (autoConnectType.getSelectedItem().equals(XMLResourceBundle.getBundledString("cfgMysqlLabel")))
//			sb.append("<type>mysql</type>\n\t\t<path>").
//				append(acDBPath.getText()).append("</path>\n\t\t<host>").
//				append(acDBHost.getText()).append("</host>\n\t\t<database>").
//				append(acDBDatabase.getText()).append("</database>\n\t\t<user>").
//				append(acDBUser.getText()).append("</user>\n\t\t<password>").
//				append(acDBPassword.getText()).append("</password>\n\t");
//		else if (autoConnectType.getSelectedItem().equals(XMLResourceBundle.getBundledString("cfgFileLabel")))
//			sb.append("<type>file</type>\n\t\t<path>").
//				append(acFSPath.getText()).append("</path>\n\t");
//		else sb.append("\n\t");
		sb.append(StringUtils.getTagContent(xml, "autoconnect"));
		sb.append("</autoconnect>\n");
		
		sb.append("\n\t<display>\n\t\t<lang>").append(displayLang.getSelectedItem().equals(XMLResourceBundle.getBundledString("cfgEnglishLabel")) ? "en" : "fr").append("</lang>\n\t\t<width>").
			append(displayWidth.getText()).append("</width>\n\t\t<height>").
			append(displayHeight.getText()).append("</height>\n\t\t<help>").
			append(displayHelp.isSelected() ? "yes" : "no").append("</help>\n\t\t<fullscreen>").
			append(displayFullscreen.isSelected() ? "yes" : "no").append("</fullscreen>\n\t\t<useNativeCursor>").
			append(displayNativeCursor.isSelected() ? "yes" : "no").append("</useNativeCursor>\n\t</display>\n");
		
		for (PluginPanel panel : pluginPanels)
		{
			sb.append("\n\t<plugin>\n\t\t<use>").append(panel.usePlugin.isSelected() ? "yes" : "no").append("</use>\n\t\t<jar>").
				append(panel.jarName).append("</jar>\n\t</plugin>\n");
		}
		sb.append("\n\t<safe>").append(safeMode.isSelected() ? "yes" : "no").append("</safe>\n");
		try {sb.append("\n\t<idle>"+Integer.parseInt(readerConfig.timeoutField.getText())+"</idle>\n");}
		catch (Throwable t) {}
		
		sb.append("</config>\n");
		
		StringUtils.writeFile(config, sb.toString());
		//System.out.println(sb.toString());
	}
}
