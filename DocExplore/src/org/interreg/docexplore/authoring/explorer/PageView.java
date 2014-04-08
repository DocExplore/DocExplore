/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

@SuppressWarnings("serial")
public class PageView extends DataLinkView
{
	MetaDataKey miniKey;
	
	public PageView(DataLinkExplorer explorer) throws Exception
	{
		super(explorer);
		miniKey = explorer.link.getKey("mini", "");
		
		addMouseListener(new MouseAdapter()
		{
			@Override public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON3)
					return;
				Component comp = getComponentAt(e.getPoint());
				if (comp == null || !(comp instanceof ViewItem))
					return;
				Point p = new Point(e.getPoint());
				SwingUtilities.convertPointToScreen(p, PageView.this);
				PreviewPanel.previewRegion((Region)((ViewItem)comp).data.object, p.x, p.y);
			}
		});
	}

	@Override public boolean canHandle(String path) throws Exception
	{
		return AnnotatedObject.resolveUri(explorer.link, path) instanceof Page;
	}

	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		Page page = (Page)AnnotatedObject.resolveUri(explorer.link, path);
		explorer.notifyExploringChanged(page);
		
		Set<Region> regions = page.getRegions();
		Vector<ViewItem> res = new Vector<ViewItem>();
		for (Region region : regions)
			res.add(new ViewItem("Region #"+region.getId(), "", region));
		return res;
	}

	public Icon buildIcon(Region region) throws Exception
	{
		Page page = region.getPage();
		BufferedImage mini = page.getMetaDataListForKey(miniKey).get(0).getImage();
		BufferedImage res = new BufferedImage(mini.getWidth(), mini.getHeight(), BufferedImage.TYPE_INT_RGB);
		Dimension dim = DocExploreDataLink.getImageDimension(page);
		
		Graphics2D g = res.createGraphics();
		g.drawImage(mini, 0, 0, null);
		
		Point [] outline = region.getOutline();
		Point last = outline[outline.length-1];
		g.setColor(Color.red);
		for (int i=0;i<outline.length;i++)
		{
			g.drawLine(last.x*mini.getWidth()/dim.width, last.y*mini.getHeight()/dim.height, 
				outline[i].x*mini.getWidth()/dim.width, outline[i].y*mini.getHeight()/dim.height);
			last = outline[i];
		}
		return new ImageIcon(res);
	}

	@Override protected Icon getIcon(Object object)
	{
		try {return buildIcon((Region)object);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return null;
	}
	
	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items) {return DropType.None;}
	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception {}
}
