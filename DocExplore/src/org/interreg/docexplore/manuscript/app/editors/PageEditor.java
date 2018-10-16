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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.EditorView;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.util.Pair;

public class PageEditor extends ImageView implements DocumentPanelEditor
{
	private static final long serialVersionUID = 6910768551955062736L;
	
	protected DocumentEditorHost host;
	Page page;
	Region region;
	RegionOverlay regions;
	
	public PageEditor(DocumentEditorHost listener, Page page) throws Exception
	{
		this(listener, page, null);
	}
	public PageEditor(DocumentEditorHost listener, Region region) throws Exception
	{
		this(listener, region.getPage(), region);
	}
	@SuppressWarnings("serial")
	protected PageEditor(DocumentEditorHost host, Page page, Region region) throws Exception
	{
		super(new SelectionOperation());
		
		this.host = host;
		this.page = page;
		this.region = region;
		this.regions = new RegionOverlay(this);
		if (page != null)
		{
			if (PosterUtils.isPoster(page.getBook()) && !PosterUtils.isUpToDate(page.getBook()))
				PosterUtils.buildConfiguration(page.getBook(), host.getAppHost().getLink(), this);
			regions.setPage(page, region);
			setImage(page.getImage().getImage());
		}
		setBackground(Color.white);
		setFocusable(true);
		requestFocus();
		
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			try {onActionRequest("delete", null);}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
		getActionMap().put("ESC", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			cancelOperation();
		}});
	}
	
	public DocumentEditorHost getHost() {return host;}
	public Page getPage() {return page;}
	public Region getRegion() {return region;}
	public AnnotatedObject getDocument() {return region != null ? region : page;}
	public RegionOverlay getOverlay() {return regions;}
	
	public void switchDocument(AnnotatedObject document) throws Exception
	{
		if (page != null && document.getId() == (region != null ? region : page).getId())
			return;
		host.switchDocument(document);
		int oldPageId = this.page !=null ? this.page.getId() : -1;
		this.region = document instanceof Region ? (Region)document : null;
		this.page = region != null ? region.getPage() : (Page)document;
		regions.setPage(page, region);
		setImage(page.getImage().getImage(), oldPageId != page.getId());
	}
	
	public void goTo(String s) throws Exception
	{
		if (page == null)
			return;
		Pair<Page, Point> pair = ManuscriptEditor.decode(page.getBook(), s);
		setOperation(new BeaconOperation(pair.second, 5000, 500));
	}
	
	@Override public void setOperation(Operation<? extends EditorView> operation)
	{
		super.setOperation(operation);
		requestFocus();
	}
	@Override public void cancelOperation()
	{
		super.cancelOperation();
		host.onActionStateRequestCompleted();
		repaint();
	}
	@Override protected void onMessageChanged(String s)
	{
		host.setMessage(s);
	}
	
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		super.drawView(g, pixelSize);
		drawOverlay(g, pixelSize);
		super.drawMini(g, pixelSize);
	}
	@Override protected void drawMini(Graphics2D g, double pixelSize) {}
	protected void drawOverlay(Graphics2D g, double pixelSize)
	{
		regions.render(g, pixelSize);
	}
	
	@Override public Component getComponent() {return this;}
	@Override public void refresh()
	{
		if (page == null)
			return;
		try
		{
			page.unloadImage();
			setImage(page.getImage().getImage(), false);
			regions.setPage(page, true);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	@Override public void onActionRequest(String action, Object param) throws Exception
	{
		if (page == null)
			return;
		if (action.equals("up"))
		{
			host.onCloseRequest();
			host.onDocumentEditorRequest(page.getBook());
		}
		else if (action.equals("prev"))
			switchDocument(page.getBook().getPage(page.getPreviousPageNumber()));
		else if (action.equals("next"))
			switchDocument(page.getBook().getPage(page.getNextPageNumber()));
		else if (action.equals("goto"))
			goTo((String)param);
		else if (action.equals("fit"))
			fit();
		else if (action.equals("delete"))
		{
			if (region != null)
				host.getAppHost().broadcastAction(DocumentEvents.deleteRegion.event, region);
		}
		else if (action.equals("add-rect-roi"))
		{
			if (!(Boolean)param) cancelOperation();
			else setOperation(new RectROIOperation());
		}
		else if (action.equals("add-free-roi"))
		{
			if (!(Boolean)param) cancelOperation();
			else setOperation(new FreeShapeROIOperation());
		}
		else if (action.equals("crop"))
		{
			if (!(Boolean)param) cancelOperation();
			else setOperation(new CropOperation());
		}
		else if (action.equals("rotate-left"))
			host.getAppHost().getActionRequestListener().onRotatePageLeftRequest(page);
		else if (action.equals("rotate-right"))
			host.getAppHost().getActionRequestListener().onRotatePageRightRequest(page);
		else if (action.equals("mirror-hor"))
			host.getAppHost().getActionRequestListener().onHorizontalMirrorPageRequest(page);
		else if (action.equals("mirror-ver"))
			host.getAppHost().getActionRequestListener().onVerticalMirrorPageRequest(page);
		else return;
		repaint();
	}
	@Override public void onShow() {}
	@Override public void onHide() {}
	@Override public void onCloseRequest()
	{
		if (page == null)
			return;
		if (region != null)
			region.unloadMetaData();
		page.unloadMetaData();
		page.unloadRegions();
		page.unloadImage();
	}
	@Override public boolean allowsSidePanel() {return true;}
}
