package org.interreg.docexplore.datalink.fs2;

import java.awt.Point;
import java.util.List;

import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.actions.AddRegionsAction;

public class AddFS2RegionsAction extends AddRegionsAction
{
	DataLinkFS2 link;
	
	public AddFS2RegionsAction(DataLinkFS2 link, Page page, Point [] outline)
	{
		super(page, outline);
		this.link = link;
	}
	public AddFS2RegionsAction(DataLinkFS2 link, Page page, List<Point []> outlines)
	{
		super(page, outlines);
		this.link = link;
	}

	DeleteFS2RegionsAction reverse = null;
	public void doAction() throws Exception
	{
		if (reverse == null)
			super.doAction();
		else reverse.undoAction();
	}

	public void undoAction() throws Exception
	{
		if (reverse == null)
		{
			reverse = new DeleteFS2RegionsAction(link, regions);
			reverse.cacheDir = cacheDir;
		}
		reverse.doAction();
	}
}
