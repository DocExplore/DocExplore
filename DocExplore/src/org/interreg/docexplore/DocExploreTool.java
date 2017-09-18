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

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.FileDialogs;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.nfd.JNativeFileDialog;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;
import org.interreg.docexplore.util.ZipUtils;

public class DocExploreTool
{
	private static String version = "?.?.?";
	private static File homeDir = null, execDir = null, pluginDir = null;
	static {initDirectories();}
	
	public static File getHomeDir() {return homeDir;}
	public static File getExecutableDir() {return execDir;}
	public static File getPluginDir() {return pluginDir;}
	
	public static File initDirectories()
	{
		try {readVersion();}
		catch (Throwable e) {}
		System.out.println("Version "+version);
		
		execDir = new File(".").getAbsoluteFile();
		System.out.println("Executable dir: "+execDir.getAbsolutePath());
		
		pluginDir = new File(System.getProperty("user.home")+File.separator+"DocExplorePlugins");
		if (!pluginDir.exists())
			initPlugins();
		else
		{
			File infoFile = new File(pluginDir, ".info");
			String info = null;
			if (infoFile.exists()) try {info = StringUtils.readFile(infoFile);}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			if (info == null || !info.equals(version))
				initPlugins();
		}
		System.out.println("Plugins dir: "+pluginDir.getAbsolutePath());
		
		System.out.println("OS: "+System.getProperty("os.name"));
		System.out.println("Arch: "+System.getProperty("os.arch"));
		System.out.println("Java: "+System.getProperty("java.version"));
		System.out.println("Encoding: "+System.getProperty("file.encoding"));
		System.out.println("PID: "+Uninstaller.getPID());
		
		setPreferredLAF();
		
		final File [] file = {null};
		List<String> homes = getHomes();
		if (homes != null)
			file[0] = new File(homes.get(0));
		if (file[0] == null)
		{
			//JOptionPane.showMessageDialog(null, "Test the default font too see the difference.\nAnd the line spacing.");
			file[0] = askForHome(XMLResourceBundle.getBundledString("chooseHomeMessage"));
			if (file[0] == null)
				System.exit(0);
		}
		
		try
		{
			setHome(file[0].getCanonicalPath());
			initIfNecessary(file[0]);
		}
		catch (Exception e)
		{
			ErrorHandler.defaultHandler.submit(e); 
			if (homeDir == null) 
				System.exit(0);
		}
		
		return file[0];
	}
	
