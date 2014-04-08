/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;

public class DeleteFS2RegionsAction extends DeleteRegionsAction
{
	DataLinkFS2 link;
	
	public DeleteFS2RegionsAction(DataLinkFS2 link, Region region)
	{
		super(region);
		this.link = link;
	}
	public DeleteFS2RegionsAction(DataLinkFS2 link, List<Region> regions)
	{
		super(regions);
		this.link = link;
	}

	Map<Region, File> dirs = null;
	public void doAction() throws Exception
	{
		dirs = new HashMap<Region, File>();
		for (Region region : regions)
		{
			Page page = region.getPage();
			File dir = RegionFS2.getRegionDir(link.root, page.getBook().getId(), page.getPageNumber(), region.getId());
			FileUtils.moveDirectoryToDirectory(dir, cacheDir, true);
			page.regions.remove(region.getId());
			dirs.put(region, dir);
		}
	}
	
	public void undoAction() throws Exception
	{
		for (Region region : dirs.keySet())
		{
			File dir = dirs.get(region);
			FileUtils.moveDirectoryToDirectory(new File(cacheDir, dir.getName()), dir.getParentFile(), true);
			region.getPage().regions.put(region.getId(), region);
		}
	}
}
