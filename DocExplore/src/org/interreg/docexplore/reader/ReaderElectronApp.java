package org.interreg.docexplore.reader;

import java.io.File;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;

public class ReaderElectronApp extends DocExploreTool
{
	public static void main(String [] args)
	{
		File readerDir = new File(DocExploreTool.getHomeDir(), "reader");
		
		String os = System.getProperty("os.name");
		if (os.toLowerCase().contains("win"))
		{
			File appDir = new File(DocExploreTool.getExecutableDir(), "readerapp");
			if (!appDir.exists())
				appDir = new File("C:\\Users\\aburn\\Documents\\dev\\workspace\\DocExploreReader");
			try
			{
				Runtime.getRuntime().exec(new String [] {
					appDir.getAbsolutePath()+"/electron.exe", 
					readerDir.getAbsolutePath(), "true",
				},
				null,
				appDir);
			}
			catch (Exception e)
			{
				ErrorHandler.defaultHandler.submit(e);
			}
		}
		else if (os.toLowerCase().contains("mac"))
		{
			try
			{
				Runtime.getRuntime().exec(new String [] {
					"/Applications/DocExplore.app/Contents/MacOS/Electron",  
					readerDir.getAbsolutePath(), "true",
				},
				null,
				null);
			}
			catch (Exception e)
			{
				ErrorHandler.defaultHandler.submit(e);
			}
		}
		System.exit(0);
	}
}
