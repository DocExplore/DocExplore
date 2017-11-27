/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.actions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.Pair;

public class AddPagesAction extends UnreversibleAction
{
	public final DocExploreDataLink link;
	public final Book book;
	public final List<File> files;
	public final List<Pair<AnnotatedObject, File>> failed = new LinkedList<Pair<AnnotatedObject, File>>();
	public final List<Page> pages = new LinkedList<Page>();
	double progress;
	
	public AddPagesAction(DocExploreDataLink link, Book book, List<File> files)
	{
		this.link = link;
		this.book = book;
		this.files = files;
	}

	public void doAction() throws Exception
	{
		progress = 0;
		final AtomicInteger cnt = new AtomicInteger(0);
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		try
		{
			for (final File file : files)
			{
				final Page page = book.appendPage(new FileImageSource(file));
				pages.add(page);
				service.execute(new Runnable() {public void run()
				{
					try
					{
						page.addMetaData(new MetaData(link, link.sourceKey, file.getName()));
						BufferedImage image = page.getImage().getImage();
						page.addMetaData(new MetaData(link, link.dimKey, image.getWidth()+","+image.getHeight()));
						DocExploreDataLink.getImageMini(page);
					}
					catch (Exception e)
					{
						ErrorHandler.defaultHandler.submit(e, true);
						synchronized (failed) {failed.add(new Pair<AnnotatedObject, File>(page, file));}
					}
					
					cnt.incrementAndGet();
					progress = cnt.get()*1./files.size();
				}});
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		
		while (cnt.get() < files.size())
			try {Thread.sleep(50);}
			catch (Exception e) {}
		
		for (Pair<AnnotatedObject, File> pair : failed)
			((Page)pair.first).removeFromBook();
	}
	
	public double progress() {return progress;}
	
	public String description()
	{
		return (files != null ? files.size() : pages.size()) == 1 ? XMLResourceBundle.getBundledString("addPage") : XMLResourceBundle.getBundledString("addPages");
	}
}
