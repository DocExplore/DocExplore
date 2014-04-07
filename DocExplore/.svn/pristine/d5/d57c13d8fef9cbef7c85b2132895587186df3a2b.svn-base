package org.interreg.docexplore.util.history;

import java.io.File;

public abstract class ReversibleAction
{
	public File cacheDir = null;
	
	public abstract void doAction() throws Exception;
	public abstract void undoAction() throws Exception;
	public void dispose() {}
	public abstract String description();
	public double progress() {return 0;}
}
