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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.util.GuiUtils;

public class PosterUtils
{
	public static boolean isPoster(Book book) throws DataLinkException
	{
		String display = book.getMetaDataString(book.getLink().getOrCreateKey("display", ""));
		return display != null && display.equals("poster");
	}
	public static boolean isInStitches(Book book) throws DataLinkException
	{
		List<MetaData> mds = book.getMetaDataListForKey(book.getLink().getOrCreateKey("stitch", ""));
		return !mds.isEmpty();
	}
	public static boolean isInRendering(Book book) throws DataLinkException
	{
		List<MetaData> mds = book.getMetaDataListForKey(book.getLink().getOrCreateKey("stitch-render", ""));
		return !mds.isEmpty() && mds.get(0).getString().equals("true");
	}
	public static boolean isInEditing(Book book) throws DataLinkException
	{
		List<MetaData> mds = book.getMetaDataListForKey(book.getLink().getOrCreateKey("stitch-edit", ""));
		return !mds.isEmpty() && !mds.get(0).getString().equals("");
	}
	public static boolean isUpToDate(Book book) throws DataLinkException
	{
		List<MetaData> mds = book.getMetaDataListForKey(book.getLink().getOrCreateKey("up-to-date", ""));
		return !mds.isEmpty() && mds.get(0).getString().equals("true");
	}
	
	public static void assignPartLocations(DocExploreDataLink link, Book book) throws DataLinkException
	{
		List<MetaData> parts = book.getMetaDataListForKey(link.partKey);
		Map<MetaData, int []> ints = new HashMap<MetaData, int []>();
		int min = -1;
		for (MetaData part : parts)
		{
			int [] vs = extractInts(part.getMetaDataString(link.sourceKey));
			if (min < 0 || vs.length < min)
				min = vs.length;
			ints.put(part, vs);
		}
		int xcol = min > 1 ? min-2 : min > 0 ? 0 : -1;
		int ycol = xcol < 0 ? -1 : min > xcol+1 ? xcol+1 : -1;
		
		Map<Integer, Integer> yValues = new TreeMap<Integer, Integer>();
		for (MetaData part : parts)
		{
			int [] vs = ints.get(part);
			int y = ycol < 0 ? 0 : vs[ycol];
			yValues.put(y, 0);
		}
		int ycnt = 0;
		for (Map.Entry<Integer, Integer> e : yValues.entrySet())
			e.setValue(ycnt++);
		
		@SuppressWarnings("unchecked")
		Map<Integer, MetaData> [] partsByX = new TreeMap [ycnt];
		for (MetaData part : parts)
		{
			int [] vs = ints.get(part);
			int x = xcol < 0 ? 0 : vs[xcol];
			int y = ycol < 0 ? 0 : vs[ycol];
			Map<Integer, MetaData> row = partsByX[yValues.get(y)];
			if (row == null)
				partsByX[yValues.get(y)] = row = new TreeMap<Integer, MetaData>();
			while (row.containsKey(x))
				x++;
			row.put(x, part);
		}
		
		for (int y=0;y<partsByX.length;y++)
		{
			int x = 0;
			for (Map.Entry<Integer, MetaData> row : partsByX[y].entrySet())
			{
				setPartPos(link, row.getValue(), x, y);
				x++;
			}
		}
		
	}
	
	public static int [] getPartPos(DocExploreDataLink link, MetaData part) throws DataLinkException
	{
		List<MetaData> mds = part.getMetaDataListForKey(link.partPosKey);
		if (mds.isEmpty())
			return null;
		String [] toks = mds.get(0).getString().split(",");
		return new int [] {Integer.parseInt(toks[0]), Integer.parseInt(toks[1])};
	}
	
	public static void setPartPos(DocExploreDataLink link, MetaData part, int i, int j) throws DataLinkException
	{
		List<MetaData> mds = part.getMetaDataListForKey(link.partPosKey);
		MetaData pos = null;
		if (!mds.isEmpty())
			pos = mds.get(0);
		else
		{
			pos = new MetaData(link, link.partPosKey, "");
			part.addMetaData(pos);
		}
		pos.setString(i+","+j);
	}
	
