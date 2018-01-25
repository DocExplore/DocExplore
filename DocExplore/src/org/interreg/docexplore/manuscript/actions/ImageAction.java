/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.actions;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.history.ReversibleAction;

public abstract class ImageAction extends ReversibleAction
{
	AnnotatedObject object;
	
	public ImageAction(AnnotatedObject object)
	{
		this.object = object;
	}
	
	private BufferedImage getImage() throws Exception
	{
		return object instanceof Page ? ((Page)object).getImage().getImage() : ((MetaData)object).getImage();
	}
	
	protected abstract BufferedImage doImageAction(BufferedImage image);
	protected void doRegionAction(Page page, BufferedImage newImage) throws DataLinkException {}
	
	Map<Integer, Point []> oldOutlines = new TreeMap<Integer, Point []>();
	public void doAction() throws Exception
	{
		BufferedImage image = getImage();
		ImageUtils.write(image, "PNG", new File(cacheDir, "old.png"));
		
		if (object instanceof Page)
		{
			for (Region region : ((Page)object).getRegions())
				oldOutlines.put(region.getId(), copyOutline(region.getOutline()));
		}
		
		BufferedImage nimage = doImageAction(image);
		File newFile = new File(cacheDir, "new.png");
		ImageUtils.write(nimage, "PNG", newFile);
		if (object instanceof Page)
		{
			Page page = (Page)object;
			page.setImage(new FileImageSource(newFile));
			page.unloadImage();
			doRegionAction(page, nimage);
		}
		else ((MetaData)object).setValue(MetaData.imageType, new FileInputStream(newFile));
		
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
		MetaDataKey miniKey = object.getLink().getKey("mini", "");
		List<MetaData> mds = object.getMetaDataListForKey(miniKey);
		if (!mds.isEmpty())
			object.removeMetaData(mds.get(0));
		DocExploreDataLink.getImageMini(object);
		
		MetaDataKey dimKey = object.getLink().getKey("dimension", "");
		mds = object.getMetaDataListForKey(dimKey);
		if (!mds.isEmpty())
			object.removeMetaData(mds.get(0));
		DocExploreDataLink.getImageDimension(object);
		
		if (object instanceof Page)
			((Page)object).unloadImage();
	}

	public void undoAction() throws Exception
	{
		if (object instanceof Page)
		{
			Page page = (Page)object;
			page.setImage(new FileImageSource(new File(cacheDir, "old.png")));
			page.unloadImage();
			
			for (Region region : page.getRegions())
			{
				Point [] outline = oldOutlines.get(region.getId());
				if (outline != null)
					region.setOutline(outline);
			}
		}
		else ((MetaData)object).setValue(MetaData.imageType, new FileInputStream(new File(cacheDir, "old.png")));
		
		updateMetaData();
	}

	public String description()
	{
		return Lang.s("cropPage");
	}

	public void dispose()
	{
		oldOutlines = null;
	}
}
