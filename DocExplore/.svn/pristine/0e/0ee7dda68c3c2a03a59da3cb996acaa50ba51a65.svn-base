package org.interreg.docexplore.management;

import java.io.InputStream;

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;

public class Clipboard
{
	String uri;
	
	public Clipboard()
	{
		this.uri = null;
	}
	
	public boolean hasCopiedId() {return uri != null;}
	
	public void copy(AnnotatedObject object)
	{
		uri = object.getCanonicalUri();
	}
	
	public void paste(AnnotatedObject target) throws Exception
	{
		if (!hasCopiedId())
			return;
		
		MetaDataKey mini = target.getLink().getOrCreateKey("mini", "");
		MetaDataKey dim = target.getLink().getOrCreateKey("dimension", "");
		MetaDataKey src = target.getLink().getOrCreateKey("source-file", "");
		
		AnnotatedObject from = AnnotatedObject.resolveUri(target.getLink(), uri);
		for (MetaDataKey key : from.getMetaData().keySet())
		{
			if (key == mini || key == dim || key == src)
				continue;
			
			for (MetaData md : from.getMetaDataListForKey(key))
			{
				InputStream value = md.getValue();
				MetaData mdCopy = new MetaData(target.getLink(), key, md.getType(), value);
				target.addMetaData(mdCopy);
			}
		}
	}
}