	static int [] extractInts(String s)
	{
		int [] res = new int [0];
		String curDigit = "";
		for (int i=0;i<s.length();i++)
		{
			char c = s.charAt(i);
			if (Character.isDigit(c))
				curDigit += c;
			else if (curDigit.length() > 0)
			{
				res = Arrays.copyOf(res, res.length+1);
				res[res.length-1] = Integer.parseInt(curDigit);
				curDigit = "";
			}
		}
		if (curDigit.length() > 0)
		{
			res = Arrays.copyOf(res, res.length+1);
			res[res.length-1] = Integer.parseInt(curDigit);
		}
		return res;
	}
	
	public static List<MetaData> getUpperTiles(DocExploreDataLink link, Book book) throws DataLinkException
	{
		List<MetaData> tiles = new LinkedList<MetaData>();
		List<MetaData> allTiles = book.getMetaDataListForKey(link.partKey);
		for (MetaData tile : allTiles)
			if (tile.getMetaDataString(link.partPosKey) == null)
				tiles.add(tile);
		return tiles;
	}
	
	public static MetaData [][] getBaseTilesArray(DocExploreDataLink link, Book book) throws DataLinkException
	{
		int w = 0, h = 0;
		for (MetaData md : book.getMetaDataListForKey(link.partKey))
		{
			String posmd = md.getMetaDataString(link.partPosKey);
			if (posmd == null || posmd.equals(""))
				continue;
			String [] pos = md.getMetaDataString(link.partPosKey).split(",");
			int x = Integer.parseInt(pos[0]);
			int y = Integer.parseInt(pos[1]);
			if (x+1 > w) w = x+1;
			if (y+1 > h) h = y+1;
		}
		MetaData [][] res = new MetaData [w][h];
		for (MetaData md : book.getMetaDataListForKey(link.partKey))
		{
			String posmd = md.getMetaDataString(link.partPosKey);
			if (posmd == null || posmd.equals(""))
				continue;
			String [] pos = posmd.split(",");
			res[Integer.parseInt(pos[0])][Integer.parseInt(pos[1])] = md;
		}
		return res;
	}
	
	public static boolean posterHasHoles(DocExploreDataLink link, Book book) throws DataLinkException
	{
		MetaData [][] parts = getBaseTilesArray(link, book);
		for (int i=0;i<parts.length;i++)
			for (int j=0;j<parts[i].length;j++)
				if (parts[i][j] == null)
					return true;
		return false;
	}
	
//	public static void transposePoster(DocExploreDataLink link, Book book) throws DataLinkException
//	{
//		MetaData [][] parts = getBaseTilesArray(link, book);
//		for (int i=0;i<parts.length;i++)
//			for (int j=0;j<parts[i].length;j++)
//				if (parts[i][j] != null)
//					setPartPos(link, parts[i][j], j, i);
//	}
	
