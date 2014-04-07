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
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.Pair;

public class AddPagesAction extends UnreversibleAction
{
	public final DocExploreDataLink link;
	public final Book book;
	public final List<File> files;
	public final List<Pair<Page, File>> failed = new LinkedList<Pair<Page, File>>();
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
			final MetaDataKey sourceKey = link.getOrCreateKey("source-file", "");
			final MetaDataKey dimKey = link.getOrCreateKey("dimension", "");
			
			for (final File file : files)
			{
				final Page page = book.appendPage(new FileImageSource(file));
				pages.add(page);
				service.execute(new Runnable() {public void run()
				{
					try
					{
						page.addMetaData(new MetaData(link, sourceKey, file.getName()));
						BufferedImage image = page.getImage().getImage();
						page.addMetaData(new MetaData(link, dimKey, image.getWidth()+","+image.getHeight()));
						DocExploreDataLink.getImageMini(page);
					}
					catch (Exception e)
					{
						ErrorHandler.defaultHandler.submit(e, true);
						synchronized (failed) {failed.add(new Pair<Page, File>(page, file));}
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
		
		for (Pair<Page, File> pair : failed)
			pair.first.removeFromBook();
	}
	
	public double progress() {return progress;}
	
	public String description()
	{
		return (files != null ? files.size() : pages.size()) == 1 ? XMLResourceBundle.getBundledString("addPage") : XMLResourceBundle.getBundledString("addPages");
	}
}
