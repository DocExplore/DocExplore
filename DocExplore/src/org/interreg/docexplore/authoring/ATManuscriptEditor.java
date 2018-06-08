package org.interreg.docexplore.authoring;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.actions.AddPagesAction;
import org.interreg.docexplore.manuscript.actions.RetroactiveAction;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.manuscript.app.editors.ManuscriptEditor;
import org.interreg.docexplore.manuscript.app.editors.DocumentPreviewEditor;
import org.interreg.docexplore.manuscript.app.editors.PosterPartsEditor;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.MemoryImageSource;
import org.interreg.docexplore.util.history.ReversibleAction;

@SuppressWarnings("serial")
public class ATManuscriptEditor extends ManuscriptEditor
{
	ATAppHost appHost;
	DocumentPreviewEditor previewEditor;
	
	public ATManuscriptEditor(final ATAppHost appHost, DocumentEditorHost host, Book book, Object param) throws DataLinkException
	{
		super(host, book, param);
		
		this.appHost = appHost;
		
		//topPanel.titlePanel.add(host.getAppHost().helpPanel.createHelpMessageButton(Lang.s("helpAtBookMsg")));
		topPanel.titlePanel.add(host.getAppHost().helpPanel.createHelpMessageButton(
				PosterUtils.isPoster(book) ? PosterUtils.isInStitches(book) ? 
					(PosterUtils.isInEditing(book) ? 
						Lang.s("helpPosterStitchEditMsg") 
						: PosterUtils.isInRendering(book) ? Lang.s("helpPosterStitchRenderMsg") : Lang.s("helpPosterStitchMsg"))
					: Lang.s("helpAtPosterMsg") 
					: Lang.s("helpAtBookMsg")));
		
		JButton editButton = new IconButton("pencil-24x24.png", Lang.s("manageRenameLabel"));
		topPanel.titlePanel.add(editButton);
		editButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent arg0)
		{
			GuiUtils.centerOnComponent(appHost.nameDialog, appHost.getFrame());
			appHost.nameDialog.showDialog();
		}});
		
		if (PosterUtils.isPoster(book) && !PosterUtils.isInStitches(book) && !PosterUtils.isInRendering(book))
		{
			JPanel previewPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.CENTER, SwingConstants.CENTER, false, false));
			previewPanel.setOpaque(false);
			JPanel previewLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			previewLabelPanel.setOpaque(false);
			previewLabelPanel.add(new JLabel("<html><b>"+Lang.s("generalPreview")+"</b></html>"));
			previewLabelPanel.add(appHost.helpPanel.createHelpMessageButton(Lang.s("helpPreview")));
			previewPanel.add(previewLabelPanel);
			previewPanel.add(this.previewEditor = new DocumentPreviewEditor(this, 128));
			topPanel.rightPanel.add(previewPanel);
			
			IconButton setPreview = new IconButton("camera-24x24.png", Lang.s("generalSetPreview"));
			((PosterPartsEditor)configurationEditor).previewButtons.add(setPreview);
			setPreview.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
			{
				BufferedImage image = ((PosterPartsEditor)configurationEditor).posterPreview.getImage();
				if (image == null)
					return;
				try
				{
					MetaData preview = new MetaData(appHost.getLink(), appHost.getLink().previewKey, MetaData.imageType, new MemoryImageSource(image).getFile());
					previewEditor.setPreview(preview);
				}
				catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
			}});
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override public void onActionRequest(final String action, final Object param) throws Exception
	{
		if (action.equals("up"))
			return;
		else if (action.equals(importPagesRequestEvent))
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
			{
				float progress = 0;
				@Override public void run()
				{
					try
					{
						final Page moveAfter = (Page)((Object [])param)[0];
						final List<Page> importPages = (List<Page>)((Object [])param)[1];
						final DocExploreDataLink sourceLink = (DocExploreDataLink)((Object [])param)[2];
						List<Page> addedPages = new ArrayList<>(importPages.size());
						
						if (!appHost.importOptions.showOptions(appHost.app, sourceLink, importPages))
							return;
						for (int i=0;i<importPages.size();i++)
						{
							addedPages.add(appHost.importer.add(importPages.get(i), book, (moveAfter == null ? 1 : moveAfter.pageNum+1)+i, appHost.importOptions));
							progress = i*1f/importPages.size();
						}
						
						AddPagesAction add = appHost.getLink().actionProvider().addPages(book, new ArrayList<File>(0));
						add.pages = addedPages;
						appHost.historyManager.submit(new RetroactiveAction(add)
						{
							@Override public void doAction() throws Exception
							{
								super.doAction();
								DocumentEvents.broadcastChanged(appHost, book);
							}
							@Override public void undoAction() throws Exception
							{
								super.undoAction();
								DocumentEvents.broadcastChanged(appHost, book);
							}
						});
						
					}
					catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
				}
				@Override public float getProgress() {return progress;}
			}, this);
		}
		else if (action.equals(importPartsRequestEvent) && ((Object [])param)[0] instanceof Book)
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable()
			{
				float progress = 0;
				@Override public void run()
				{
					try
					{
						if (!book.getMetaDataListForKey(appHost.getLink().partKey).isEmpty())
						{
							JOptionPane.showMessageDialog(ATManuscriptEditor.this, Lang.s("importEmptyPosterMessage"));
							return;
						} 
						final Book importedBook = (Book)((Object [])param)[0];
						final DocExploreDataLink sourceLink = (DocExploreDataLink)((Object [])param)[1];
						List<Page> pages = new ArrayList<>(1);
						pages.add(importedBook.getPage(1));
						if (!appHost.importOptions.showOptions(appHost.app, sourceLink, pages))
							return;
						
						MetaData [][] tiles = PosterUtils.getBaseTilesArray(sourceLink, importedBook);
						List<MetaData> sourceSet = new ArrayList<>();
						for (int i=0;i<tiles.length;i++)
							for (int j=0;j<tiles[i].length;j++)
								if (tiles[i][j] != null)
									sourceSet.add(appHost.importer.add(tiles[i][j], book, null));
						
						Page importedPage = importedBook.getPage(1);
						appHost.historyManager.submit(new ReversibleAction()
						{
							ReversibleAction addMetaData = new RetroactiveAction(appHost.getLink().actionProvider().addMetaDatas(book, sourceSet));
							@Override public void doAction() throws Exception
							{
								addMetaData.doAction();
								book.removePage(1);
								appHost.importer.add(importedPage, book, appHost.importOptions);
								DocumentEvents.broadcastChanged(appHost, book);
							}
							@Override public void undoAction() throws Exception
							{
								addMetaData.undoAction();
								book.removePage(1);
								book.appendPage(importedPage.getImage());
								DocumentEvents.broadcastChanged(appHost, book);
							}
							@Override public String description() {return Lang.s("importPoster");}
						});
					}
					catch (Throwable t) {ErrorHandler.defaultHandler.submit(t);}
				}
				@Override public float getProgress() {return progress;}
			}, this);
		}
		else super.onActionRequest(action, param);
	}
	
	@Override public void refresh()
	{
		super.refresh();
		if (previewEditor != null)
			previewEditor.refresh();
	}
}
