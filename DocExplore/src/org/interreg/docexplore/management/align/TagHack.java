package org.interreg.docexplore.management.align;

import java.util.List;
import java.util.Set;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

public class TagHack
{
	public static void doTagHack(DocExploreDataLink link) throws Exception
	{
		MetaData descTag = link.getOrCreateTag("Description", "fr");
		
		Book book = link.getBook(1);
		int lastPage = book.getLastPageNumber();
		for (int pn=1;pn<=lastPage;pn++)
		{
			Page page = book.getPage(pn);
			Set<Region> regions = page.getRegions();
			
			for (Region region : regions)
			{
				boolean isTitle = false;
				List<MetaData> transcriptions = region.getMetaDataListForKey(link.transcriptionKey);
				for (MetaData trans : transcriptions)
				{
					if (trans.getMetaDataListForKey(link.tagKey).isEmpty())
						trans.addMetaData(descTag);
					else
					{
						//System.out.println(trans.getMetaDataListForKey(link.tagKey).get(0).getString());
						if (trans.getMetaDataListForKey(link.tagKey).get(0).getString().toLowerCase().contains("titre"))
							isTitle = true;
					}
				}
				region.unloadMetaData();
				if (isTitle)
					page.removeRegion(region);
			}
		}
	}
}
