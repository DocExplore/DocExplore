package org.interreg.docexplore.management.image;

import java.awt.Color;
import java.awt.Component;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.management.gui.DocumentEditor;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;

public class ImageMetaDataEditor extends ImageView implements DocumentEditor
{
	private static final long serialVersionUID = 6910768551955062736L;
	
	protected DocumentEditorHost host;
	MetaData metaData;
	
	public ImageMetaDataEditor(DocumentEditorHost host, MetaData metaData) throws Exception
	{
		super();
		
		this.host = host;
		this.metaData = metaData;
		if (metaData != null)
			setImage(metaData.getImage());
		setBackground(Color.white);
	}
	
	public DocumentEditorHost getHost() {return host;}
	public MetaData getMetaData() {return metaData;}
	public AnnotatedObject getDocument() {return metaData;}
	
	public void switchDocument(AnnotatedObject document) throws Exception
	{
		if (metaData != null && document.getId() == metaData.getId())
			return;
		host.onDocumentSwitched(document);
		int oldId = metaData !=null ? metaData.getId() : -1;
		this.metaData = (MetaData)document;
		setImage(metaData.getImage(), oldId != metaData.getId());
	}
	
	@Override public void cancelOperation()
	{
		super.cancelOperation();
		host.onActionStateRequestCompleted();
	}
	@Override protected void onMessageChanged(String s)
	{
		host.setMessage(s);
	}
	@Override public void goTo(String s) throws Exception {}
	@Override public Component getComponent() {return this;}
	@Override public void refresh()
	{
		if (metaData == null)
			return;
		try {setImage(metaData.getImage(), false);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		repaint();
	}
	@Override public void onActionRequest(String action) throws Exception
	{
		if (metaData == null)
			return;
		if (action.equals("fit"))
			fit();
	}
	@Override public void onActionStateRequest(String action, boolean state) throws Exception
	{
		if (metaData == null)
			return;
		if (action.equals("crop"))
		{
			if (!state) cancelOperation();
			else setOperation(new CropOperation());
		}
		else return;
		repaint();
	}
	@Override public void onClose()
	{
		metaData.unloadMetaData();
	}
}
