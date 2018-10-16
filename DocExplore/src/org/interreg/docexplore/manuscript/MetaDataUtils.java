/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.interreg.docexplore.util.ImageUtils;

public class MetaDataUtils
{
	public static void refreshImageMetaData(AnnotatedObject object) throws Exception
	{
		ManuscriptLink link = object.getLink();
		BufferedImage image = object instanceof Page ? ((Page)object).getImage().getImage() : ((MetaData)object).getImage();
		
		MetaDataKey dimKey = link.getOrCreateKey("dimension", "");
		List<MetaData> dims = object.getMetaDataListForKey(dimKey);
		String dimString = image.getWidth()+","+image.getHeight();
		if (dims.size() > 0)
			dims.get(0).setString(dimString);
		else object.addMetaData(new MetaData(link, dimKey, dimString));
		
		MetaDataKey miniKey = object.getLink().getKey("mini", "");
		List<MetaData> minis = object.getMetaDataListForKey(miniKey);
		BufferedImage miniImage = null;
		miniImage = ImageUtils.createIconSizeImage(object instanceof Page ? ((Page)object).getImage().getImage() : ((MetaData)object).getImage(), DocExploreDataLink.miniSize);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageUtils.write(miniImage, "png", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());
		if (minis.size() > 0)
			minis.get(0).setValue(MetaData.imageType, is);
		else object.addMetaData(new MetaData(link, miniKey, MetaData.imageType, is));
	}
}
