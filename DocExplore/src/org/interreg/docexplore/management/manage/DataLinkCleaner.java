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
