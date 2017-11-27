package org.interreg.docexplore.manuscript.actions;

import org.interreg.docexplore.util.history.ReversibleAction;

public class CompoundAction extends ReversibleAction
{
	ReversibleAction [] actions;
	
	public CompoundAction(ReversibleAction ... actions)
	{
		this.actions = actions;
	}
	
	@Override public void doAction() throws Exception
	{
		for (int i=0;i<actions.length;i++)
		{
			actions[i].cacheDir = cacheDir;
			actions[i].doAction();
		}
	}

	@Override public void undoAction() throws Exception
	{
		for (int i=actions.length-1;i>=0;i--)
			actions[i].undoAction();
	}
	
	@Override public void dispose()
	{
		for (int i=0;i<actions.length;i++)
			actions[i].dispose();
	}

	@Override public String description()
	{
		String desc = "";
		for (int i=0;i<actions.length;i++)
			desc += (i==0 ? "" : ", ")+actions[i].description();
		return desc;
	}

}
