package org.interreg.docexplore.management.gui;

import java.awt.Component;

public interface DocumentEditor
{
	public Component getComponent();
	public void refresh();
	public void goTo(String s) throws Exception;
	
	public void onActionRequest(String action) throws Exception;
	public void onActionStateRequest(String action, boolean state) throws Exception;
	public void onClose();
}
