package org.interreg.docexplore;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.StringUtils;

public class PresentationImporter
{
	File exportDir = new File(DocExploreTool.getHomeDir(), "reader");
	
	public PresentationImporter()
	{
	}
	
	double progress = 0;
	void doImport(Component comp, String title, String desc, File bookFile) throws Exception
	{
		progress = 0;
		File indexFile = new File(exportDir, "index.xml");
		if (!indexFile.exists())
			{ErrorHandler.defaultHandler.submit(new Exception("Invalid server resource directory"));}
		
		try
		{
			int bookNum = 0;
			for (String filename : exportDir.list())
				if (filename.startsWith("book") && filename.endsWith(".xml"))
			{
				String numString = filename.substring(4, filename.length()-4);
				try
				{
					int num = Integer.parseInt(numString);
					if (num >= bookNum)
						bookNum = num+1;
				}
				catch (Exception e) {}
			}
			
			String bookFileName = "book"+bookNum+".xml";
			String xml = StringUtils.readFile(indexFile, "UTF-8");
			
			//look for duplicate
			int curIndex = 0;
			while (curIndex >= 0)
			{
				int startIndex = xml.indexOf("<Book", curIndex);
				if (startIndex < 0)
					break;
				int index = xml.indexOf("title=\"", startIndex);
				if (index < 0)
					break;
				index += "title=\"".length();
				int endIndex = xml.indexOf("\"", index);
				String testTitle = xml.substring(index, endIndex);
				if (testTitle.equals(title))
				{
					int res = JOptionPane.showConfirmDialog(comp, XMLResourceBundle.getString("authoring-lrb", "exportDuplicateMessage"), "Confirmation", 
						JOptionPane.YES_NO_CANCEL_OPTION);
					if (res == JOptionPane.CANCEL_OPTION)
						return;
					if (res == JOptionPane.YES_OPTION)
					{
						endIndex = xml.indexOf("</Book>", startIndex);
						xml = xml.substring(0, startIndex)+xml.substring(endIndex+"</Book>".length(), xml.length());
					}
					else
					{
						title = JOptionPane.showInputDialog(comp, XMLResourceBundle.getString("authoring-lrb", "collectionAddBookMessage"), title);
						if (title != null)
							doImport(comp, title, desc, bookFile);
						return;
					}
					break;
				}
				curIndex = endIndex;
			}
			
			int insertIndex = xml.indexOf("</Index>");
			if (insertIndex < 0)
				throw new Exception("Invalid index file");
			
			xml = xml.substring(0, insertIndex)+"\t<Book title=\""+title+"\" src=\""+bookFileName+"\">\n\t\t"+desc+"\n\t</Book>\n"+
				xml.substring(insertIndex);
			FileOutputStream indexOutput = new FileOutputStream(indexFile);
			indexOutput.write(xml.getBytes(Charset.forName("UTF-8")));
			indexOutput.close();
			
			File bookDir = new File(exportDir, "book"+bookNum);
			String bookSpec = StringUtils.readFile(bookFile, "UTF-8");
			int start = bookSpec.indexOf("path=\"")+6;
			int end = bookSpec.indexOf("\"", start);
			bookSpec = bookSpec.substring(0, start)+bookDir.getName()+"/"+bookSpec.substring(end);
			
			FileOutputStream bookOutput = new FileOutputStream(new File(exportDir, bookFileName));
			bookOutput.write(bookSpec.toString().getBytes(Charset.forName("UTF-8")));
			bookOutput.close();
			
			File contentDir = new File(bookFile.getParentFile(), bookFile.getName().substring(0, bookFile.getName().length()-4));
			FileUtils.moveDirectory(contentDir, bookDir);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
