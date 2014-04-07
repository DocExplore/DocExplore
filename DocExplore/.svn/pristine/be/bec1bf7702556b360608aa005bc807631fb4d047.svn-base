package org.interreg.docexplore.authoring.explorer.file;

import java.io.File;

import javax.swing.JFileChooser;

import org.interreg.docexplore.authoring.AuthoringToolFrame;
import org.interreg.docexplore.authoring.explorer.Explorer;

@SuppressWarnings("serial")
public class FileExplorer extends Explorer
{

	public FileExplorer(AuthoringToolFrame tool) throws Exception
	{
		super(tool);
		
		addView(new RootView(this));
		addView(new FolderView(this));
		
		explore(new JFileChooser().getCurrentDirectory().getAbsolutePath());
	}

	public String getParentPath(String path)
	{
		if (path.equals(""))
			return path;
		return new File(path).getParent();
	}
}
