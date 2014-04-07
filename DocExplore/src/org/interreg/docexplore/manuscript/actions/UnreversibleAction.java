package org.interreg.docexplore.manuscript.actions;

import org.interreg.docexplore.util.history.ReversibleAction;

public abstract class UnreversibleAction extends ReversibleAction
{
	public void undoAction() throws Exception
	{
		throw new Exception("Action '"+description()+"' can't be undone!");
	}
}
