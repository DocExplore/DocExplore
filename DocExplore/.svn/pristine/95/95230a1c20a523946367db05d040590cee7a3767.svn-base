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
