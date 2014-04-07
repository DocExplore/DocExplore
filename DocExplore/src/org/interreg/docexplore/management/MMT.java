package org.interreg.docexplore.management;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.DocExplore;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.plugin.PluginManager;
import org.interreg.docexplore.util.GuiUtils;


public class MMT extends DocExploreTool
{
	public static void main(final String [] args)
	{
		final Startup startup = new Startup(XMLResourceBundle.getBundledString("frameTitle"), "logoMMT.png", true, true, true, true);
		
		try
		{
			final MainWindow win = new MainWindow(startup, new PluginManager(startup));
			JOptionPane.setRootFrame(win);
			
			win.setSize(800, 600);
			if (startup.winSize == null)
				SwingUtilities.invokeLater(new Runnable() {public void run() {win.setExtendedState(JFrame.MAXIMIZED_BOTH);}});
			else
			{
				win.setSize(startup.winSize[0], startup.winSize[1]);
				GuiUtils.centerOnScreen(win);
			}
			
			startup.screen.setText("Autoconnecting");
			if (startup.autoConnectLink[0] != null)
				try {win.setLink(startup.autoConnectLink[0]);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			
			win.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					startup.shutdown();
					DocExplore.main(args);
				}
			});
			win.setVisible(true);
			
			startup.startupComplete();
		}
		catch (Exception e)
		{
			ErrorHandler.defaultHandler.submit(e);
		}
	}
	
	
}
