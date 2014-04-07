package org.interreg.docexplore.management.manage;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class SelectPagesPanel
{
	public static List<File> show()
	{
		File [] files = DocExploreTool.getFileDialogs().openFiles(DocExploreTool.getImagesCategory(), XMLResourceBundle.getBundledString("manageSelectFilesMsg"));
		
		if (files != null)
		{
			List<File> res = new LinkedList<File>();
			if (files != null)
				for (File child : files)
					if (!child.isDirectory() && !child.isHidden())
						res.add(child);
			return res;
		}
		return null;
	}
}
