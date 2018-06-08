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
