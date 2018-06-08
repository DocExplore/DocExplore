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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.authoring.AT;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.MMT;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.ReaderElectronApp;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

public class DocExplore extends DocExploreTool
{
	static Boolean safeMode = null;
	
	@SuppressWarnings("serial")
	public static void main(final String [] args)
	{
		//read only the language from the config file
		try
		{
			File file = new File(getHomeDir(), "config.xml");
			if (file.exists())
			{
				String config = StringUtils.readFile(file);
				
				String safe = StringUtils.getTagContent(config, "safe");
				if (safe != null)
				{
					if (safeMode == null)
						safeMode = StringUtils.getBoolean(safe);
					else if (safeMode.booleanValue() != StringUtils.getBoolean(safe))
						relaunch(null);
				}
				
				String display = StringUtils.getTagContent(config, "display");
				if (display != null)
				{
					String langXml = StringUtils.getTagContent(display, "lang");
					if (langXml != null)
					{
						if (langXml.toLowerCase().startsWith("fr"))
							Locale.setDefault(Locale.FRENCH);
						else Locale.setDefault(Locale.ENGLISH);
					}
				}
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		if (args.length > 0 && args[0].endsWith(".pres"))
		{
			new Thread() {public void run()
			{
				try {AT.main(args);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
			}}.start();
			return;
		}
		
		final JFrame win = new JFrame("DocExplore "+version());
		win.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new LooseGridLayout(0, 2, 10, 10, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
		win.add(panel, BorderLayout.CENTER);
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("mmt-logo.png", 192, 192)) {public void actionPerformed(ActionEvent arg0)
		{
			win.setVisible(false);
			new Thread() {public void run()
			{
				try {MMT.main(args);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
			}}.start();
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("mmtLabel")+"</b><br>"+Lang.s("mmtMessage")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("at-logo.png", 192, 192)) {public void actionPerformed(ActionEvent arg0)
		{
			win.setVisible(false);
			new Thread() {public void run()
			{
				try {AT.main(args);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
			}}.start();
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("atLabel")+"</b><br>"+Lang.s("atMessage")+"</html>"));
		
		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("reader-logo.png", 192, 192)) {public void actionPerformed(ActionEvent arg0)
		{
			win.setVisible(false);
			new Thread() {public void run()
			{
				try {ReaderElectronApp.main(args);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
			}}.start();
		}}));
		panel.add(new JLabel("<html><b>"+Lang.s("readerLabel")+"</b><br>"+Lang.s("readerMessage")+"</html>"));
		
		JPanel oldReaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		oldReaderPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("reader-logo-old.png", 64, 64)) {public void actionPerformed(ActionEvent arg0)
		{
			win.setVisible(false);
			new Thread() {public void run()
			{
				try {ReaderApp.main(args);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
			}}.start();
		}}));
		panel.add(oldReaderPanel);
		panel.add(new JLabel("<html><b>"+Lang.s("readerOldLabel")+"</b><br>"+Lang.s("readerOldMessage")+"</html>"));
		
		
//		panel.add(new JLabel(" ")); panel.add(new JLabel(" ")); panel.add(new JLabel(" ")); panel.add(new JLabel(" "));
//		
//		panel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("conf-logo.png")) {public void actionPerformed(ActionEvent arg0)
//		{
//			//win.setVisible(false);
//			new Thread() {public void run()
//			{
//				try
//				{
//					if (ConfigPanel.showConfigDialog())
//					{
//						win.setVisible(false);
//						main(new String [0]);
//					}
//				}
//				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
//			}}.start();
//		}}));
//		panel.add(new JLabel("<html><b>"+XMLResourceBundle.getBundledString("confLabel")+"</b><br>"+XMLResourceBundle.getBundledString("confMessage")+"</html>"));
		
		panel.add(new JLabel(" "));
		JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		configPanel.add(new JLabel("<html><b>"+Lang.s("confLabel")+"</b><br>"));
		configPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("gears-24x24.png")) {public void actionPerformed(ActionEvent arg0)
		{
			//win.setVisible(false);
			new Thread() {public void run()
			{
				try
				{
					if (ConfigPanel.showConfigDialog())
					{
						win.setVisible(false);
						main(new String [0]);
					}
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e); System.exit(0);}
			}}.start();
		}}));
		panel.add(configPanel);
		
		panel.add(new JLabel(" "));
		JPanel homePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel rootPanel = new JPanel(new LooseGridLayout(0, 1, 5, 2, false, false, SwingConstants.RIGHT, SwingConstants.CENTER, false, false));
		rootPanel.add(new JLabel("<html><b>"+Lang.s("homeLabel")+"</b><br>"));
		rootPanel.add(new JLabel(getHomeDir().getAbsolutePath()));
		homePanel.add(rootPanel);
		homePanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("home-24x24.png")) {public void actionPerformed(ActionEvent e)
		{
			final String [] current = {null};
			try {current[0] = getHomeDir().getCanonicalPath();}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex, true);}
			
			JPopupMenu menu = new JPopupMenu();
			List<String> homes = getHomes();
			if (homes != null)
				for (final String home : homes)
					menu.add(new JMenuItem(new AbstractAction(home) {public void actionPerformed(ActionEvent arg0)
					{
						try
						{
							setHome(home);
							SwingUtilities.invokeLater(new Runnable() {public void run() {relaunch(win);}});
						}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
					}}) {{setEnabled(!home.equals(current[0]));}});
			if (menu.getComponentCount() > 0)
				menu.addSeparator();
			menu.add(new JMenuItem(new AbstractAction(Lang.s("switchHomeLabel")) {public void actionPerformed(ActionEvent arg0)
			{
				File where = askForHome(Lang.s("switchHomeMessage"));
				if (where == null)
					return;
				try
				{
					setHome(where.getCanonicalPath());
					SwingUtilities.invokeLater(new Runnable() {public void run() {relaunch(win);}});
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			}}));
			menu.show((JButton)e.getSource(), 0, 0);		
		}}));
		panel.add(homePanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		win.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("exitLabel")) {
			public void actionPerformed(ActionEvent e) {System.exit(0);}}));
		
		win.pack();
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GuiUtils.centerOnScreen(win);
		win.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	private static void relaunch(Component comp)
	{
		final JDialog dialog = new JDialog((Frame)null, Lang.s("restartLabel"), true);
		JPanel content = new JPanel(new LooseGridLayout(0, 1, 10, 10, false, false, SwingConstants.CENTER, SwingConstants.TOP, true, false));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		JLabel message = new JLabel(Lang.s("restartMessage"), ImageUtils.getIcon("free-32x32.png"), SwingConstants.LEFT);
		message.setIconTextGap(20);
		content.add(message);
		content.add(new JButton(new AbstractAction(Lang.s("quitLabel")) {public void actionPerformed(ActionEvent arg0)
		{
			System.exit(0);
		}}));
		dialog.add(content);
		dialog.pack();
		GuiUtils.centerOnComponent(dialog, comp);
		dialog.setVisible(true);
	}
}
