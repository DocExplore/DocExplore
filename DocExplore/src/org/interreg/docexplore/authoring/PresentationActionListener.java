package org.interreg.docexplore.authoring;

import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;

public class PresentationActionListener implements ManuscriptAppHost.DocExploreActionListener
{
	public final ATApp app;
	
	public PresentationActionListener(ATApp app)
	{
		this.app = app;
	}
	
	@Override public void onAction(String action, Object param)
	{
		if (action.equals("new-presentation"))
		{
			
		}
		else if (action.equals("load-presentation"))
		{
			
		}
	}
	
}
