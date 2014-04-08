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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.filesystem.DataLinkFileSystem;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.datalink.mysql.DataLinkMySQL;
import org.interreg.docexplore.datalink.mysql.DataLinkMySQLSource;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.plugin.analysis.AnalysisPlugin;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.nfd.JNativeFileDialog;
import org.interreg.docexplore.reader.plugin.ClientPlugin;
import org.interreg.docexplore.reader.plugin.InputPlugin;
import org.interreg.docexplore.reader.plugin.ServerPlugin;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.StringUtils;

/**
 * Class responsible for parsing the configuration file and loading the plugins for all DocExplore tools.
 * @author Alexander Burnett
 */
public class Startup
{
	public JFrame splash;
	public SplashScreen screen;
	public String [] path = {null};
	public Process [] connection = {null};
	public DataLink [] autoConnectLink = {null};
	public int [] winSize = null;
	public String lang = null;
	public boolean showHelp = false;
	public boolean fullscreen = false;
	public boolean nativeCursor = true;
	public boolean safeMode = false;
	public int idle = 300;
	
	public static class PluginConfig
	{
		public final Class<?> clazz;
		public final File jarFile;
		public final File dependencies;
	
		public PluginConfig(Class<?> clazz, File jarFile, File dependencies)
		{
			this.clazz = clazz;
			this.jarFile = jarFile;
			this.dependencies = dependencies;
		}
	}
	public List<PluginConfig> plugins = new LinkedList<PluginConfig>();
	
	public static class PluginClassLoader extends URLClassLoader
	{
		public PluginClassLoader(ClassLoader cl) {super(new URL [0], cl);}
		public void addURLs(Collection<URL> urls) {for (URL url : urls) addURL(url);}
	}
	public PluginClassLoader pluginClassloader = null;
	
