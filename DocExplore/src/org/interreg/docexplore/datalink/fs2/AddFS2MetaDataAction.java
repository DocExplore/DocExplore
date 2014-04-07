package org.interreg.docexplore.datalink.fs2;

import java.util.List;

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;

public class AddFS2MetaDataAction extends AddMetaDataAction
{
	DataLinkFS2 link;
	
	public AddFS2MetaDataAction(DataLinkFS2 link, AnnotatedObject document, MetaData annotation)
	{
		super(document, annotation);
		this.link = link;
	}
	public AddFS2MetaDataAction(DataLinkFS2 link, AnnotatedObject document, List<MetaData> annotations)
	{
		super(document, annotations);
		this.link = link;
	}

	DeleteFS2MetaDataAction reverse = null;
	public void doAction() throws Exception
	{
		if (reverse == null)
			super.doAction();
		else reverse.undoAction();
	}
	
	public void undoAction() throws Exception
	{
		if (reverse == null)
		{
			reverse = new DeleteFS2MetaDataAction(link, document, annotations);
			reverse.cacheDir = cacheDir;
		}
		reverse.doAction();
	}
}
