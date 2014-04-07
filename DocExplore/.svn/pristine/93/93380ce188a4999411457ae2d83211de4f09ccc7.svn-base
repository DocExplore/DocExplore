package org.interreg.docexplore.manuscript.actions;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.history.ReversibleAction;

public class CropPageAction extends ReversibleAction
{
	Page page;
	int tlx, tly, brx, bry;
	
	public CropPageAction(Page page, int tlx, int tly, int brx, int bry)
	{
		this.page = page;
		this.tlx = tlx;
		this.tly = tly;
		this.brx = brx;
		this.bry = bry;
	}
	
	Map<Integer, Point []> oldOutlines = new TreeMap<Integer, Point []>();
	public void doAction() throws Exception
	{
		BufferedImage image = page.getImage().getImage();
		ImageUtils.write(image, "PNG", new File(cacheDir, "old.png"));
		for (Region region : page.getRegions())
			oldOutlines.put(region.getId(), copyOutline(region.getOutline()));
		
		int nw = brx-tlx, nh = bry-tly;
		BufferedImage nimage = new BufferedImage(nw, nh, BufferedImage.TYPE_3BYTE_BGR);
		nimage.createGraphics().drawImage(image, 0, 0, nw, nh, tlx, tly, brx, bry, null);
		File newFile = new File(cacheDir, "new.png");
		ImageUtils.write(nimage, "PNG", newFile);
		page.setImage(new FileImageSource(newFile));
		page.unloadImage();
		
		for (Region region : page.getRegions())
		{
			Point [] outline = copyOutline(region.getOutline());
			for (int i=0;i<outline.length;i++)
			{
				outline[i].x = Math.max(0, Math.min(nw-1, outline[i].x-tlx));
				outline[i].y = Math.max(0, Math.min(nh-1, outline[i].y-tly));
			}
			region.setOutline(outline);
		}
		
		updateMetaData();
	}
	Point [] copyOutline(Point [] outline)
	{
		Point [] res = new Point [outline.length];
		for (int i=0;i<outline.length;i++)
			res[i] = new Point(outline[i]);
		return res;
	}
	void updateMetaData() throws Exception
	{
		MetaDataKey miniKey = page.getLink().getKey("mini", "");
		List<MetaData> mds = page.getMetaDataListForKey(miniKey);
		if (!mds.isEmpty())
			page.removeMetaData(mds.get(0));
		DocExploreDataLink.getImageMini(page);
		
		MetaDataKey dimKey = page.getLink().getKey("dimension", "");
		mds = page.getMetaDataListForKey(dimKey);
		if (!mds.isEmpty())
			page.removeMetaData(mds.get(0));
		DocExploreDataLink.getImageDimension(page);
		
		page.unloadImage();
	}

	public void undoAction() throws Exception
	{
		page.setImage(new FileImageSource(new File(cacheDir, "old.png")));
		page.unloadImage();
		
		for (Region region : page.getRegions())
		{
			Point [] outline = oldOutlines.get(region.getId());
			if (outline != null)
				region.setOutline(outline);
		}
		
		updateMetaData();
	}

	public String description()
	{
		return XMLResourceBundle.getBundledString("cropPage");
	}

	public void dispose()
	{
		oldOutlines = null;
	}
}
