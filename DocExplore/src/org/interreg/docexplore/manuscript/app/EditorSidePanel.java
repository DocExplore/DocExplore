package org.interreg.docexplore.manuscript.app;

import java.awt.Component;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;

public interface EditorSidePanel
{
	public void setDocument(AnnotatedObject document) throws DataLinkException;
	public void onShow();
	public void onHide();
	public Component getComponent();
	public MetaData addAnnotation();
}
