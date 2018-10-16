/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript;

import java.util.List;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.util.history.ReversibleAction;

public class CoverUtils
{
	public static enum Part 
	{
		Front {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.frontCover;}}, 
		Back {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.backCover;}}, 
		FrontInner {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.frontInnerCover;}}, 
		BackInner {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.backInnerCover;}}, 
		FrontTrans {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.frontCoverTrans;}}, 
		BackTrans {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.backCoverTrans;}}, 
		FrontInnerTrans {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.frontInnerCoverTrans;}}, 
		BackInnerTrans {@Override public MetaDataKey getKey(DocExploreDataLink link) {return link.backInnerCoverTrans;}};
		public abstract MetaDataKey getKey(DocExploreDataLink link);
	};
	
	public static MetaData [] getCoverImages(DocExploreDataLink link, Book book) throws DataLinkException
	{
		MetaData [] images = new MetaData [Part.values().length];
		for (int i=0;i<images.length;i++)
		{
			List<MetaData> list = book.getMetaDataListForKey(Part.values()[i].getKey(link));
			if (!list.isEmpty())
				images[i] = list.get(0);
		}
		return images;
	}
	
	public static ReversibleAction importCover(ManuscriptAppHost host, Book book, MetaData [] sourceImages) throws DataLinkException
	{
		MetaData [] images = new MetaData [sourceImages.length];
		for (int i=0;i<images.length;i++)
			if (sourceImages[i] != null)
				images[i] = new MetaData(host.getLink(), Part.values()[i].getKey(host.getLink()), MetaData.imageType, sourceImages[i].getValue());
		
		DeleteMetaDataAction [] deleteActions = new DeleteMetaDataAction [images.length];
		AddMetaDataAction [] addActions = new AddMetaDataAction [images.length];
		MetaData [] old = getCoverImages(host.getLink(), book);
		for (int i=0;i<old.length;i++)
		{
			if (old[i] != null)
				deleteActions[i] = host.getLink().actionProvider().deleteMetaData(book, old[i]);
			if (images[i] != null)
				addActions[i] = host.getLink().actionProvider().addMetaData(book, images[i]);
		}
		return new ReversibleAction()
		{
			@Override public void doAction() throws Exception
			{
				for (int i=0;i<addActions.length;i++)
				{
					if (deleteActions[i] != null) deleteActions[i].doAction();
					if (addActions[i] != null) addActions[i].doAction();
				}
				DocumentEvents.broadcastChanged(host, book);
			}
			@Override public void undoAction() throws Exception
			{
				for (int i=0;i<addActions.length;i++)
				{
					if (addActions[i] != null) addActions[i].undoAction();
					if (deleteActions[i] != null) deleteActions[i].undoAction();
				}
				DocumentEvents.broadcastChanged(host, book);
			}
			@Override public String description() {return Lang.s("importCover");}
		};
	}
}
