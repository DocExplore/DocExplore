package org.interreg.docexplore.reader;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.reader.net.ReaderServer;

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