	/**
	 * Begins the startup procedure by displaying a splash screen, reading the configuration file and loading the plugins.
	 * @param appName The name of application (only used on MacOS X to attempt to natively set the app name)
	 * @param logo Splash screen logo
	 * @param useSplash Whether or not to display a splash screen
	 * @param useAutoConnect Whether or not to automatically connect to the {@link org.interreg.docexplore.datalink.DataLink.java} provided in the configuration file.
	 * @param useDisplay Whether or not to override the default display values with those provided in the configuration file.
	 * @param usePlugins Whether or not to load the plugins provided in the configuration file.
	 */
	public Startup(String appName, String logo, boolean useSplash, boolean useAutoConnect, boolean useDisplay, boolean usePlugins)
	{
		if (useSplash)
		{
			splash = new JFrame();
			screen = new SplashScreen(logo);
			splash.add(screen);
			splash.setUndecorated(true);
			splash.pack();
			//splash.setAlwaysOnTop(true);
			GuiUtils.centerOnScreen(splash);
			splash.setVisible(true);
			screen.setText("Initialization...");
		}
		
		try
		{
			File file = new File(DocExploreTool.getHomeDir(), "config.xml");
			if (file.exists())
			{
				String config = StringUtils.readFile(file);
				if (useAutoConnect)
				{
					String autoConnect = StringUtils.getTagContent(config, "autoconnect");
					String useAC = autoConnect == null ? null : StringUtils.getTagContent(autoConnect, "use");
					if (autoConnect != null && (useAC == null || StringUtils.getBoolean(useAC)))
					{
						if (screen != null)
							screen.setText("Connection to source...");
						
						String type = StringUtils.getTagContent(autoConnect, "type");
						
						if (type != null && type.equals("mysql"))
						{
							path[0] = StringUtils.getTagContent(autoConnect, "path");
							String host = StringUtils.getTagContent(autoConnect, "host");
							String db = StringUtils.getTagContent(autoConnect, "database");
							String user = StringUtils.getTagContent(autoConnect, "user");
							String pass = StringUtils.getTagContent(autoConnect, "password");
							
							DataLinkMySQLSource source = new DataLinkMySQLSource(host, db, user, pass);
							autoConnectLink[0] = source.getDataLink(false);
							if (autoConnectLink[0] == null)
							{
								connection[0] = Runtime.getRuntime().exec(path[0]+"\\mysqld.exe --max_allowed_packet=1024M");
								try {Thread.sleep(2000);}
								catch (Exception ex) {}
								autoConnectLink[0] = source.getDataLink();
							}
						}
						else if (type != null && type.equals("file"))
						{
							path[0] = StringUtils.getTagContent(autoConnect, "path");
							autoConnectLink[0] = new DataLinkFS2Source(path[0]).getDataLink();
						}
						else if (type != null && type.equals("file-depr"))
						{
							path[0] = StringUtils.getTagContent(autoConnect, "path");
							autoConnectLink[0] = new DataLinkFileSystem.DataLinkFileSystemSource(path[0]).getDataLink();
						}
					}
				}
				
				if (useDisplay)
				{
					String display = StringUtils.getTagContent(config, "display");
					if (display != null)
					{
						String width = StringUtils.getTagContent(display, "width"), height = StringUtils.getTagContent(display, "height");
						if (width != null && height != null && width.trim().length() > 0 && height.trim().length() > 0)
						{
							try {winSize = new int [] {Integer.parseInt(width), Integer.parseInt(height)};}
							catch (Exception e) {}
						}
						
						String help = StringUtils.getTagContent(display, "help");
						if (help != null)
							showHelp = StringUtils.getBoolean(help);
						
						String fs = StringUtils.getTagContent(display, "fullscreen");
						if (fs != null)
							fullscreen = StringUtils.getBoolean(fs);
						
						String nc = StringUtils.getTagContent(display, "useNativeCursor");
						if (nc != null)
							nativeCursor = StringUtils.getBoolean(nc);
						
						String langXml = StringUtils.getTagContent(display, "lang");
						if (langXml != null)
							lang = langXml;
					}
				}
				
				if (usePlugins)
				{
					List<String> pluginXmls = StringUtils.getTagsContent(config, "plugin");
					pluginClassloader = new PluginClassLoader(this.getClass().getClassLoader());
					for (String plugin : pluginXmls)
						try
						{
							String use = StringUtils.getTagContent(plugin, "use");
							if (use != null && !StringUtils.getBoolean(use))
								continue;
							String jarName = StringUtils.getTagContent(plugin, "jar");
							File jarFile = new File(DocExploreTool.getPluginDir(), jarName);
							
							List<String> classes = getPluginEntries(jarFile, pluginClassloader);

							for (String className : classes)
								plugins.add(new PluginConfig(
									Class.forName(className, true, pluginClassloader), 
									jarFile,
									new File(DocExploreTool.getPluginDir(), "dependencies/"+jarName.substring(0, jarName.length()-4))));
						}
						catch (Exception e) {e.printStackTrace();}
				}
				
				String safe = StringUtils.getTagContent(config, "safe");
				safeMode = safe != null && StringUtils.getBoolean(safe);
				
				String idleString = StringUtils.getTagContent(config, "idle");
				if (idleString != null)
					try {idle = Integer.parseInt(idleString);}
					catch (Throwable t) {}
			}
			else System.out.println("Couldn't find config file, using defaults");
		}
		catch (Exception e)
		{
			ErrorHandler.defaultHandler.submit(e);
			System.exit(1);
		}
		
		if (safeMode)
		{
			System.out.println("Safe mode enabled");
			JNativeFileDialog.noNative = true;
//			try {System.setProperty("apple.laf.useScreenMenuBar", "false");}
//			catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		}
		else
		{
			JNativeFileDialog.noNative = false;
//			try {System.setProperty("apple.laf.useScreenMenuBar", "true");}
//			catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		}
		
		if (lang == null)
			lang = Locale.getDefault().getLanguage().toLowerCase();
		if (lang.startsWith("fr"))
			Locale.setDefault(Locale.FRENCH);
		else Locale.setDefault(Locale.ENGLISH);

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
//				if ("Nimbus".equals(info.getName()))
//					{UIManager.setLookAndFeel(info.getClassName()); break;}
		}
		catch (Exception e) {e.printStackTrace();}
		
//		try {System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);}
//		catch (Exception e) {e.printStackTrace();}
		
		if (screen != null)
			screen.setText("Interface startup...");
	}
	
	List<String> getPluginEntries(File file, PluginClassLoader loader)
	{
		try
		{
			List<String> pluginEntries = new LinkedList<String>();
			List<URL> urls = Startup.extractDependencies(file.getName().substring(0, file.getName().length()-4), file.getName());
			urls.add(file.toURI().toURL());
			loader.addURLs(urls);
			
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (!entry.getName().endsWith(".class") || entry.getName().indexOf('$') > 0)
					continue;
				String className = entry.getName().substring(0, entry.getName().length()-6).replace('/', '.');
				Class<?> clazz = null;
				try {clazz = loader.loadClass(className);}
				catch (NoClassDefFoundError e) {}
				if (clazz == null)
					continue;
				
				if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
					continue;
				if (MetaDataPlugin.class.isAssignableFrom(clazz) || AnalysisPlugin.class.isAssignableFrom(clazz) || ClientPlugin.class.isAssignableFrom(clazz) || 
					ServerPlugin.class.isAssignableFrom(clazz) || InputPlugin.class.isAssignableFrom(clazz))
						{System.out.println("Found plugin entry '"+clazz.getName()+"'"); pluginEntries.add(clazz.getName());}
			}
			jarFile.close();
			
			return pluginEntries;
		}
		catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	
	/**
	 * Scans a plugin (a jar file) for nested jar files. Nested jar files will be extracted to the dependencies folder (in a subfolder derived from the name of the plugin).
	 * If the jar files are already extracted from a previous execution, they are not overwritten. 
	 * @param pluginName Qualified name of the plugin class 
	 * @param jarName Name of the containing jar file
	 * @return The list of extracted jar files. These URLs should be added to the proper {@link ClassLoader} to load the plugin.
	 * @throws Exception
	 */
	public static List<URL> extractDependencies(String pluginName, String jarName) throws Exception
	{
		List<URL> urls = new LinkedList<URL>();
		File depDir = new File(DocExploreTool.getPluginDir(), "dependencies/"+jarName.substring(0, jarName.length()-4));
		depDir.mkdirs();
		
		JarFile jarFile = new JarFile(new File(DocExploreTool.getPluginDir(), jarName));
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements())
		{
			JarEntry entry = entries.nextElement();
			if (!entry.getName().endsWith(".jar"))
				continue;
			
			File dep = new File(depDir, entry.getName().substring(entry.getName().lastIndexOf('/')));
			if (!dep.exists())
			{
				System.out.println("Extracting dependencies for "+jarName+"/"+entry.getName()+" ("+pluginName+")");
				
				byte [] bytes = ByteUtils.readStream(jarFile.getInputStream(entry));
				if (bytes == null)
				{
					jarFile.close();
					throw new IOException("Can't extract "+entry.getName());
				}
				
				FileOutputStream output = new FileOutputStream(dep);
				output.write(bytes);
				output.close();
			}
			else System.out.println("Using cached dependencies for "+jarName+"/"+entry.getName()+" ("+pluginName+")");
			
			urls.add(dep.toURI().toURL());
		}
		jarFile.close();
		return urls;
	}
	
	/**
	 * Returns a subset of detected plugins that are of a given type.
	 * @param classes A list of plugin classes
	 * @return
	 */
	public List<PluginConfig> filterPlugins(Class<?> ... classes)
	{
		LinkedList<PluginConfig> res = new LinkedList<PluginConfig>();
		for (PluginConfig config : plugins)
			for (int i=0;i<classes.length;i++)
				if (classes[i].isAssignableFrom(config.clazz))
				{
					res.add(config);
					break;
				}
		return res;
	}
	
	/**
	 * Hides the splashscreen once the application is initialized.
	 */
	public void startupComplete()
	{
		screen.setText("Startup complete");
		try {Thread.sleep(1000);}
		catch (Exception e) {}
		splash.setVisible(false);
	}
	
	/**
	 * Disposes the default {@link org.interreg.docexplore.datalink.DataLink.java} when application shuts down.
	 */
	public void shutdown()
	{
		System.out.println("Shutdown...");
		if (autoConnectLink[0] != null && autoConnectLink[0] instanceof DataLinkMySQL && connection[0] != null)
			connection[0].destroy();
	}
}
