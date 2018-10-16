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

import java.awt.image.BufferedImage;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataUtils;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.util.MemoryImageSource;
import org.interreg.docexplore.util.history.ReversibleAction;

public class RotateRightAction extends ReversibleAction
{
	DocExploreDataLink link;
	AnnotatedObject document;
	
	public RotateRightAction(DocExploreDataLink link, AnnotatedObject document)
	{
		this.link = link;
		this.document = document;
	}
	
	public void doAction() throws Exception
	{
		if (document instanceof Book)
		{
			rotatePoster(link, (Book)document);
			((Book)document).setMetaDataString(link.upToDateKey, "false");
		}
		else if (document instanceof MetaData)
			rotate((MetaData)document);
		else if (document instanceof Page)
			rotate((Page)document);
	}

	public void undoAction() throws Exception
	{
		if (document instanceof Book)
		{
			RotateLeftAction.rotatePoster(link, (Book)document);
			((Book)document).setMetaDataString(link.upToDateKey, "false");
		}
		else if (document instanceof MetaData)
			RotateLeftAction.rotate((MetaData)document);
		else if (document instanceof Page)
			RotateLeftAction.rotate((Page)document);
	}
	
	public void dispose()
	{
		document = null;
	}

	public String description()
	{
		return document instanceof Book ? Lang.s("rotateParts") : Lang.s("rotateImage");
	}
	
	public static void rotatePoster(DocExploreDataLink link, Book book) throws DataLinkException
	{
		MetaData [][] parts = PosterUtils.getBaseTilesArray(link, book);
		for (int i=0;i<parts.length;i++)
			for (int j=0;j<parts[i].length;j++)
				if (parts[i][j] != null)
					PosterUtils.setPartPos(link, parts[i][j], parts[0].length-1-j, i);
	}
	
	public static void rotate(MetaData md) throws Exception
	{
		BufferedImage image = md.getImage();
		md.setValue(MetaData.imageType, new MemoryImageSource(rotate(image)).getFile());
	    MetaDataUtils.refreshImageMetaData(md);
	}
	public static void rotate(Page page) throws Exception
	{
		BufferedImage image = page.getImage().getImage();
		page.setImage(new MemoryImageSource(rotate(image)));
	    MetaDataUtils.refreshImageMetaData(page);
	}
	public static BufferedImage rotate(BufferedImage image)
	{
		int w = image.getWidth(), h = image.getHeight();
	    BufferedImage dest = new BufferedImage(h, w, image.getType() == 0 ? BufferedImage.TYPE_4BYTE_ABGR : image.getType());
	    for (int y=0;y<h;y++) 
	        for (int x=0;x<w;x++) 
	        	dest.setRGB(h-y-1, x, image.getRGB(x, y));
	    return dest;
	}
}
