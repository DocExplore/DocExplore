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
