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

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public class CropPageAction extends ImageAction
{
	int tlx, tly, brx, bry;
	
	public CropPageAction(AnnotatedObject object, int tlx, int tly, int brx, int bry)
	{
		super(object);
		
		this.tlx = tlx;
		this.tly = tly;
		this.brx = brx;
		this.bry = bry;
	}
	
	@Override protected BufferedImage doImageAction(BufferedImage image)
	{
		int nw = brx-tlx, nh = bry-tly;
		BufferedImage nimage = new BufferedImage(nw, nh, BufferedImage.TYPE_3BYTE_BGR);
		nimage.createGraphics().drawImage(image, 0, 0, nw, nh, tlx, tly, brx, bry, null);
		return nimage;
	}
	
	@Override protected void doRegionAction(Page page, BufferedImage newImage) throws DataLinkException
	{
		int nw = brx-tlx, nh = bry-tly;
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
	}
	
	public String description()
	{
		return Lang.s("cropPage");
	}
}
