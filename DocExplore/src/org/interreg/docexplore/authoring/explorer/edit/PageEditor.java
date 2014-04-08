/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollPane;

import org.interreg.docexplore.authoring.explorer.DataLinkView.DropType;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.authoring.explorer.ViewMouseListener;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.gui.MainWindow.MainWindowListener;
import org.interreg.docexplore.management.image.PageViewer;
import org.interreg.docexplore.management.image.RegionOverlay.RegionObject;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.CropPageAction;
import org.interreg.docexplore.manuscript.actions.DeleteRegionsAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;

@SuppressWarnings("serial")
public class PageEditor extends PageViewer implements ViewMouseListener.DropTarget
{
	PageEditorToolbar toolBar;
	PageEditorView view;
	JScrollPane scrollPane;
	
	public PageEditor(final JScrollPane scrollPane, final PageEditorView view)
	{
		this.toolBar = new PageEditorToolbar(this, scrollPane);
		this.view = view;
		this.scrollPane = scrollPane;
		
		addListener(new PageViewer.Listener()
		{
			public void regionAdded(Page page, Point [] outline)
			{
				try
				{
					Region region = page.addRegion();
					region.setOutline(outline);
					view.explorer.regionsImported(view.curPage, Collections.singletonList(region));
					setDocument(region);
				} 
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public void regionRemoved(Region region)
			{
				try 
				{
					DeleteRegionsAction deleteRegionsAction = view.explorer.getActionProvider().deleteRegion(region);
					view.explorer.tool.historyManager.doAction(new WrappedAction(deleteRegionsAction)
					{
						public void doAction() throws Exception {super.doAction(); reloadPage();}
						public void undoAction() throws Exception {super.undoAction(); reloadPage();}
					});
				}
				catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public void pageCropped(Page page, int tlx, int tly, int brx, int bry)
			{
				try 
				{
					CropPageAction action = view.explorer.getActionProvider().cropPage(page, tlx, tly, brx, bry);
					view.explorer.tool.historyManager.doAction(new WrappedAction(action)
					{
						public void doAction() throws Exception {super.doAction(); reloadPage();}
						public void undoAction() throws Exception {super.undoAction(); reloadPage();}
					});
				}
				catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
			}
			public void objectSelected(AnnotatedObject object) {try {setDocument(object);} catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}}
			public void analysisRequested(BufferedImage image) {}
			public void regionAnnotationRequested(Region region) {}
			public void operationSet(ImageOperation operation) {}
		});
		
		msg = pageMsg;
	}
	
	static String pageMsg = XMLResourceBundle.getBundledString("helpPageMsg");
	static String regionMsg = XMLResourceBundle.getBundledString("helpRegionMsg");
	
	public void setDocument(AnnotatedObject document)
	{
		try
		{
			msg = document instanceof Page ? pageMsg : regionMsg;
			super.setDocument(document);
			view.explorer.notifyExploringChanged(document);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void reloadPage()
	{
		try
		{
			AnnotatedObject document = this.document;
			super.reload();
			setDocument(document);
			view.explorer.notifyExploringChanged(document);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void reload()
	{
		try
		{
			super.reload();
			//setDocument(view.curPage);
			view.explorer.notifyExploringChanged(document);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	List<MainWindowListener> listeners = new LinkedList<MainWindow.MainWindowListener>();
	public void addMainWindowListener(MainWindowListener listener) {listeners.add(listener);}
	
	@Override public void dropped(ExplorerView source, List<ViewItem.Data> items, Point where) {view.dropped(source, items, where); reloadPage();}
	@Override public void dragged(ExplorerView source, List<ViewItem.Data> items, Point where) {view.dragged(source, items, where); repaint();}
	@Override public void exited() {view.exited(); repaint();}
	
	@Override public void paint(Graphics _g)
	{
		super.paint(_g);
		Graphics2D g = (Graphics2D)_g;
		
		if (view.dragging != null)
		{
			if (view.dropType == DropType.OnItem)
			{
				Point where = toImage(view.dragging);
				RegionObject region = overlay.regionObjectAt(where);
				if (region == null)
					view.dropType = DropType.None;
				else
				{
					g.setColor(Color.red);
					Stroke old = g.getStroke();
					g.setStroke(view.vim.stroke);
					AffineTransform oldt = g.getTransform();
					g.transform(transform);
					
					g.draw(region.polygon);
					
					g.setStroke(old);
					g.setTransform(oldt);
				}
			}
			view.paintDropTarget(_g);
		}
	}
	
	public String msg = "";
	public void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		if (!view.explorer.tool.displayHelp || msg.length() == 0)
			return;
		Rectangle visible = scrollPane.getViewport().getViewRect();
		BufferedImage help = ExplorerView.helpRenderer.getImage(
			"<html><center><div style=\"font-family: Arial; font-size: 24; font-weight: bold; color: rgb(128, 128, 128)\">"+msg+"</div></center></html>", 
			visible.width, ExplorerView.background);
		g.drawImage(help, visible.x, visible.y+visible.height-help.getHeight(), null);
	}
}