	public static void setPreferredLAF()
	{
		try
		{
			String laf = UIManager.getSystemLookAndFeelClassName();
			if (laf.equals("javax.swing.plaf.metal.MetalLookAndFeel"))
			{
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
					if (info.getName().equals("Nimbus"))
					{
						laf = info.getClassName();
						break;
					}
			}
			UIManager.setLookAndFeel(laf);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private static void initPlugins()
	{
		pluginDir.mkdirs();
		
		File depDir = new File(pluginDir, "dependencies");
		if (depDir.exists()) try {FileUtils.deleteDirectory(depDir);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		
		File pluginArchive = new File(execDir, "plugins.zip");
		if (pluginArchive.exists()) try {ZipUtils.unzip(pluginArchive, pluginDir);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		
		File infoFile = new File(pluginDir, ".info");
		try {StringUtils.writeFile(infoFile, version);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		
		System.out.println("Plugins directory inited for "+version);
	}
	
	private static void readVersion() throws Exception
	{
		InputStream in = ClassLoader.getSystemResourceAsStream("version.xml");
		String xml = StringUtils.readStream(in);
		in.close();
		version = StringUtils.getTagContent(xml, "major").trim()+"."
			+StringUtils.getTagContent(xml, "minor").trim()+"."
			+StringUtils.getTagContent(xml, "build").trim();
	}
	public static String version() {return version;}
	
	@SuppressWarnings("serial")
	protected static File askForHome(String text)
	{
		final File [] file = {null};
		final JDialog dialog = new JDialog((Frame)null, XMLResourceBundle.getBundledString("homeLabel"), true);
		JPanel content = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false, SwingConstants.CENTER, SwingConstants.TOP, true, false));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		JLabel message = new JLabel(text, ImageUtils.getIcon("free-64x64.png"), SwingConstants.LEFT);
		message.setIconTextGap(20);
		//message.setFont(Font.decode(Font.SANS_SERIF));
		content.add(message);
		
		final JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		pathPanel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("homeLabel")+":</b></html>"));
		final JTextField pathField = new JTextField(System.getProperty("user.home")+File.separator+"DocExplore", 40);
		pathPanel.add(pathField);
		pathPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("browseLabel"))
		{
			JNativeFileDialog nfd = null;
			public void actionPerformed(ActionEvent arg0)
			{
				if (nfd == null)
				{
					nfd = new JNativeFileDialog();
					nfd.acceptFiles = false;
					nfd.acceptFolders = true;
					nfd.multipleSelection = false;
					nfd.title = XMLResourceBundle.getBundledString("homeLabel");
				}
				nfd.setCurrentFile(new File(pathField.getText()));
				if (nfd.showOpenDialog())
					pathField.setText(nfd.getSelectedFile().getAbsolutePath());
			}
		}));
		content.add(pathPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgOkLabel")) {public void actionPerformed(ActionEvent e)
		{
			File res = new File(pathField.getText());
			if (res.exists() && !res.isDirectory() || !res.exists() && !res.mkdirs())
				JOptionPane.showMessageDialog(dialog, XMLResourceBundle.getBundledString("homeErrorMessage"), XMLResourceBundle.getBundledString("errorLabel"), JOptionPane.ERROR_MESSAGE);
			else
			{
				file[0] = res;
				dialog.setVisible(false);
			}
		}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("cfgCancelLabel")) {public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		content.add(buttonPanel);
		
		dialog.getContentPane().add(content);
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		return file[0];
	}
	
	@SuppressWarnings("unchecked")
	protected static void setHome(String value)
	{
		File homePointerFile = new File(System.getProperty("user.home")+"/.docexplore");
		if (homePointerFile.exists())
		{
			List<String> homes = null;
			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(homePointerFile));
				homes = (List<String>)in.readObject();
				in.close();
			}
			catch (Exception e)
			{
				ErrorHandler.defaultHandler.submit(e, true);
				try {homePointerFile.delete();}
				catch (Exception e2) {e2.printStackTrace();}
			}	
			
			if (homes != null)
			{
				for (int i=0;i<homes.size();i++)
					if (homes.get(i).equals(value))
						homes.remove(i--);
				homes.add(0, value);
				
				try
				{
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(homePointerFile, false));
					out.writeObject(homes);
					out.close();
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
		}
		if (!homePointerFile.exists())
		{
			try
			{
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(homePointerFile, false));
				List<String> homes = new LinkedList<String>();
				homes.add(value);
				out.writeObject(homes);
				out.close();
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		}
		homeDir = new File(value);
		System.out.println("DocExplore home directory: "+value);
	}
	
	@SuppressWarnings("unchecked")
	protected static List<String> getHomes()
	{
		File homePointerFile = new File(System.getProperty("user.home")+"/.docexplore");
		if (!homePointerFile.exists())
			return null;
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(homePointerFile));
			List<String> homes = (List<String>)in.readObject();
			in.close();
			return homes;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return null;
	}
	
	private static void initIfNecessary(File home) throws Exception
	{
		File readerDir = new File(home, "reader");
		File readerIndexFile = new File(readerDir, "index.xml");
		File configFile = new File(home, "config.xml");
		File dbFile = new File(home, "db");
		if (!readerDir.exists() || !readerIndexFile.exists() || !configFile.exists() || !dbFile.exists())
		{
			File initArchive = new File(execDir, "init.zip");
			if (initArchive.exists())
			{
				try {ZipUtils.unzip(initArchive, home);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}
			
			if (!readerDir.exists())
				readerDir.mkdir();
			if (!readerIndexFile.exists())
				StringUtils.writeFile(readerIndexFile, "<Index>\n</Index>");
			if (!configFile.exists())
				StringUtils.writeFile(configFile, "<config>\n\t<autoconnect>\n\t\t<use>yes</use>\n\t\t<type>file</type>\n\t\t<path>db</path>\n\t</autoconnect>\n</config>");
			if (!dbFile.exists())
				dbFile.mkdir();
		}
	}
	
	private static FileDialogs fileDialogs = null;
	public static FileDialogs.Category getImagesCategory() {return getFileDialogs().getOrCreateCategory("Images", ImageUtils.supportedFormats);}
	public static FileDialogs.Category getPluginCategory() {return getFileDialogs().getOrCreateCategory("DocExplore plugins", Collections.singleton("jar"));}
	public static FileDialogs.Category getIBookCategory() {return getFileDialogs().getOrCreateCategory("DocExplore Interactive Book", Collections.singleton("dib"));}
	public static FileDialogs.Category getWebIBookCategory() {return getFileDialogs().getOrCreateCategory("DocExplore Interactive Web Book", Collections.singleton("zip"));}
	public static FileDialogs.Category getMobileIBookCategory() {return getFileDialogs().getOrCreateCategory("DocExplore Interactive Mobile Book", Collections.singleton("depa"));}
	public static FileDialogs.Category getPresentationCategory() {return getFileDialogs().getOrCreateCategory("DocExplore Presentation", Collections.singleton("pres"));}
	public static FileDialogs.Category getBookCategory() {return getFileDialogs().getOrCreateCategory("DocExplore Book", Collections.singleton("dmb"));}
	public static FileDialogs getFileDialogs()
	{
		if (fileDialogs == null || !fileDialogs.fdcache.getParentFile().equals(homeDir))
			fileDialogs = new FileDialogs(homeDir);
		return fileDialogs;
	}
	
}
