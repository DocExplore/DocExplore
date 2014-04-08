/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
