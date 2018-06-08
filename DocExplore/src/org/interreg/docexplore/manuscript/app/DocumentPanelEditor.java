package org.interreg.docexplore.manuscript.app;

import java.awt.Component;

public interface DocumentPanelEditor
{
	public Component getComponent();
	public void refresh();
	public boolean allowsSidePanel();
	
	public void onActionRequest(String action, Object parameter) throws Exception;
	public void onCloseRequest();
	public void onShow();
	public void onHide();
}
