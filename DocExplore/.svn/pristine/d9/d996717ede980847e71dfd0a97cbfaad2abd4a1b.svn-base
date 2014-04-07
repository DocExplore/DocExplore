package org.interreg.docexplore.management.export;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;

public class PageExport
{
	static class ExportArea
	{
		Rectangle rect;
		Shape shape;
		String label;
		
		public ExportArea(Page page, String label)
		{
			this.rect = null;
			this.shape = null;
			this.label = label;
		}
		
		public ExportArea(Region region, String label)
		{
			Point [] outline = region.getOutline();
			int [] xp = new int [outline.length], yp = new int [outline.length];
			for (int i=0;i<outline.length;i++)
				{xp[i] = outline[i].x; yp[i] = outline[i].y;}
			this.shape = new Polygon(xp, yp, outline.length);
			this.rect = shape.getBounds();
			this.label = label;
		}
	}
	
	ExportDialog dialog;
	int idCnt;
	OutputStreamWriter out;
	
	public PageExport(ExportDialog dialog) throws IOException
	{
		this.dialog = dialog;
		this.idCnt = 0;
		this.out = null;
		
		if (dialog.labelFile.isSelected())
		{
			File output = new File(new File(dialog.folderName.getText()), dialog.labelFileName.getText());
			out = new OutputStreamWriter(new FileOutputStream(output));
			out.write("<export>\n");
		}
	}
	
	public void complete() throws IOException
	{
		if (out != null)
		{
			out.write("</export>\n");
			out.close();
		}
	}
	
	public void exportPage(Page page) throws IOException, DataLinkException
	{
		//DocExploreDataLink link = (DocExploreDataLink)page.getLink();
		
		List<ExportArea> areas = new LinkedList<PageExport.ExportArea>();
		if (dialog.areaPage.isSelected())
			areas.add(new ExportArea(page, labelFor(page)));
		else for (Region region : page.getRegions())
			if (dialog.areaAllRois.isSelected() || dialog.areaRoiTags.matchesCriteria(region))
				areas.add(new ExportArea(region, labelFor(region)));
		
		BufferedImage content = null;
		if (dialog.contentPage.isSelected())
			try {content = page.getImage().getImage();}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		else if (dialog.contentAnnotationField.isSelected())
		{
			MetaDataKey key = dialog.contentAnnotationFieldName.getSelectedKey();//link.getKey(dialog.contentAnnotationFieldName.getText().trim());
			if (key != null)
				for (MetaData annotation : page.getMetaDataListForKey(key))
					if (annotation.getType().equals(MetaData.imageType))
						{content = annotation.getImage(); break;}
		}
		else
		{
			Map<MetaDataKey, List<MetaData>> map = page.getMetaData();
			for (Map.Entry<MetaDataKey, List<MetaData>> entry : map.entrySet())
				for (MetaData annotation : entry.getValue())
					if (annotation.getType().equals(MetaData.imageType) && dialog.contentAnnotationTags.matchesCriteria(annotation))
						{content = annotation.getImage(); break;}
		}
		
		if (content != null)
		{
			File dir = new File(dialog.folderName.getText());
			StringBuffer xmlBuffer = dialog.labelNone.isSelected() ? null : new StringBuffer();
			for (ExportArea area : areas)
			{
				BufferedImage image = null;
				if (area.rect == null)
					image = content;
				else
				{
					image = new BufferedImage((int)area.rect.getWidth(), (int)area.rect.getHeight(), BufferedImage.TYPE_INT_RGB);
					for (int i=0;i<image.getWidth();i++)
						for (int j=0;j<image.getHeight();j++)
					{
						double x = area.rect.x+i, y = area.rect.y+j;
						if (area.shape.contains(x, y) && x>=0 && y>=0 && x<content.getWidth() && y < content.getHeight())
							image.setRGB(i, j, content.getRGB((int)x, (int)y));
						else image.setRGB(i, j, (dialog.fillBlack.isSelected() ? Color.black : Color.white).getRGB());
					}
				}
				
				String fileName = "image"+idCnt+"."+dialog.imageFormat.getSelectedItem().toString();
				idCnt++;
				File output = new File(dir, fileName);
				try
				{
					ImageUtils.write(image, dialog.imageFormat.getSelectedItem().toString(), output);
					if (xmlBuffer != null)
					{
						xmlBuffer.append("\t<image src=\"").append(fileName).append("\"");
						if (area.label != null)
							xmlBuffer.append(" label=\"").append(area.label).append("\"");
						xmlBuffer.append("/>\n");
					}
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			
			if (xmlBuffer != null)
				out.write(xmlBuffer.toString());
		}
			
		page.unloadImage();
		page.unloadMetaData();
	}
	
	String labelFor(AnnotatedObject object) throws DataLinkException
	{
		DocExploreDataLink link = (DocExploreDataLink)object.getLink();
		
		if (dialog.labelNone.isSelected())
			return null;
		if (dialog.labelAnnotationField.isSelected())
		{
			MetaDataKey key = dialog.labelAnnotationFieldName.getSelectedKey();//link.getKey(dialog.labelAnnotationFieldName.getText().trim());
			if (key != null)
				for (MetaData annotation : object.getMetaDataListForKey(key))
					if (annotation.getType().equals(MetaData.textType))
						return annotation.getString();
		}
		else if (dialog.labelAnnotationTag.isSelected())
		{
			Map<MetaDataKey, List<MetaData>> map = object.getMetaData();
			for (Map.Entry<MetaDataKey, List<MetaData>> entry : map.entrySet())
				for (MetaData annotation : entry.getValue())
					if (annotation.getType().equals(MetaData.textType) && dialog.labelAnnotationTags.matchesCriteria(annotation))
						return annotation.getString();
		}
		else
		{
			for (MetaData transcription : object.getMetaDataListForKey(link.transcriptionKey))
				if (dialog.labelTranscriptionTags.matchesCriteria(transcription))
				{
					String full = transcription.getString();
					return full.substring(full.indexOf("<content>")+"<content>".length(), full.indexOf("</content>"));
				}
		}
		return "";
	}
}
