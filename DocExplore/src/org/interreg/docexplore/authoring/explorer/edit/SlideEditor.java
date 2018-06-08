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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;

import org.interreg.docexplore.authoring.explorer.DataLinkView.DropType;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.authoring.explorer.ViewMouseListener;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost.AppListener;
import org.interreg.docexplore.manuscript.app.editors.PosterPageEditor;
import org.interreg.docexplore.manuscript.app.editors.RegionOverlay.RegionObject;

@SuppressWarnings("serial")
public class SlideEditor extends PosterPageEditor implements ViewMouseListener.DropTarget
{
	SlideEditorListener listener;
	SlideEditorToolbar toolBar;
	SlideEditorView view;
	
	public SlideEditor(final SlideEditorView view) throws Exception
	{
		super(new SlideEditorListener(view), (Page)null);
		
		this.listener = (SlideEditorListener)getHost();
		((SlideEditorListener)getHost()).editor = this;
		this.toolBar = new SlideEditorToolbar(this);
		this.view = view;
		
		ViewMouseListener.makeFileSystemDropTarget(this);
		
		setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		addComponentListener(new ComponentAdapter()
			{@Override public void componentResized(ComponentEvent e) {revalidate(); repaint();}});
		msg = pageMsg;
	}
	
	static String pageMsg = Lang.s("helpPageMsg");
	static String regionMsg = Lang.s("helpRegionMsg");
	
	@Override public void switchDocument(final AnnotatedObject document) throws Exception
	{
		super.switchDocument(document);
		view.explorer.notifyExploringChanged(document);
		refresh();
	}
	
	public void reloadPage()
	{
		try
		{
			AnnotatedObject document = getDocument();
			super.refresh();
			switchDocument(document);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	List<AppListener> listeners = new LinkedList<AppListener>();
	public void addMainWindowListener(AppListener listener) {listeners.add(listener);}
	
	@Override public void dropped(ExplorerView source, List<ViewItem.Data> items, Point where) {view.dropped(source, items, where); reloadPage();}
	@Override public void dragged(ExplorerView source, List<ViewItem.Data> items, Point where) {view.dragged(source, items, where); repaint();}
	@Override public void exited() {view.exited(); repaint();}
	
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		super.drawView(g, pixelSize);
		
		if (view.dragging != null)
		{
			if (view.dropType == DropType.OnItem)
			{
				RegionObject region = getOverlay().regionObjectAt(toViewX(view.dragging.x), toViewY(view.dragging.y));
				if (region == null)
					view.dropType = DropType.None;
				else
				{
					g.setColor(Color.red);
					g.draw(region.polygon);
				}
			}
			g.setTransform(defaultTransform);
			view.paintDropTarget(g);
			g.setTransform(viewTransform);
		}
		
		if (view.explorer.tool.displayHelp && !renderedMsg.equals(msg))
		{
			help = ExplorerView.helpRenderer.getImage(
				"<html><div style=\"font-family: Arial; font-size: 24; font-weight: bold; color: rgb(128, 128, 128)\">"+msg+"</div></html>", 
				getWidth(), ExplorerView.background);
			renderedMsg = msg;
		}
		if (view.explorer.tool.displayHelp && msg.length() > 0)
		{
			g.setTransform(defaultTransform);
			g.drawImage(help, 0, getHeight()-help.getHeight(), null);
		}
	}
	BufferedImage help = null;
	public String msg = "", renderedMsg = "";
}
