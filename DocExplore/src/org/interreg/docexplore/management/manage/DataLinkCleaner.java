/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.manage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

public class DataLinkCleaner implements ProgressRunnable
{
	MainWindow win;
	
	public DataLinkCleaner(MainWindow win)
	{
		this.win = win;
	}
	
	public void run()
	{
		try
		{
			DocExploreDataLink link = win.getDocExploreLink();
			if (link == null || link.getLink() == null)
				throw new Exception(XMLResourceBundle.getBundledString("cleanLinkNoConnection"));
			
			Set<Integer> referencedIds = getAllReferencedMetaDataIds(link);
			Set<Integer> allIds = new TreeSet<Integer>();
			Collection<MetaDataKey> keys = link.getAllKeys();
			int cnt = 0;
			for (MetaDataKey key : keys)
			{
				allIds.addAll(link.getLink().getMetaDataIds(key.getId(), null));
				cnt++;
				progress = .5f+cnt*.25f/keys.size();
			}
			allIds.removeAll(referencedIds);
			
			System.out.println(referencedIds.size()+" referenced annotations");
			System.out.println(allIds.size()+" unreferenced annotations");
			
			win.historyManager.reset();
			
			cnt = 0;
			for (int id : allIds)
			{
				link.getLink().removeMetaData(id);
				cnt++;
				progress = .75f+cnt*.25f/allIds.size();
			}
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public Set<Integer> getAllReferencedMetaDataIds(DocExploreDataLink link) throws DataLinkException
	{
		Set<Integer> res = new TreeSet<Integer>();
		
		List<Integer> bookIds = link.getLink().getAllBookIds();
		int cnt = 0;
		for (int bookId : bookIds)
		{
			Book book = link.getBook(bookId);
			getMetaDataIds(book, res);
			
			for (int pageNum=1;pageNum<=book.getLastPageNumber();pageNum++)
			{
				Page page = book.getPage(pageNum);
				getMetaDataIds(page, res);
				
				for (Region region : page.getRegions())
					getMetaDataIds(region, res);
				page.unloadAll();
			}
			book.unloadMetaData();
			cnt++;
			progress = cnt*.25f/bookIds.size();
		}
		
		cnt = 0;
		Set<Integer> mdMds = new TreeSet<Integer>();
		for (int mdId : res)
		{
			MetaData md = link.getMetaData(mdId);
			getMetaDataIds(md, mdMds);
			cnt++;
			progress = .25f+cnt*.25f/res.size();
		}
		res.addAll(mdMds);
		
		return res;
	}
	static void getMetaDataIds(AnnotatedObject object, Set<Integer> res) throws DataLinkException
	{
		for (Map.Entry<MetaDataKey, List<MetaData>> mds : object.getMetaData().entrySet())
			for (MetaData md : mds.getValue())
				res.add(md.getId());
		object.unloadMetaData();
	}

	float progress = 0;
	public float getProgress() {return progress;}
}
