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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.authoring.AuthoringToolFrame;
import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.authoring.explorer.edit.PresentationEditorView;
import org.interreg.docexplore.authoring.explorer.edit.CollectionEditorView;
import org.interreg.docexplore.authoring.explorer.edit.SlideEditorView;
import org.interreg.docexplore.datalink.fs2.FS2ActionProvider;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.actions.ActionProvider;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.AddRegionsAction;
import org.interreg.docexplore.manuscript.actions.WrappedAction;

@SuppressWarnings("serial")
public class DataLinkExplorer extends Explorer
{
	public DocExploreDataLink link;
	public BookImporter importer;
	ActionProvider actionProvider;
	
	PresentationEditorView bev = null;
	SlideEditorView pev = null;
	
	public String pageTerm = "Page";
	public String partTerm = "Part";
	
	public DataLinkExplorer(AuthoringToolFrame tool, DocExploreDataLink link, BookImporter importer) throws Exception
	{
		super(tool);
		
		this.link = link;
		if (importer != null)
		{
			this.importer = importer;
			addView(new CollectionEditorView(this));
			addView(bev = new PresentationEditorView(this));
			addView(pev = new SlideEditorView(this));
			pageTerm = "Slide";
			reset();
		}
		else
		{
			addView(new CollectionView(this));
			BookView bv = new BookView(this);
			//bv.setFilter(tool.filter);
			addView(bv);
			addView(new PageView(this));
			addView(new RegionView(this));
			explore("docex://");
		}
		
		pathField.setEditable(false);
		this.actionProvider = new FS2ActionProvider(link);
		link.notifyDataLinkChanged();
	}
	
	public ActionProvider getActionProvider() {return actionProvider;}
	
	public void reset() throws Exception
	{
		List<Integer> books = link.getLink().getAllBookIds();
		if (books.isEmpty())
		{
			Book book = new Book(link, XMLResourceBundle.getBundledString("collectionDefaultBookLabel"));
			books = new LinkedList<Integer>();
			books.add(book.getId());
		}
		explore("docex://"+books.get(0));
		link.notifyDataLinkChanged();
	}
	
	public String getParentPath(String path)
	{
		String sub = path.substring("docex://".length());
		int ind = sub.lastIndexOf('/');
		if (importer != null && ind < 0)
			return path;
		return "docex://"+(ind >= 0 ? sub.substring(0, ind) : "");
	}
	
	public void regionsImported(Page page, Collection<Region> regions)
	{
		if (pev == null)
			return;
		try
		{
			final AddRegionsAction action = getActionProvider().addRegions(page, null);
			tool.historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception
				{
					action.cacheDir = cacheDir;
					if (action.regions.isEmpty()) 
						return; 
					super.doAction(); 
					pev.editor.reloadPage();
				}
				public void undoAction() throws Exception {super.undoAction(); pev.editor.reloadPage();}
			});
			action.regions.addAll(regions);
		}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	public void metaDataImported(final Region region, Collection<MetaData> annotations)
	{
		if (pev == null)
			return;
		try
		{
			final AddMetaDataAction action = getActionProvider().addMetaDatas(region, new LinkedList<MetaData>());
			tool.historyManager.submit(new WrappedAction(action)
			{
				public void doAction() throws Exception
				{
					action.cacheDir = cacheDir;
					if (action.annotations.isEmpty()) 
						return; 
					super.doAction();
					if (tool.mdEditor.document == region)
						tool.mdEditor.reload();
				}
				public void undoAction() throws Exception
				{
					super.undoAction();
					if (tool.mdEditor.document == region)
						tool.mdEditor.reload();
				}
			});
			action.annotations.addAll(annotations);
		}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
	
	String pathToString(String path)
	{
		String res = "";
		if (path.equals("docex://"))
			return "";
		try
		{
			String suf = path.substring("docex://".length());
			int ind = suf.indexOf('/');
			ind = ind < 0 ? suf.length() : ind;
			int bookId = Integer.parseInt(suf.substring(0, ind));
			Book data = link.getBook(bookId);
			res = data.getName();
			if (ind < suf.length())
			{
				suf = suf.substring(ind+2);
				ind = suf.indexOf('/');
				ind = ind < 0 ? suf.length() : ind;
				int page = Integer.parseInt(suf.substring(0, ind));
				res += " - "+pageTerm+" "+page;
				
				if (ind < suf.length())
				{
					suf = suf.substring(ind+1);
					int regionId = Integer.parseInt(suf);
					res += " - Region #"+regionId;
				}
			}
		}
		catch (Exception e) {e.printStackTrace();}
		return res;
	}
}
