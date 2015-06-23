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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
	
	protected void doExport(Book book, File exportDir, ExportOptions options, int bookNum, String format) throws Exception
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
				ImageUtils.write(cover, format, new File(bookDir, "cover."+format));
				bookSpec.append(" cover=\"cover."+format+"\"");
				cover = null;
				
				BufferedImage preview = CoverManager.buildPreviewCoverImage(book);
				if (preview != null)
					ImageUtils.write(preview, format, new File(exportDir, "book"+bookNum+"."+format));
				preview = null;
			}
			BufferedImage innerCover = CoverManager.buildInnerCoverImage(book);
			if (innerCover != null)
			{
				ImageUtils.write(innerCover, format, new File(bookDir, "innerCover."+format));
				bookSpec.append(" innerCover=\"innerCover."+format+"\"");
				innerCover = null;
			}
			bookSpec.append(">\n");
			
			int pageNum = 0;
			int lastPage = book.getLastPageNumber();
			int imageNum = 0;
			for (int i=1;i<=lastPage;i++)
			{
				Page page = book.getPage(i);
				BufferedImage image = page.getImage().getImage();
				
				ImageUtils.write(options.handlePage(fix(image)), format, new File(bookDir, "image"+pageNum+"."+format));
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
							ImageUtils.write(fix(roiImage), format, new File(bookDir, "roiImage"+imageNum+"."+format));
							
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
}
