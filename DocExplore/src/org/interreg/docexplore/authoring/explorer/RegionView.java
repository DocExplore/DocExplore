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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.authoring.preview.PreviewPanel;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class RegionView extends DataLinkView
{

	public RegionView(DataLinkExplorer explorer)
	{
		super(explorer);
		
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
				SwingUtilities.convertPointToScreen(p, RegionView.this);
				MetaData md = (MetaData)(((ViewItem)comp).data.object);
				try
				{
					if (md.getType().equals(MetaData.textType))
						PreviewPanel.previewText(md.getString(), p.x, p.y);
					else if (md.getType().equals(MetaData.imageType))
						PreviewPanel.previewImage(md.getImage(), p.x, p.y);
					else RegionView.this.explorer.tool.createPreview(md, p.x, p.y);
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		});
	}

	@Override public boolean canHandle(String path) throws Exception
	{
		return AnnotatedObject.resolveUri(explorer.link, path) instanceof Region;
	}
	
	@Override protected void opened(ViewItem item) {}

	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		Region region = (Region)AnnotatedObject.resolveUri(explorer.link, path);
		explorer.notifyExploringChanged(region);
		
		Vector<ViewItem> res = new Vector<ViewItem>();
		for (Map.Entry<MetaDataKey, List<MetaData>> entry : region.getMetaData().entrySet())
			for (MetaData metaData : entry.getValue())
				if (metaData.getType().equals(MetaData.textType))
					res.add(new ViewItem(entry.getKey().getName(""), metaData.getString(), metaData));
				else res.add(new ViewItem(entry.getKey().getName(""), entry.getKey().getName(""), metaData));
		return res;
	}

	Icon textIcon = ImageUtils.getIcon("text-48x48.png");
	Icon errorIcon = ImageUtils.getIcon("page_search-48x48.png");
	@Override protected Icon getIcon(Object object)
	{
		MetaData md = (MetaData)object;
		if (md.getType().equals(MetaData.textType))
			return textIcon;
		try
		{
			if (md.getType().equals(MetaData.imageType))
				return ImageUtils.createIconFromImage(md.getImage(), 64);
			
			Icon icon = explorer.tool.getMetaDataIcon(md);
			if (icon != null)
				return icon;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return errorIcon;
	}
	
	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items) {return DropType.None;}
	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception {}
}
