package org.interreg.docexplore.manuscript.app;

import java.awt.image.BufferedImage;

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;

public interface DocumentEditorHost
{
	public ManuscriptAppHost getAppHost();
	public void setMessage(String s);
	
	public void switchDocument(AnnotatedObject document);
	public DocumentPanel onDocumentEditorRequest(AnnotatedObject document);
	public void onCloseRequest();
	public MetaData onAddAnnotationRequest();
	public void onAnalysisRequest(BufferedImage image);
	public void onActionStateRequestCompleted();
}
