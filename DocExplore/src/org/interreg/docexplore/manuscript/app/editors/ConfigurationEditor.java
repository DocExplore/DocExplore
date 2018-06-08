package org.interreg.docexplore.manuscript.app.editors;

import java.awt.Component;

public interface ConfigurationEditor
{
	public Component getComponent();
	public boolean allowGoto();
	public void onActionRequest(String action, Object param);
	public void onCloseRequest();
	public void refresh();
	public void setReadOnly(boolean b);
}
