package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.util.List;

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.actions.DeleteMetaDataAction;

public class DeleteFS2MetaDataAction extends DeleteMetaDataAction
{
	DataLinkFS2 link;
	
	public DeleteFS2MetaDataAction(DataLinkFS2 link, AnnotatedObject document, MetaData annotation)
	{
		super(document, annotation);
		this.link = link;
	}
	public DeleteFS2MetaDataAction(DataLinkFS2 link, AnnotatedObject document, List<MetaData> annotations)
	{
		super(document, annotations);
		this.link = link;
	}

	File fromDir = null;
	public void doAction() throws Exception
	{
		super.doAction();
	}

	public void undoAction() throws Exception
	{
		for (MetaData annotation : annotations)
			document.addMetaData(annotation);
	}
}
