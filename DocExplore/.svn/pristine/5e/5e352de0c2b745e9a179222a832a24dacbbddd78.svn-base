package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.authoring.explorer.DataLinkExplorer;
import org.interreg.docexplore.authoring.explorer.DataLinkView;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.PageView;
import org.interreg.docexplore.authoring.explorer.RegionView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.authoring.explorer.ViewMouseListener;
import org.interreg.docexplore.authoring.explorer.file.FolderView;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.plugin.metadata.MetaDataPlugin;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.FileImageSource;

@SuppressWarnings("serial")
public class PageEditorView extends DataLinkView
{
	public PageEditor editor;
	
	public PageEditorView(final DataLinkExplorer explorer)
	{
		super(explorer);
		this.editor = new PageEditor(explorer.scrollPane, this);
	}
	
	public void shown()
	{
		explorer.toolPanel.add(editor.toolBar); 
		explorer.setCustomView(editor);
		ViewMouseListener.makeFileSystemDropTarget(editor);
		explorer.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		try {editor.setDocument(curPage);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		explorer.validate();
	}
	public void hidden()
	{
		explorer.toolPanel.remove(editor.toolBar);
		explorer.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		explorer.validate();
	}

	@Override public boolean canHandle(String path) throws Exception {return AnnotatedObject.resolveUri(explorer.link, path) instanceof Page;}
	Page curPage = null;
	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		curPage = (Page)AnnotatedObject.resolveUri(explorer.link, path);
		return new Vector<ViewItem>();
	}
	@Override protected Icon getIcon(Object object) {return null;}

	@Override public DropType getDropType(ExplorerView source, List<ViewItem.Data> items)
	{
		if (source == null)
			return DropType.OnItem;
		if (source instanceof PageView)
			return DropType.Anywhere;
		if (source instanceof RegionView)
			return DropType.OnItem;
		if (source instanceof FolderView)
			return DropType.OnItem;
		return DropType.None;
	}
	
	@Override public void itemsDropped(ExplorerView source, List<ViewItem.Data> items, Point where) throws Exception
	{
		if (source == null)
		{
			where = editor.toImage(where);
			Region region = editor.overlay.regionAt(where);
			if (region == null)
				return;
			List<MetaData> annotations = new LinkedList<MetaData>();
			for (ViewItem.Data item : items)
				if (item.object instanceof File && !((File)item.object).isDirectory())
			{
				MetaData md = importFile(region, (File)item.object);
				if (md == null)
					continue;
				annotations.add(md);
			}
			explorer.metaDataImported(region, annotations);
		}
		else if (source instanceof PageView)
		{
			ImportOptions importOptions = explorer.tool.importOptions;
			List<Region> sourceSet = new LinkedList<Region>();
			for (ViewItem.Data item : items)
				if (item.object instanceof Region)
					sourceSet.add((Region)item.object);
			if (!importOptions.showOptionsForRegions(explorer.tool, ((PageView)source).explorer.link, sourceSet))
				return;
			
			List<Region> regions = new LinkedList<Region>();
			for (Region region : sourceSet)
				regions.add(explorer.importer.add(region, curPage, importOptions));
			
			explorer.regionsImported(curPage, regions);
		}
		else if (source instanceof RegionView)
		{
			where = editor.toImage(where);
			Region region = editor.overlay.regionAt(where);
			if (region == null)
				return;
			List<MetaData> annotations = new LinkedList<MetaData>();
			for (ViewItem.Data item : items)
				if (item.object instanceof MetaData)
					annotations.add(explorer.importer.add((MetaData)item.object, region, null));//explorer.tool.filter));
			explorer.metaDataImported(region, annotations);
			
		}
		else if (source instanceof FolderView)
		{
			where = editor.toImage(where);
			Region region = editor.overlay.regionAt(where);
			if (region == null)
				return;
			List<MetaData> annotations = new LinkedList<MetaData>();
			for (ViewItem.Data item : items)
				if (item.object instanceof File && !((File)item.object).isDirectory())
			{
				MetaData md = importFile(region, (File)item.object);
				if (md == null)
					continue;
				annotations.add(md);
			}
			explorer.metaDataImported(region, annotations);
		}
	}
	
	MetaData importFile(Region region, File file) throws Exception
	{
		MetaData md = null;
		boolean handledByPlugin = false;
		for (MetaDataPlugin plugin : explorer.tool.plugins)
			if (plugin.canPreview(file))
		{
			handledByPlugin = true;
			md = new MetaData(region.getLink(), BookImporter.getDisplayKey(region.getLink()), plugin.getType(), new FileInputStream(file));
			break;
		}
		if (!handledByPlugin)
		{
			FileImageSource image = new FileImageSource(file);
			if (!image.isValid())
				return null;
			md = new MetaData(region.getLink(), BookImporter.getDisplayKey(region.getLink()), MetaData.imageType, image.getFile());
		}
		
		region.addMetaData(md);
		BookImporter.setRank(md, BookImporter.getHighestRank(region)+1);
		return md;
	}
}
