package org.interreg.docexplore.datalink.fs2;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.datalink.objects.RegionData;
import org.interreg.docexplore.util.StringUtils;

public class RegionFS2
{
	public static File getRegionDir(File root, int bookId, int pageNum, int regionId)
		{return new File(PageFS2.getPageDir(root, bookId, pageNum), "region"+regionId);}
	
	static List<Point> getPoints(String outline)
	{
		List<Point> res = new LinkedList<Point>();
		String [] elems = outline.split(",");
		if (outline.length() > 1)
			for (int i=0;i<elems.length;i+=2)
				res.add(new Point(Integer.parseInt(elems[i].trim()), Integer.parseInt(elems[i+1].trim())));
		return res;
	}
	static String getOutline(Point [] points)
	{
		StringBuffer sb = new StringBuffer();
		boolean start = true;
		for (Point point : points)
		{
			if (!start)
				sb.append(", ");
			sb.append(point.x).append(",").append(point.y);
			start = false;
		}
		return sb.toString();
	}
	
	public static RegionData getRegionData(File root, int regionId, int bookId, int pageNum) throws IOException
	{
		File regionDir = getRegionDir(root, bookId, pageNum, regionId);
		String data = StringUtils.readFile(new File(regionDir, "index.xml"));
		List<Point> outline = getPoints(StringUtils.getTagContent(data, "Outline"));
		
		return new RegionData(outline, MetaDataFS2.getMetaData(root, regionDir));
	}
	
	public static int addRegion(File root, int pageId, int bookId, int pageNum) throws IOException
	{
		File pageDir = PageFS2.getPageDir(root, bookId, pageNum);
		int id = DataLinkFS2.getNextId(root);
		File regionDir = new File(pageDir, "region"+id);
		regionDir.mkdir();
		StringUtils.writeFile(new File(regionDir, "index.xml"), "<Region>\n\t<Outline></Outline>\n</Region>");
		return id;
	}
	
	public static void removeRegion(File root, int regionId, int bookId, int pageNum)
	{
		File regionDir = getRegionDir(root, bookId, pageNum, regionId);
		FS2Utils.delete(regionDir);
	}
	
	public static void setRegionOutline(File root, int regionId, int bookId, int pageNum, Point [] outline) throws IOException
	{
		File regionDir = getRegionDir(root, bookId, pageNum, regionId);
		File dataFile = new File(regionDir, "index.xml");
		String data = StringUtils.readFile(dataFile);
		data = StringUtils.setTagContent(data, "Outline", getOutline(outline));
		StringUtils.writeFile(dataFile, data);
	}
	
	public static void addMetaData(File root, int bookId, int pageNum, int regionId, int metaDataId) throws IOException
	{
		MetaDataFS2.addMetaDataToObject(getRegionDir(root, bookId, pageNum, regionId), metaDataId);
	}
	public static void removeMetaData(File root, int bookId, int pageNum, int regionId, int metaDataId) throws IOException
	{
		MetaDataFS2.removeMetaDataFromObject(getRegionDir(root, bookId, pageNum, regionId), metaDataId);
	}
}
