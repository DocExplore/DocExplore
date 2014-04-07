package org.interreg.docexplore.manuscript.actions;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public class AddRegionsAction extends UnreversibleAction
{
	public Page page;
	public List<Point []> outlines;
	
	public AddRegionsAction(Page page, Point [] outline)
	{
		this.page = page;
		this.outlines = Collections.singletonList(outline);
	}
	public AddRegionsAction(Page page, List<Point []> outlines)
	{
		this.page = page;
		this.outlines = outlines;
	}
	
	public List<Region> regions = new LinkedList<Region>();
	public void doAction() throws Exception
	{
		regions = new LinkedList<Region>();
		for (Point [] outline : outlines)
		{
			Region region = page.addRegion();
			region.setOutline(outline);
			regions.add(region);
		}
	}

	public String description()
	{
		return XMLResourceBundle.getBundledString("addRegion");
	}

}