	public static boolean removeFromRow(DocExploreDataLink link, Book book, int col, int row) throws DataLinkException
	{
		MetaData [][] parts = getBaseTilesArray(link, book);
		MetaData part = parts[col][row];
		if (part == null)
			return false;
		for (int i=col+1;i<parts.length;i++)
			if (parts[i][row] != null)
				PosterUtils.setPartPos(link, parts[i][row], i-1, row);
		part.getMetaDataListForKey(link.partPosKey).get(0).setString("");
		boolean rowIsEmpty = true;
		for (int i=0;i<parts.length;i++)
			if (parts[i][row] != null && parts[i][row] != part)
				{rowIsEmpty = false; break;}
		if (rowIsEmpty)
		{
			for (int i=0;i<parts.length;i++)
				for (int j=row+1;j<parts[0].length;j++)
					if (parts[i][j] != null)
						PosterUtils.setPartPos(link, parts[i][j], i, j-1);
		}
		return rowIsEmpty;
	}
	public static void addToRow(DocExploreDataLink link, Book book, MetaData part, int col, int row, boolean insert) throws DataLinkException
	{
		MetaData [][] parts = getBaseTilesArray(link, book);
		part.getMetaDataListForKey(link.partPosKey).get(0).setString((insert ? 0 : col)+","+row);
		if (insert)
		{
			for (int i=0;i<parts.length;i++)
				for (int j=row;j<parts[0].length;j++)
					if (parts[i][j] != null)
						PosterUtils.setPartPos(link, parts[i][j], i, j+1);
		}
		else
		{
			for (int i=col;i<parts.length;i++)
				if (parts[i][row] != null)
					PosterUtils.setPartPos(link, parts[i][row], i+1, row);
		}
	}
	
	public static void buildConfiguration(Book book, DocExploreDataLink link, Component component)
	{
		GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable() {@Override public void run()
		{
			try
			{
				if (book.getLastPageNumber() > 0)
				{
					TileConfiguration config = new TileConfiguration();
					config.build(link, book, progress);
					
					Page page = book.getPage(1);
					page.setMetaDataString(link.dimKey, config.getFullWidth()+","+config.getFullHeight());
					//DocExploreDataLink.getImageMini(page);
					book.setMetaDataString(link.upToDateKey, "true");
				}
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}
		float [] progress = {0}; @Override public float getProgress() {return progress[0];}}, component);
	}
	
	static final int maxDim = 2*1024;
	public static BufferedImage buildComposite(DocExploreDataLink link, Book book, float [] progress) throws DataLinkException
	{
		MetaData [][] parts = getBaseTilesArray(link, book);
		if (parts.length == 0 || parts[0].length == 0)
			return new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		int fullWidth = 0, fullHeight = 0;
		int [] widths = new int [parts.length];
		int [] heights = new int [parts[0].length];
		for (int i=0;i<widths.length;i++)
		{
			int min = -1;
			for (int j=0;j<parts[0].length;j++)
				if (parts[i][j] != null)
			{
				String [] dims = parts[i][j].getMetaDataString(link.dimKey).split(",");
				int w = Integer.parseInt(dims[0]);
				if (min < 0 || w < min)
					min = w;
			}
			widths[i] = min;
			fullWidth += min;
		}
		for (int j=0;j<heights.length;j++)
		{
			int min = -1;
			for (int i=0;i<parts.length;i++)
				if (parts[i][j] != null)
			{
				String [] dims = parts[i][j].getMetaDataString(link.dimKey).split(",");
				int h = Integer.parseInt(dims[1]);
				if (min < 0 || h < min)
					min = h;
			}
			heights[j] = min;
			fullHeight += min;
		}
		int width, height;
		if (fullWidth > fullHeight)
		{
			width = Math.min(fullWidth, maxDim);
			height = fullHeight*width/fullWidth;
		}
		else
		{
			height = Math.min(fullHeight, maxDim);
			width = fullWidth*height/fullHeight;
		}
		BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = res.createGraphics();
		int x0 = 0;
		for (int i=0;i<widths.length;i++)
		{
			int xt0 = x0*width/fullWidth;
			int x1 = x0+widths[i];
			int xt1 = x1*width/fullWidth;
			int y0 = 0;
			for (int j=0;j<heights.length;j++)
			{
				progress[0] = (i*heights.length+j)*1f/(widths.length*heights.length);
				int yt0 = y0*height/fullHeight;
				int y1 = y0+heights[j];
				int yt1 = y1*height/fullHeight;
				MetaData part = parts[i][j];
				if (part != null)
					g.drawImage(part.getImage(), xt0, yt0, xt1, yt1, 0, 0, widths[i], heights[j], null);
				y0 = y1;
			}
			x0 = x1;
		}
		return res;
	}
}
