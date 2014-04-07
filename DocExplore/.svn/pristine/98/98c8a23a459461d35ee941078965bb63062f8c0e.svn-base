package org.interreg.docexplore.authoring;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.util.GuiUtils;

public class AT extends DocExploreTool
{
	public static void main(String [] args) throws Exception
	{
		Startup startup = new Startup(XMLResourceBundle.getBundledString("frameTitle"), "logoAT.png", true, true, true, true);
		
		if (startup.autoConnectLink[0] == null)
			throw new Exception("Missing auto connect link in config.xml!");
		
		startup.screen.setText("Autoconnecting");
		DocExploreDataLink link = new DocExploreDataLink();
		link.setLink(startup.autoConnectLink[0]);
		
		AuthoringToolFrame win = new AuthoringToolFrame(link, startup);
		JOptionPane.setRootFrame(win);
		win.pack();
		win.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		win.setSize(800, 600);
		if (startup.winSize == null)
			win.setExtendedState(JFrame.MAXIMIZED_BOTH);
		else
		{
			win.setSize(startup.winSize[0], startup.winSize[1]);
			GuiUtils.centerOnScreen(win);
		}
		win.setVisible(true);
		
		//new ResourceMonitor().setVisible(true);
		
		startup.startupComplete();
		
		boolean recovering = win.recovery && JOptionPane.showConfirmDialog(win, 
			XMLResourceBundle.getBundledString("generalRecoveryMessage"),
			XMLResourceBundle.getBundledString("generalRecoveryLabel"),
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		if (!recovering)
		{
			if (args.length > 0)
				win.menu.load(new File(args[0]), true);
			else if (win.recovery)
				win.menu.newFile(true);
		}
	}
}
