package org.interreg.docexplore.manuscript.actions;

import org.interreg.docexplore.util.history.ReversibleAction;

public class WrappedAction extends ReversibleAction
{
	ReversibleAction action;
	
	public WrappedAction(ReversibleAction action) {this.action = action;}
	public void doAction() throws Exception {action.cacheDir = cacheDir; action.doAction();}
	public void undoAction() throws Exception {action.undoAction();}
	public String description() {return action.description();}
	public void dispose() {action.dispose();}
	public double progress() {return action.progress();}
}
