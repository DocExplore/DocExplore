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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFileChooser;

import org.imgscalr.Scalr;
import org.interreg.docexplore.authoring.preview.RegionPreview;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;

public class WebStaticExporter
{
	JFileChooser exportChooser = new JFileChooser();
	
	int imageCnt = 0;
	int maxDim = 500;
	String writeImage(BufferedImage image, File dir) throws Exception
	{
		if (image.getWidth() > maxDim || image.getHeight() > maxDim)
		{
			int w = image.getWidth(), h = image.getHeight();
			if (w > maxDim) {h = h*maxDim/w; w = maxDim;}
			if (h > maxDim) {w = w*maxDim/h; h = maxDim;}
			image = Scalr.resize(image, Scalr.Method.QUALITY, w, h);
		}
		String name = "image"+imageCnt+".jpg";
		imageCnt++;
		File file = new File(dir, name);
		ImageUtils.write(image, "JPEG", file);
		return name;
	}
	
	RegionPreview rp = new RegionPreview();
	public void doExport(Book book) throws Exception
	{
		File exportDir = null;
		exportChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		exportChooser.setCurrentDirectory(exportDir);
		if (exportChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		exportDir = exportChooser.getSelectedFile();
		
		try
		{
			imageCnt = 0;
			StringBuffer html = new StringBuffer();
			html.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>\n");
			
			html.append("<center><h1>").append(book.getName()).append("</h1></center><br/>\n");
			int lastPage = book.getLastPageNumber();
			for (int i=1;i<=lastPage;i++)
			{
				Page page = book.getPage(i);
				html.append("<h2>").append("Page ").append(i).append("</h2><br/>\n");
				
				html.append("<image src=\"").append(writeImage(page.getImage().getImage(), exportDir)).append("\" /><br/><br/><br/>\n");
				
				int regionCnt = 1;
				for (Region region : page.getRegions())
				{
					html.append("<table><tr><td colspan=2><h3>").append("Region ").append(regionCnt).append("</h3></td></tr>");
					html.append("<tr><td valign=top>\n");
					html.append("<image src=\"").append(writeImage(rp.buildPreview(region), exportDir)).append("\" />\n");
					html.append("</td><td valign=top height=100%><div style=\"background:black\">\n");
					
					Map<Integer, MetaData> mds = new TreeMap<Integer, MetaData>();
					for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
						for (MetaData md : entry.getValue())
							mds.put(BookImporter.getRank(md), md);
					for (MetaData md : mds.values())
					{
						if (md.getType().equals(MetaData.textType))
						{
							html.append(md.getString()).append("\n");
						}
						else if (md.getType().equals(MetaData.imageType))
						{
							html.append("<image src=\"").append(writeImage(md.getImage(), exportDir)).append("\" />\n");
						}
						html.append("<br/>\n");
					}
					html.append("</div></td></tr></table><br/><br/>\n");
					
					region.unloadMetaData();
					regionCnt++;
				}
				
				page.unloadImage();
				page.unloadMetaData();
				page.unloadRegions();
			}
			
			html.append("</body></html>\n");
			
			FileOutputStream bookOutput = new FileOutputStream(new File(exportDir, "index.html"));
			bookOutput.write(html.toString().getBytes(Charset.forName("UTF-8")));
			bookOutput.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
