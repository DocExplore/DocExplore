/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.FileImageSource;

public class MetaDataUtils
{
	public static MetaData importFile(List<MetaDataPlugin> plugins, Region region, File file) throws Exception
	{
		MetaData md = null;
		boolean handledByPlugin = false;
		for (MetaDataPlugin plugin : plugins)
			if (plugin.canPreview(file))
		{
			handledByPlugin = true;
			md = new MetaData(region.getLink(), BookImporter.getDisplayKey(region.getLink()), plugin.getType(), new FileInputStream(file));
			break;
		}
		if (!handledByPlugin)
		{
			FileImageSource image = new FileImageSource(file);
			if (!image.isValid())
				return null;
			md = new MetaData(region.getLink(), BookImporter.getDisplayKey(region.getLink()), MetaData.imageType, image.getFile());
		}
		md.addMetaData(new MetaData(region.getLink(), region.getLink().getOrCreateKey("source-uri"), file.getAbsolutePath()));
		
		region.addMetaData(md);
		BookImporter.setRank(md, BookImporter.getHighestRank(region)+1);
		return md;
	}
	
	public static List<MetaData> importFiles(List<MetaDataPlugin> plugins, Region region, File [] files) throws Exception
	{
		List<MetaData> annotations = new LinkedList<MetaData>();
		for (File file : files)
			if (!file.isDirectory())
		{
			MetaData md = importFile(plugins, region, file);
			if (md == null)
				continue;
			annotations.add(md);
		}
		return annotations;
	}
	
//	public static MetaData copyMetaData(Region region, MetaData annotation) throws Exception
//	{
//		MetaData copy = new MetaData(region.getLink(), annotation.getKey(), annotation.getType(), annotation.getValue());
//		if (annotation.getType().equals(MetaData.textType))
//		{
//			MetaDataKey key = region.getLink().getOrCreateKey("style", "");
//			List<MetaData> mds = annotation.getMetaDataListForKey(key);
//			if (mds != null && mds.size() > 0)
//				copy.addMetaData(new MetaData(copy.getLink(), key, mds.get(0).getString()));
//		}
//		
//		region.addMetaData(copy);
//		BookImporter.setRank(copy, BookImporter.getHighestRank(region)+1);
//		return copy;
//	}
//	
//	public static List<MetaData> copyMetaData(Region region, Region from) throws Exception
//	{
//		Map<Integer, MetaData> mds = new TreeMap<Integer, MetaData>();
//		for (Map.Entry<MetaDataKey, List<MetaData>> entry : from.getMetaData().entrySet())
//			for (MetaData md : entry.getValue())
//				mds.put(BookImporter.getRank(md), md);
//		
//		List<MetaData> res = new LinkedList<MetaData>();
//		for (MetaData annotation : mds.values())
//			res.add(copyMetaData(region, annotation));
//		
//		return res;
//	}
}
