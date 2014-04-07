package org.interreg.docexplore.authoring.explorer.file;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import org.interreg.docexplore.authoring.explorer.Explorer;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class RootView extends ExplorerView
{

	public RootView(Explorer explorer)
	{
		super(explorer);
		
		msg = XMLResourceBundle.getBundledString("helpLocalMsg");
	}

	public boolean canHandle(String path) throws Exception
	{
		return path.equals("");
	}

	protected List<ViewItem> buildItemList(String path) throws Exception
	{
		if (!path.equals(""))
			throw new Exception("Invalid filesystem root : "+path);
		List<ViewItem> res = new LinkedList<ViewItem>();
		for (File child : File.listRoots())
			if (child.isDirectory())
				res.add(new ViewItem(child.getAbsolutePath(), "", child));
		return res;
	}

	Icon icon = ImageUtils.getIcon("drive-48x48.png");
	protected Icon getIcon(Object object)
	{
		return icon;
	}

	protected String getPath(Object object)
	{
		try {return ((File)object).getCanonicalPath();}
		catch (Exception e) {return e.getMessage();}
	}

	
}
