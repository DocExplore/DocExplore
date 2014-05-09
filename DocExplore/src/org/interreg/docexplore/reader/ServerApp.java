/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.reader.net.ReaderServer;

/**
 * Executable for the standalone server app
 * @author Alexander Burnett
 *
 */
public class ServerApp
{
	ReaderServer server = null;
	
	public ServerApp(Startup startup) throws Exception
	{
		File baseDir = new File(DocExploreTool.getHomeDir(), "reader");
		baseDir.mkdirs();
		
		this.server = new ReaderServer(8787, baseDir, startup);
		server.open();
		
//		JFrame win = new JFrame("ReaderServer");
//		win.add(new ReaderServerMonitor(server));
//		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		win.pack();
//		win.setVisible(true);
		
		//new ResourceMonitor().setVisible(true);
	}
	
	public static void main(String [] args) throws Exception
	{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			if ("Nimbus".equals(info.getName()))
				{UIManager.setLookAndFeel(info.getClassName()); break;}
		
		Startup startup = new Startup("Reader server", null, false, false, false, true);
		
		new ServerApp(startup);
	}
}
