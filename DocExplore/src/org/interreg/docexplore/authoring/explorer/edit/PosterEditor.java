package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.authoring.explorer.CollectionView;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem.Data;
import org.interreg.docexplore.authoring.explorer.ViewMouseListener;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.image.PosterPartsEditor;
import org.interreg.docexplore.management.image.PosterUtils;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;
import org.interreg.docexplore.manuscript.actions.AddPosterPartsAction;
import org.interreg.docexplore.util.history.ReversibleAction;

@SuppressWarnings("serial")
public class PosterEditor extends PosterPartsEditor implements ViewMouseListener.DropTarget
{
	PresentationEditorListener listener;
	PresentationEditorView view;
	
	public PosterEditor(PresentationEditorView view, Book book) throws DataLinkException
	{
		super(new PresentationEditorListener(view), book);
		
		this.listener = (PresentationEditorListener)host;
		((PresentationEditorListener)host).editor = this;
		this.view = view;
	}

	@Override public void dropped(ExplorerView source, List<Data> items, Point where)
	{
		try
		{
			List<MetaData> initialParts = new LinkedList<MetaData>();
			List<MetaData> addedParts = new LinkedList<MetaData>();
			if (source == null)
			{
				
			}
			else if (source instanceof CollectionView)
			{
				for (Data data : items)
				{
					if (data.object instanceof Book)
					{
						Book book = (Book)data.object;
						boolean isPoster = PosterUtils.isPoster(book);
						boolean isEmpty = view.curBook.getMetaDataListForKey(view.explorer.link.partKey).isEmpty();
						if (isPoster)
						{
							//ImportOptions importOptions = view.explorer.tool.importOptions;
							List<MetaData> sourceSet = book.getMetaDataListForKey(book.getLink().getOrCreateKey("part", ""));
							for (MetaData part : sourceSet)
								(isEmpty ? initialParts : addedParts).add(view.explorer.importer.add(part, view.curBook.getLink(), null));
						}
					}
				}
			}
			submitPastAddPartsAction(initialParts, addedParts);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		repaint();
	}

	@Override public void dragged(ExplorerView source, List<Data> items, Point where)
	{
		view.dragged(source, items, where);
		repaint();
	}

	@Override public void exited()
	{
		view.exited();
	}
	
	@Override protected void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		view.paintDropTarget(g);
	}
	
	/**
	 * Submits an add poster action to the history manager.
	 * Assumes that the parts are already added to the book and will skip the first "do" ("undo" works nonetheless).
	 * @param initialParts These parts keep their original position data (doesn't check if slots are truly empty).
	 * @param addedParts These parts are appended on an extra row at the bottom.
	 */
	void submitPastAddPartsAction(final List<MetaData> initialParts, final List<MetaData> addedParts)
	{
		if (initialParts.isEmpty() && addedParts.isEmpty())
			return;
		try
		{
			for (MetaData part : initialParts)
				view.curBook.addMetaData(part);
			if (!addedParts.isEmpty())
			{
				MetaData [][] parts = PosterUtils.getPosterPartsArray(view.explorer.link, view.curBook);
				int x = 0;
				for (MetaData part : addedParts)
				{
					view.curBook.addMetaData(part);
					PosterUtils.setPartPos(view.explorer.link, part, x++, parts.length == 0 ? 0 : parts[0].length);
				}
			}
			
			final AddMetaDataAction initial = initialParts.isEmpty() ? null : view.explorer.getActionProvider().addMetaDatas(view.curBook, null);
			final AddPosterPartsAction added = addedParts.isEmpty() ? null : view.explorer.getActionProvider().addParts(view.curBook, null);
			view.explorer.getActionProvider().addParts(view.curBook, null);
			view.explorer.tool.historyManager.submit(new ReversibleAction()
			{
				@Override public void doAction() throws Exception
				{
					if (initial != null)
					{
						initial.cacheDir = cacheDir;
						if (initial.annotations != null) 
							initial.doAction(); 
					}
					if (added != null)
					{
						added.cacheDir = cacheDir;
						if (!added.parts.isEmpty())
							added.doAction();
					}
					view.explorer.explore("docex://"+view.curBook.getId());
				}
				@Override public void undoAction() throws Exception
				{
					if (added != null)
						added.undoAction();
					if (initial != null)
						initial.undoAction();
					view.explorer.explore("docex://"+view.curBook.getId());
				}
				@Override public String description() {return "Add parts";}
			});
			if (initial != null)
				initial.annotations = initialParts;
			if (added != null)
				added.parts.addAll(addedParts);
		}
		catch (Throwable ex) {ErrorHandler.defaultHandler.submit(ex);}
	}
}
