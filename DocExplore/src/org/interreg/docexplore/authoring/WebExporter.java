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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.ByteUtils;
import org.interreg.docexplore.util.ZipUtils;

public class WebExporter extends PresentationExporter
{
	File exportDir = new File(DocExploreTool.getHomeDir(), "web-tmp");
	
	public WebExporter(AuthoringToolFrame tool)
	{
		super(tool);
	}

	boolean copyComplete = false;
	public void doExport(final DocExploreDataLink link, boolean noHtml) throws Exception
	{
		copyComplete = false;
		ExportOptions options = ExportOptions.getOptions(tool, noHtml ? ExportDialog.MobileExport : ExportDialog.WebExport);
		if (options == null)
			return;
		
		File exportTo = DocExploreTool.getFileDialogs().saveFile(noHtml ? DocExploreTool.getMobileIBookCategory() : DocExploreTool.getWebIBookCategory());
		if (exportTo == null)
			return;
		if (exportTo.exists() && 
			JOptionPane.showConfirmDialog(tool, "A file with the same name already exists. Do you wish to overwrite it?", "Overwrite", 
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
		
		exportDir.mkdirs();
		Book book = link.getBook(link.getLink().getAllBookIds().get(0));
		doExport(book, exportDir, options, 0, "JPG", noHtml ? ExportDialog.MobileExport : ExportDialog.WebExport);
		
		if (!noHtml)
		{
			copyResource("org/interreg/docexplore/reader/web/index.html", exportDir);
			copyResource("org/interreg/docexplore/reader/web/back.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/empty.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/left.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/right.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/zoom.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/zoomin.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/zoomout.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/floor.png", exportDir);
			copyResource("org/interreg/docexplore/reader/web/loading.jpg", exportDir);
			copyResource("org/interreg/docexplore/reader/web/empty.png", exportDir);
			File jsDir = new File(exportDir, "js");
			jsDir.mkdir();
			copyResource("org/interreg/docexplore/reader/web/js/BookCover.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/BookModel.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/BookPage.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/BookPageStack.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Camera.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Floor.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/hammer.min.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Hand.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Input.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/jquery-3.1.1.min.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Math3D.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/PaperCurve.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Reader.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Region.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/Specification.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/SpringPaper.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/TexLoader.js", jsDir);
			copyResource("org/interreg/docexplore/reader/web/js/three.min.js", jsDir);
		}
		copyComplete = true;
		
		ZipUtils.zip(exportDir, exportTo, progress);
		FileUtils.deleteDirectory(exportDir);
	}
	
	static void copyResource(String resource, File dir) throws Exception
	{
		File dest = new File(dir, resource.substring(resource.lastIndexOf('/')+1));
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		byte [] bytes = ByteUtils.readStream(stream);
		stream.close();
		ByteUtils.writeFile(dest, bytes);
	}
}
