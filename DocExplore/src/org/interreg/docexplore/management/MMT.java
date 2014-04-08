/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
