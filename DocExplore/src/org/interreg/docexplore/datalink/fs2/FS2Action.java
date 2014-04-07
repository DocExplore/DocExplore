package org.interreg.docexplore.datalink.fs2;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.util.history.ReversibleAction;

public abstract class FS2Action extends ReversibleAction
{
	DocExploreDataLink link;
	
	public FS2Action(DocExploreDataLink link)
	{
		if (link.getLink() == null || !(link.getLink() instanceof DataLinkFS2))
			throw new RuntimeException("Action requires FS2 DataLink!");
		this.link = link;
	}
}
