/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.align;

import java.util.List;
import java.util.Set;

import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
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
