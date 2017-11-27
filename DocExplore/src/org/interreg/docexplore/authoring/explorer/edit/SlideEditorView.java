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

import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import org.interreg.docexplore.authoring.explorer.DataLinkExplorer;
import org.interreg.docexplore.authoring.explorer.DataLinkView;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.PageView;
import org.interreg.docexplore.authoring.explorer.RegionView;
import org.interreg.docexplore.authoring.explorer.ViewItem;
import org.interreg.docexplore.authoring.explorer.file.FolderView;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

@SuppressWarnings("serial")
public class SlideEditorView extends DataLinkView
{
	public SlideEditor editor;
	
	public SlideEditorView(final DataLinkExplorer explorer)
	{
		super(explorer);
		try {this.editor = new SlideEditor(this);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	@Override public Component getViewComponent() {return editor;}
	
	public void shown()
	{
		explorer.toolPanel.add(editor.toolBar); 
		editor.toolBar.revalidate();
		editor.toolBar.repaint();
	}
	public void hidden()
	{
		explorer.toolPanel.remove(editor.toolBar);
		explorer.toolPanel.revalidate();
		explorer.toolPanel.repaint();
	}

	@Override public boolean canHandle(String path) throws Exception {return AnnotatedObject.resolveUri(explorer.link, path) instanceof Page;}
	Page curPage = null;
	@Override protected List<ViewItem> buildItemList(String path) throws Exception
	{
		curPage = (Page)AnnotatedObject.resolveUri(explorer.link, path);
		editor.switchDocument(curPage);
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
			Region region = editor.getOverlay().regionAt(editor.toViewX(where.x), editor.toViewY(where.y));
			if (region == null)
				return;
			List<MetaData> annotations = new LinkedList<MetaData>();
			for (ViewItem.Data item : items)
				if (item.object instanceof File && !((File)item.object).isDirectory())
			{
				MetaData md = MetaDataUtils.importFile(explorer.tool, region, (File)item.object);
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
			Region region = editor.getOverlay().regionAt(editor.toViewX(where.x), editor.toViewY(where.y));
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
			Region region = editor.getOverlay().regionAt(editor.toViewX(where.x), editor.toViewY(where.y));
			if (region == null)
				return;
			List<MetaData> annotations = new LinkedList<MetaData>();
			for (ViewItem.Data item : items)
				if (item.object instanceof File && !((File)item.object).isDirectory())
			{
				MetaData md = MetaDataUtils.importFile(explorer.tool, region, (File)item.object);
				if (md == null)
					continue;
				annotations.add(md);
			}
			explorer.metaDataImported(region, annotations);
		}
	}
	
	
}
