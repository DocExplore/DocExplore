/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.file;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import org.interreg.docexplore.authoring.explorer.Explorer;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class RootView extends ExplorerView
{

	public RootView(Explorer explorer)
	{
		super(explorer);
		
		msg = Lang.s("helpLocalMsg");
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
