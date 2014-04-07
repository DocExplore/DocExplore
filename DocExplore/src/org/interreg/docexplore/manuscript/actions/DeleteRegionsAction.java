package org.interreg.docexplore.manuscript.actions;

import java.util.Collections;
import java.util.List;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Region;

public class DeleteRegionsAction extends UnreversibleAction
{
	public List<Region> regions;
	
	public DeleteRegionsAction(Region region)
	{
		this.regions = Collections.singletonList(region);
	}
	public DeleteRegionsAction(List<Region> regions)
	{
		this.regions = regions;
	}
	
	public void doAction() throws Exception
	{
		for (Region region : regions)
			region.getPage().removeRegion(region);
	}

	public String description()
	{
		return XMLResourceBundle.getBundledString("deleteRegion");
	}

}
