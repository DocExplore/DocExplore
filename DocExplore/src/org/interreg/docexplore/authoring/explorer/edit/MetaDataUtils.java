package org.interreg.docexplore.authoring.explorer.edit;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.authoring.AuthoringToolFrame;
import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.FileImageSource;

public class MetaDataUtils
{
	public static MetaData importFile(AuthoringToolFrame tool, Region region, File file) throws Exception
	{
		MetaData md = null;
		boolean handledByPlugin = false;
		for (MetaDataPlugin plugin : tool.plugins)
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
	
	public static List<MetaData> importFiles(AuthoringToolFrame tool, Region region, File [] files) throws Exception
	{
		List<MetaData> annotations = new LinkedList<MetaData>();
		for (File file : files)
			if (!file.isDirectory())
		{
			MetaData md = importFile(tool, region, file);
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
