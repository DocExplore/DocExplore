package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.interreg.docexplore.datalink.objects.BookData;
import org.interreg.docexplore.datalink.objects.PageData;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.StringUtils;
import org.interreg.docexplore.util.search.FuzzySearch;

public class SearchFS2
{
	public static double search(File root, File dir, int keyId, String value) throws IOException
	{
		double score = 0;
		String indexFile = StringUtils.readFile(new File(dir, "index.xml"));
		List<String> mdsContent = StringUtils.getTagsContent(indexFile, "MetaData");
		for (String mdContent : mdsContent)
		{
			int mdId = Integer.parseInt(mdContent.trim());
			if ((keyId >= 0 && MetaDataFS2.getKeyId(root, mdId) != keyId) || !MetaDataFS2.getType(root, mdId).equals("txt"))
				continue;
			int dist = Math.min(value.length()-1, 3);
			double val = FuzzySearch.getScore(value, new String(ByteUtils.readStream(MetaDataFS2.getMetaDataValue(root, mdId))), dist);
			if (val > score)
				score = val;
		}
		return score;
	}
	
	public static Map<String, Double> searchBooks(File root, Collection<String> subset, int keyId, String value, double score) throws IOException
	{
		List<Integer> searchSet;
		if (subset == null)
			searchSet = BookFS2.getAllBookIds(root);
		else
		{
			searchSet = new Vector<Integer>(subset.size());
			for (String bookId : subset)
				searchSet.add(Integer.parseInt(bookId));
		}
		
		Map<String, Double> res = new TreeMap<String, Double>();
		for (int bookId : searchSet)
		{
			double val = search(root, BookFS2.getBookDir(root, bookId), keyId, value);
			if (val >= score)
				res.put(""+bookId, val);
		}
		return res;
	}
	
	public static Map<String, Double> searchPages(File root, Collection<String> subset, int keyId, String value, double score) throws IOException
	{
		if (subset == null)
		{
			subset = new LinkedList<String>();
			List<Integer> bookIds = BookFS2.getAllBookIds(root);
			for (int bookId : bookIds)
			{
				BookData bookData = BookFS2.getBookData(root, bookId);
				for (int pageNum : bookData.pageNumbers)
					subset.add(bookId+"/p"+pageNum);
			}
		}
		
		Map<String, Double> res = new TreeMap<String, Double>();
		for (String pageUrl : subset)
		{
			double val = search(root, getPageDir(root, pageUrl), keyId, value);
			if (val >= score)
				res.put(pageUrl, val);
		}
		return res;
	}
	
	public static Map<String, Double> searchRegions(File root, Collection<String> subset, int keyId, String value, double score) throws IOException
	{
		if (subset == null)
		{
			subset = new LinkedList<String>();
			List<Integer> bookIds = BookFS2.getAllBookIds(root);
			for (int bookId : bookIds)
			{
				BookData bookData = BookFS2.getBookData(root, bookId);
				for (int pageNum : bookData.pageNumbers)
				{
					PageData pageData = PageFS2.getPageData(root, bookId, pageNum);
					for (int regionId : pageData.regionIds)
						subset.add(bookId+"/p"+pageNum+"/"+regionId);
				}
			}
		}
		
		Map<String, Double> res = new TreeMap<String, Double>();
		for (String pageUrl : subset)
		{
			double val = search(root, getRegionDir(root, pageUrl), keyId, value);
			if (val >= score)
				res.put(pageUrl, val);
		}
		return res;
	}
	
	public static File getPageDir(File root, String url)
	{
		String [] parts = url.split("/");
		int bookId = Integer.parseInt(parts[0]);
		int pageNum = Integer.parseInt(parts[1].substring(1));
		return PageFS2.getPageDir(root, bookId, pageNum);
	}
	public static File getRegionDir(File root, String url)
	{
		String [] parts = url.split("/");
		int bookId = Integer.parseInt(parts[0]);
		int pageNum = Integer.parseInt(parts[1].substring(1));
		int regionId = Integer.parseInt(parts[2]);
		return RegionFS2.getRegionDir(root, bookId, pageNum, regionId);
	}
}
