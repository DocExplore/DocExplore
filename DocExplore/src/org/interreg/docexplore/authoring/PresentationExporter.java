/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.authoring.explorer.edit.CoverManager;
import org.interreg.docexplore.authoring.explorer.edit.Style;
import org.interreg.docexplore.authoring.explorer.edit.StyleManager;
import org.interreg.docexplore.authoring.explorer.edit.TextElement;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.TileConfiguration;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

public abstract class PresentationExporter
{
	ManuscriptAppHost host;
	StyleManager styles;
	
	public PresentationExporter(ManuscriptAppHost host, StyleManager styles)
	{
		this.host = host;
		this.styles = styles;
	}
	
	float [] progress = {0};
	
	protected void doExport(Book book, File exportDir, ExportOptions options, int bookNum, String format, int exportType) throws Exception
	{
		progress[0] = 0;
		try
		{
			String bookFileName = "book"+bookNum+".xml";
			File bookDir = new File(exportDir, "book"+bookNum);
			bookDir.mkdir();
			
			boolean transparency = options.transparencyBox.isSelected();
			if (transparency)
				format = "PNG";
			boolean [] transArray = transparency ? transArray(book) : null;
			
			boolean poster = PosterUtils.isPoster(book);
			if (poster && !PosterUtils.isUpToDate(book))
				PosterUtils.buildConfiguration(book, host.getLink(), host.frame);
			
			StringBuffer bookSpec = new StringBuffer("<Book path=\"").append("book").append(bookNum).append("/\" aspectRatio=\"").append(aspectRatio(book)).append("\"");
			
			BufferedImage preview = null;
			List<MetaData> previewList = book.getMetaDataListForKey(host.getLink().previewKey);
			if (!previewList.isEmpty())
				preview = previewList.get(0).getImage();
			else if (!poster) 
				preview = CoverManager.buildPreviewCoverImage(host.getLink(), book);
			if (preview != null)
			{
				ImageUtils.write(preview, format, new File(bookDir, "preview."+format));
				bookSpec.append(" preview=\"preview."+format+"\"");
			}
			
			if (!poster)
			{
				BufferedImage cover = CoverManager.buildCoverImage(host.getLink(), book, format.toLowerCase().equals("png"));
				if (cover != null)
				{
					ImageUtils.write(cover, format, new File(bookDir, "cover."+format));
					bookSpec.append(" cover=\"cover."+format+"\"");
					cover = null;
				}
				BufferedImage innerCover = CoverManager.buildInnerCoverImage(host.getLink(), book, format.toLowerCase().equals("png"));
				if (innerCover != null)
				{
					ImageUtils.write(innerCover, format, new File(bookDir, "innerCover."+format));
					bookSpec.append(" innerCover=\"innerCover."+format+"\"");
					innerCover = null;
				}
				bookSpec.append(" leftSide=\"leftSide."+format+"\" rightSide=\"rightSide."+format+"\"");
			}
			else bookSpec.append(" poster=\"true\"");
			bookSpec.append(">\n");
			
			bookSpec.append("\t<Name>");
			bookSpec.append(book.getName());
			bookSpec.append("</Name>\n");
			
			bookSpec.append("\t<Description>");
			MetaDataKey descKey = book.getLink().getOrCreateKey("Description");
			List<MetaData> descMds = book.getMetaDataListForKey(descKey);
			if (descMds.size() > 0)
				bookSpec.append(descMds.get(0).getString());
			bookSpec.append("</Description>\n");
			
			BufferedImage sideLeft = null, sideRight = null;
			int pageNum = 0;
			int lastPage = book.getLastPageNumber();
			int imageNum = 0;
			for (int i=1;i<=lastPage;i++)
			{
				Page page = book.getPage(i);
				TileConfiguration config = null;
				int iw = -1, ih = -1;
				if (!poster)
				{
					BufferedImage image = transparency ? buildTransImage(book, i, transArray) : fix(page.getImage().getImage());
					BufferedImage timage = transparency ? page.getImage().getImage() : null;
					iw = image.getWidth();
					ih = image.getHeight();
					
					if (sideLeft == null)
					{
						sideLeft = new BufferedImage(lastPage/2, image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
						sideRight = new BufferedImage(lastPage/2, image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
					}
					if (i%2 == 0)
						sideLeft.createGraphics().drawImage(image, (i-1)/2, 0, (i-1)/2+1, sideLeft.getHeight(), 0, 0, 1, image.getHeight(), null);
					else sideRight.createGraphics().drawImage(image, (i-1)/2, 0, (i-1)/2+1, sideRight.getHeight(), image.getWidth()-1, 0, image.getWidth(), image.getHeight(), null);
					
					ImageUtils.write(options.handlePage(image), format, new File(bookDir, "image"+pageNum+"."+format));
					if (transparency)
						ImageUtils.write(options.handlePage(timage), format, new File(bookDir, "timage"+pageNum+"."+format));
					page.unloadImage();
				}
				else
				{
					List<MetaData> configs = page.getBook().getMetaDataListForKey(host.getLink().tileConfigKey);
					config = configs.size() > 0 ? (TileConfiguration)new ObjectInputStream(configs.get(0).getValue()).readObject() : null;
					iw = config.getFullWidth();
					ih = config.getFullHeight();
					MetaData [][][] parts = config.getTiles(host.getLink());
					
					BufferedImage image = fix(host.getLink().getMetaData(config.getTileId(config.getLastLayer(), 0, 0)).getImage());
					ImageUtils.write(options.handlePage(image), format, new File(bookDir, "image"+pageNum+"."+format));
					
					for (int l=0;l<=config.getLastLayer();l++)
					{
						int hlayers = config.getHorizontalTiles(l);
						int vlayers = config.getVerticalTiles(l);
						for (int y=0;y<vlayers;y++)
							for (int x=0;x<hlayers;x++)
						{
							if (parts[l][x][y] != null && parts[l][x][y].getType().equals(MetaData.imageType))
								image = fix(parts[l][x][y].getImage());
							else image = new BufferedImage(config.tilew(l, x), config.tileh(l, y), BufferedImage.TYPE_3BYTE_BGR);
							//System.out.println(config.tilew(l, y)+" "+image.getWidth());
							ImageUtils.write(options.handlePage(image.getSubimage(0, 0, config.tilew(l, x), config.tileh(l, y))), format, new File(bookDir, "tile"+pageNum+"_"+l+"_"+x+"_"+y+"."+format));
						}
						progress[0] = l*1.f/config.getLastLayer();
					}
				}
				
				bookSpec.append("\t<Page src=\"").append("image").append(pageNum).append(".").append(format).append("\"");
				if (transparency)
					bookSpec.append(" tsrc=\"").append("timage").append(pageNum).append(".").append(format).append("\"");
				if (poster)
					bookSpec.append(" tiled=\"true\"");
				bookSpec.append(">\n");
				
				if (config != null)
				{
					for (int l=0;l<=config.getLastLayer();l++)
					{
						int hlayers = config.getHorizontalTiles(l);
						int vlayers = config.getVerticalTiles(l);
						
						bookSpec.append("\t\t<Layer w=\"");
						bookSpec.append(config.layerFullw(l));
						bookSpec.append("\" h=\"");
						bookSpec.append(config.layerFullh(l));
						bookSpec.append("\" hlayers=\"");
						bookSpec.append(hlayers);
						bookSpec.append("\" vlayers=\"");
						bookSpec.append(vlayers);
						bookSpec.append("\">\n");
						
						for (int y=0;y<vlayers;y++)
							for (int x=0;x<hlayers;x++)
						{
							bookSpec.append("\t\t\t<Tile w=\"");
							bookSpec.append(config.tileFullw(l, x));
							bookSpec.append("\" h=\"");
							bookSpec.append(config.tileFullh(l, y));
							bookSpec.append("\" src=\"");
							bookSpec.append("tile"+pageNum+"_"+l+"_"+x+"_"+y+"."+format);
							bookSpec.append("\"/>\n");
						}
						
						bookSpec.append("\t\t</Layer>\n");
					}
				}
				
				for (Region region : page.getRegions())
				{
					{
						int nMds = 0;
						for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
							nMds += entry.getValue().size();
						if (nMds == 0)
							continue;
					}
					
					bookSpec.append("\t\t<RegionOfInterest region=\"");
					Point [] outline = region.getOutline();
					boolean first = true;
					for (Point point : outline)
					{
						if (!first)
							bookSpec.append(", ");
						bookSpec.append(point.x*1f/iw).append(", ").append(point.y*1f/ih);
						first = false;
					}
					bookSpec.append("\">\n");
					
					Map<Integer, MetaData> mds = new TreeMap<Integer, MetaData>();
					for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
						for (MetaData md : entry.getValue())
							mds.put(BookImporter.getRank(md), md);
					for (MetaData md : mds.values())
					{
						if (md.getType().equals(MetaData.textType))
						{
							bookSpec.append("\t\t\t<Info type=\"text\">\n");
							Style style = TextElement.getStyle(md, styles);
							bookSpec.append("\t\t\t\t").append(StringUtils.escapeXmlChars(style.apply(md.getString()))).append("\n");
							bookSpec.append("\t\t\t</Info>\n");
						}
						else if (md.getType().equals(MetaData.imageType))
						{
							BufferedImage roiImage = md.getImage();
							ImageUtils.write(fix(roiImage), format, new File(bookDir, "roiImage"+imageNum+"."+format));
							
							bookSpec.append("\t\t\t<Info type=\"image\" src=\"").append("roiImage").append(imageNum).append(".").append(format).append("\" />\n");
							imageNum++;
						}
						else
						{
							MetaDataPlugin plugin = host.plugins.getPluginForMetaDataType(md.getType());
							if (plugin != null)
								plugin.exportMetaData(md, bookSpec, bookDir, imageNum++, options, exportType);
						}
					}
					
					bookSpec.append("\t\t</RegionOfInterest>\n");
					region.unloadMetaData();
				}
				bookSpec.append("\t</Page>\n");
				
				page.unloadRegions();
				pageNum++;
				
				progress[0] = i*1.f/lastPage;
			}
			bookSpec.append("</Book>\n");
			
			FileOutputStream bookOutput = new FileOutputStream(new File(exportDir, bookFileName));
			bookOutput.write(bookSpec.toString().getBytes(Charset.forName("UTF-8")));
			bookOutput.close();
			
			if (!poster)
			{
				ImageUtils.write(options.handlePage(sideLeft), format, new File(bookDir, "leftSide."+format));
				ImageUtils.write(options.handlePage(sideRight), format, new File(bookDir, "rightSide."+format));
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	String aspectRatio(Book book) throws Exception
	{
		Page page = book.getPage(1);
		String dim = page.getMetaDataString(host.getLink().dimKey);
		float ar = 0;
		if (dim != null)
		{
			String [] dims = dim.split(",");
			ar = Integer.parseInt(dims[0])*1f/Integer.parseInt(dims[1]);
		}
		else
		{
			BufferedImage image = page.getImage().getImage();
			page.unloadImage();
			ar = image.getWidth()*1f/image.getHeight();
		}
		return String.format(Locale.ENGLISH, "%.2f", ar);
	}
	
	BufferedImage fix(BufferedImage image)
	{
		if (image.getType() == BufferedImage.TYPE_CUSTOM)
		{
			BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			tmp.createGraphics().drawImage(image, null, 0, 0);
			image = tmp;
		}
		return image;
	}
	
	BufferedImage buildTransImage(Book book, int page, boolean [] trans) throws Exception
	{
		BufferedImage last = book.getPage(page).getImage().getImage();
		
		boolean left = page%2 == 0;
		int lastPage = book.getLastPageNumber();
		int from = page-1;
		if (left) while (from > 1 && trans[from])
			from -= 2;
		else while (from < lastPage-1 && trans[from])
			from += 2;
		if (from == page-1)
			return last;
		BufferedImage first = book.getPage(from+1).getImage().getImage();
		book.getPage(from+1).unloadImage();
		
		BufferedImage res = new BufferedImage(last.getWidth(), last.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int i=0;i<res.getWidth();i++)
			for (int j=0;j<res.getHeight();j++)
				res.setRGB(i, j, first.getRGB(i*first.getWidth()/res.getWidth(), j*first.getHeight()/res.getHeight()));
		
		Graphics2D g = res.createGraphics();
		for (int i=from+(left ? 2 : -2);left && i<page-1 || !left && i>page-1;i+=(left ? 2 : -2))
		{
			g.drawImage(book.getPage(i+1).getImage().getImage(), 0, 0, res.getWidth(), res.getHeight(), null);
			book.getPage(i+1).unloadImage();
		}
		g.drawImage(last, 0, 0, res.getWidth(), res.getHeight(), null);
		book.getPage(page).unloadImage();
		
		return res;
	}
	
	boolean hasTransparency(BufferedImage image)
	{
		for (int i=0;i<image.getWidth();i++)
			for (int j=0;j<image.getHeight();j++)
				if (ImageUtils.alpha(image.getRGB(i, j)) < 255)
					return true;
		return false;
	}
	boolean [] transArray(Book book) throws Exception
	{
		int lastPage = book.getLastPageNumber();
		boolean [] array = new boolean [lastPage];
		for (int i=1;i<=lastPage;i++)
		{
			Page page = book.getPage(i);
			array[i-1] = hasTransparency(page.getImage().getImage());
			page.unloadImage();
		}
		return array;
	}
}
