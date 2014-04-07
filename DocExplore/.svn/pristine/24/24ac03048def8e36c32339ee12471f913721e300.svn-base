package org.interreg.docexplore.authoring;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.StringUtils;

public class ReaderExporter extends PresentationExporter
{
	File exportDir = new File(DocExploreTool.getHomeDir(), "reader");
	
	public ReaderExporter(AuthoringToolFrame tool)
	{
		super(tool);
	}
	
	public void doExport(final DocExploreDataLink link) throws Exception
	{
		File indexFile = new File(exportDir, "index.xml");
		if (!indexFile.exists())
		{
			ErrorHandler.defaultHandler.submit(new Exception("Invalid server resource directory"));
			return;
		}
		
		ExportOptions options = ExportOptions.getOptions(tool);
		if (options == null)
			return;
		
		for (int bookId : link.getLink().getAllBookIds())
		{
			Book book = link.getBook(bookId);
			doExport(book, exportDir, indexFile, options);
		}
	}

	private void doExport(Book book, File exportDir, File indexFile, ExportOptions options) throws Exception
	{
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
			String title = book.getName();
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
					int res = JOptionPane.showConfirmDialog(tool, XMLResourceBundle.getBundledString("exportDuplicateMessage"), "Confirmation", 
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
						String name = JOptionPane.showInputDialog(tool, XMLResourceBundle.getBundledString("collectionAddBookMessage"), book.getName());
						if (name == null)
							return;
						book.setName(name);
						doExport(book, exportDir, indexFile, options);
						return;
					}
					break;
				}
				curIndex = endIndex;
			}
			
			int insertIndex = xml.indexOf("</Index>");
			if (insertIndex < 0)
				throw new Exception("Invalid index file");
			
			String desc = "";//JOptionPane.showInputDialog("Optionally, you may also provide a short description");
			xml = xml.substring(0, insertIndex)+"\t<Book title=\""+title+"\" src=\""+bookFileName+"\">\n\t\t"+desc+"\n\t</Book>\n"+
				xml.substring(insertIndex);
			FileOutputStream indexOutput = new FileOutputStream(indexFile);
			indexOutput.write(xml.getBytes(Charset.forName("UTF-8")));
			indexOutput.close();
			
			doExport(book, exportDir, options, bookNum);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
}
