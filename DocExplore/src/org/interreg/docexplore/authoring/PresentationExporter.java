package org.interreg.docexplore.authoring;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.interreg.docexplore.authoring.explorer.edit.CoverManager;
import org.interreg.docexplore.authoring.explorer.edit.Style;
import org.interreg.docexplore.authoring.explorer.edit.TextElement;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

public abstract class PresentationExporter
{
	AuthoringToolFrame tool;
	
	
	public PresentationExporter(AuthoringToolFrame tool)
	{
		this.tool = tool;
	}
	
	float [] progress = {0};
	
	protected void doExport(Book book, File exportDir, ExportOptions options, int bookNum) throws Exception
	{
		progress[0] = 0;
		try
		{
			String bookFileName = "book"+bookNum+".xml";
			File bookDir = new File(exportDir, "book"+bookNum);
			bookDir.mkdir();
			
			StringBuffer bookSpec = new StringBuffer("<Book path=\"").append("book").append(bookNum).append("/\" aspectRatio=\"").append(aspectRatio(book)).append("\"");
			BufferedImage cover = CoverManager.buildCoverImage(book);
			if (cover != null)
			{
				ImageUtils.write(cover, "PNG", new File(bookDir, "cover.png"));
				bookSpec.append(" cover=\"cover.png\"");
				cover = null;
				
				BufferedImage preview = CoverManager.buildPreviewCoverImage(book);
				if (preview != null)
					ImageUtils.write(preview, "PNG", new File(exportDir, "book"+bookNum+".png"));
				preview = null;
			}
			BufferedImage innerCover = CoverManager.buildInnerCoverImage(book);
			if (innerCover != null)
			{
				ImageUtils.write(innerCover, "PNG", new File(bookDir, "innerCover.png"));
				bookSpec.append(" innerCover=\"innerCover.png\"");
				innerCover = null;
			}
			bookSpec.append(">\n");
			
			int pageNum = 0;
			String format = "png";
			int lastPage = book.getLastPageNumber();
			int imageNum = 0;
			for (int i=1;i<=lastPage;i++)
			{
				Page page = book.getPage(i);
				BufferedImage image = page.getImage().getImage();
				ImageUtils.write(options.handlePage(image), format, new File(bookDir, "image"+pageNum+"."+format));
				page.unloadImage();
				
				bookSpec.append("\t<Page src=\"").append("image").append(pageNum).append(".").append(format).append("\">\n");
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
						bookSpec.append(point.x*1f/image.getWidth()).append(", ").append(point.y*1f/image.getHeight());
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
							Style style = TextElement.getStyle(md, tool.styleManager);
							bookSpec.append("\t\t\t\t").append(StringUtils.escapeXmlChars(style.apply(md.getString()))).append("\n");
							bookSpec.append("\t\t\t</Info>\n");
						}
						else if (md.getType().equals(MetaData.imageType))
						{
							BufferedImage roiImage = md.getImage();
							ImageIO.write(roiImage, format, new File(bookDir, "roiImage"+imageNum+"."+format));
							
							bookSpec.append("\t\t\t<Info type=\"image\" src=\"").append("roiImage").append(imageNum).append(".").append(format).append("\" />\n");
							imageNum++;
						}
						else tool.exportMetaData(md, bookSpec, bookDir, imageNum++);
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
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	String aspectRatio(Book book) throws Exception
	{
		Page page = book.getPage(1);
		BufferedImage image = page.getImage().getImage();
		return String.format(Locale.ENGLISH, "%.2f", image.getWidth()*1./image.getHeight());
	}
}
