package org.interreg.docexplore.manuscript.actions;

import java.util.Collections;
import java.util.List;

import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;

public class AddMetaDataAction extends UnreversibleAction
{
	public AnnotatedObject document;
	public List<MetaData> annotations;
	
	public AddMetaDataAction(AnnotatedObject document, MetaData annotation)
	{
		this.document = document;
		this.annotations = Collections.singletonList(annotation);
	}
	public AddMetaDataAction(AnnotatedObject document, List<MetaData> annotations)
	{
		this.document = document;
		this.annotations = annotations;
	}
	
	public void doAction() throws Exception
	{
		for (MetaData annotation : annotations)
			document.addMetaData(annotation);
	}

	public String description() {return XMLResourceBundle.getBundledString("addMetaData");}
	
	public void dispose()
	{
		document = null;
		annotations = null;
	}
}
