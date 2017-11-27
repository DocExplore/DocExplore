package org.interreg.docexplore.management.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.management.gui.DocumentEditor;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.Pair;

public class PageEditor extends ImageView implements DocumentEditor
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
	protected PageEditor(DocumentEditorHost host, Page page, Region region) throws Exception
	{
		super(new SelectionOperation());
		
		this.host = host;
		this.page = page;
		this.region = region;
		this.regions = new RegionOverlay(this);
		if (page != null)
		{
			regions.setPage(page, region);
			setImage(page.getImage().getImage());
		}
		setBackground(Color.white);
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
		host.onDocumentSwitched(document);
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
		Pair<Page, Point> pair = BookEditor.decode(page.getBook(), s);
		setOperation(new BeaconOperation(pair.second, 5000, 500));
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
	
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		super.drawView(g, pixelSize);
		drawOverlay(g, pixelSize);
	}
	
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
	@Override public void onActionRequest(String action) throws Exception
	{
		if (page == null)
			return;
		if (action.equals("up"))
		{
			host.onDocumentEditorRequest(page.getBook());
			host.onCloseRequest();
		}
		else if (action.equals("prev"))
			switchDocument(page.getBook().getPage(page.getPreviousPageNumber()));
		else if (action.equals("next"))
			switchDocument(page.getBook().getPage(page.getNextPageNumber()));
		else if (action.equals("fit"))
			fit();
		else if (action.equals("remove-roi"))
		{
			Region region = this.region;
			switchDocument(page);
			host.getActionListener().onDeleteRegionRequest(region);
		}
	}
	@Override public void onActionStateRequest(String action, boolean state) throws Exception
	{
		if (page == null)
			return;
		if (action.equals("add-rect-roi"))
		{
			if (!state) cancelOperation();
			else setOperation(new RectROIOperation());
		}
		else if (action.equals("add-free-roi"))
		{
			if (!state) cancelOperation();
			else setOperation(new FreeShapeROIOperation());
		}
		else if (action.equals("crop"))
		{
			if (!state) cancelOperation();
			else setOperation(new CropOperation());
		}
		else return;
		repaint();
	}
	@Override public void onClose()
	{
		if (page == null)
			return;
		if (region != null)
			region.unloadMetaData();
		page.unloadMetaData();
		page.unloadRegions();
		page.unloadImage();
	}
}
