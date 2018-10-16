/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.actions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.MemoryImageSource;
import org.interreg.docexplore.util.Pair;

public class AddPosterPartsAction extends UnreversibleAction
{
	public DocExploreDataLink link;
	public Book book;
	public List<File> files;
	public final List<Pair<AnnotatedObject, File>> failed = new LinkedList<Pair<AnnotatedObject, File>>();
	public final List<MetaData> parts = new LinkedList<MetaData>();
	public double progress;
	
	public AddPosterPartsAction(DocExploreDataLink link, Book book, List<File> files)
	{
		this.link = link;
		this.book = book;
		this.files = files;
	}
	
	@Override public void doAction() throws Exception
	{
		progress = 0;
		final AtomicInteger cnt = new AtomicInteger(0);
		ExecutorService service = Executors.newFixedThreadPool(2);
		
		for (final File file : files)
		{
			final MetaData part = new MetaData(link, link.partKey, MetaData.imageType, new ByteArrayInputStream(new byte [0]));
			parts.add(part);
			book.addMetaData(part);
			service.execute(new Runnable() {public void run()
			{
				try
				{
					part.setMetaDataString(link.bookKey, ""+book.getId());
					
					part.setValue(MetaData.imageType, new FileInputStream(file));
					part.addMetaData(new MetaData(link, link.sourceKey, file.getName()));
					BufferedImage image = ImageUtils.read(part.getValue());
					part.addMetaData(new MetaData(link, link.dimKey, image.getWidth()+","+image.getHeight()));
					
					DocExploreDataLink.getImageMini(part);
				}
				catch (Exception e)
				{
					ErrorHandler.defaultHandler.submit(e, true);
					synchronized (failed) {failed.add(new Pair<AnnotatedObject, File>(part, file));}
				}
				
				cnt.incrementAndGet();
				progress = cnt.get()*1./files.size();
			}});
		}
		
		while (cnt.get() < files.size())
			try {Thread.sleep(50);}
			catch (Exception e) {}
		
		for (Pair<AnnotatedObject, File> pair : failed)
			book.removeMetaData((MetaData)pair.first);
		
		if (book.getLastPageNumber() == 0) try
		{
			PosterUtils.assignPartLocations(link, book);
			BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);//PosterUtils.buildComposite(link, book, progress);
			Page page = book.appendPage(new MemoryImageSource(image));
			page.setMetaDataString(link.dimKey, image.getWidth()+","+image.getHeight());
			DocExploreDataLink.getImageMini(page);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, false);}
		else
		{
			List<MetaData> mds = book.getMetaDataListForKey(link.partKey);
			int max = -1;
			for (MetaData md : mds)
			{
				String pos = md.getMetaDataString(link.partPosKey);
				if (pos == null)
					continue;
				int row = Integer.parseInt(pos.split(",")[1]);
				if (row > max)
					max = row;
			}
			int colCnt = 0;
			for (MetaData part : parts)
			{
				part.setMetaDataString(link.partPosKey, (colCnt++)+","+(max+1));
			}
		}
		book.setMetaDataString(link.upToDateKey, "false");
	}

	public String description()
	{
		return (files != null ? files.size() : parts.size()) == 1 ? Lang.s("addPart") : Lang.s("addParts");
	}

	public double progress() {return progress;}
}
