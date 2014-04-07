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
