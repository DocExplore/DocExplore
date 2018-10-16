/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
		else if (action.equals("rotate-left"))
			host.getAppHost().getActionRequestListener().onRotateMetaDataLeftRequest(metaData);
		else if (action.equals("rotate-right"))
			host.getAppHost().getActionRequestListener().onRotateMetaDataRightRequest(metaData);
		else if (action.equals("mirror-hor"))
			host.getAppHost().getActionRequestListener().onHorizontalMirrorMetaDataRequest(metaData);
		else if (action.equals("mirror-ver"))
			host.getAppHost().getActionRequestListener().onVerticalMirrorMetaDataRequest(metaData);
		else return;
		repaint();
	}
	@Override public void onShow() {}
	@Override public void onHide() {}
	@Override public void onCloseRequest() {metaData.unloadMetaData();}
	
	@Override public boolean allowsSidePanel() {return true;}
}
