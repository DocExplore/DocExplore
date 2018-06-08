package org.interreg.docexplore.manuscript.app.editors;

import java.awt.Color;
import java.awt.Component;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;

public class ImageMetaDataEditor extends ImageView implements DocumentPanelEditor
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
		host.switchDocument(document);
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
	@Override public Component getComponent() {return this;}
	@Override public void refresh()
	{
		if (metaData == null)
			return;
		try {setImage(metaData.getImage(), false);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		repaint();
	}
	@Override public void onActionRequest(String action, Object param) throws Exception
	{
		if (metaData == null)
			return;
		if (action.equals("fit"))
			fit();
		if (action.equals("crop"))
		{
			if (!(Boolean)param) cancelOperation();
			else setOperation(new CropOperation());
		}
		else return;
		repaint();
	}
	@Override public void onShow() {}
	@Override public void onHide() {}
	@Override public void onCloseRequest() {metaData.unloadMetaData();}
	
	@Override public boolean allowsSidePanel() {return true;}
}
