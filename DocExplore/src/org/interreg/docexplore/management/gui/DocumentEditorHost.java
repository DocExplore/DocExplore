package org.interreg.docexplore.management.gui;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.manage.ActionRequestListener;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;

public interface DocumentEditorHost
{
	public DocExploreDataLink getLink();
	public ActionRequestListener getActionListener();
	public void setMessage(String s);
	
	public void onDocumentSwitched(AnnotatedObject document);
	public DocumentPanel onDocumentEditorRequest(AnnotatedObject document);
	public void onCloseRequest();
	public MetaData onAddAnnotationRequest();
	public void onAnalysisRequest(BufferedImage image);
	public void onActionStateRequestCompleted();
}
