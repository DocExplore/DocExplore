/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.StitcherUtils;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentPanel;
import org.interreg.docexplore.manuscript.app.DocumentPanelEditor;
import org.interreg.docexplore.stitcher.Fragment;
import org.interreg.docexplore.stitcher.FragmentView;
import org.interreg.docexplore.stitcher.RenderEditor;
import org.interreg.docexplore.stitcher.StitchEditor;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

@SuppressWarnings("serial")
public class ManuscriptEditor extends JPanel implements DocumentPanelEditor, FragmentView.Listener, RenderEditor.Listener, StitchEditor.Listener
{
	public static final String importFilesRequestEvent = "bookeditor-import-files";
	public static final String importPagesRequestEvent = "bookeditor-import-pages";
	public static final String movePagesRequestEvent = "bookeditor-move-pages";
	public static final String importPartsRequestEvent = "bookeditor-import-parts";
	
	public final DocumentEditorHost host;
	public final ConfigurationEditor configurationEditor;
	public final Book book;
	protected boolean canOpenPages = true;
	
	public final EditorHeader topPanel;
	private boolean sidePanel = true;
	
	public ManuscriptEditor(final DocumentEditorHost host, final Book book, Object param) throws DataLinkException
	{
		super(new BorderLayout());
		
		this.host = host;
		this.book = book;
		this.topPanel = new EditorHeader();
		
		if (PosterUtils.isPoster(book))
		{
			if (PosterUtils.isInStitches(book))
			{
				sidePanel = false;
				FragmentView view = null;
				if (param != null && param instanceof FragmentView)
				{
					view = (FragmentView)param;
					view.setListener(this);
				}
				else
				{
					view = new FragmentView(this);
					StitcherUtils.loadStitches(this, view, book, host.getAppHost().getLink());
				}
				if (PosterUtils.isInRendering(book))
					this.configurationEditor = StitcherUtils.buildRenderEditor(this, view);
				else if (PosterUtils.isInEditing(book))
					this.configurationEditor = StitcherUtils.buildStitchEditor(this, view);
				else
				{
					this.configurationEditor = view;
					StitcherUtils.integrateFragmentView(this, view);
				}
			}
			else this.configurationEditor = new PosterPartsEditor(host, book);
		}
		else this.configurationEditor = new BookEditor(this);
		add(configurationEditor.getComponent(), BorderLayout.CENTER);
		
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			configurationEditor.onActionRequest("delete", null);
		}});
		
		refreshTitle();
		
		if (configurationEditor.allowGoto())
		{
			JPanel gotoPanel = new JPanel(new FlowLayout());
			gotoPanel.setOpaque(false);
			gotoPanel.add(new JLabel(Lang.s("imageGotoLabel")));
			final JTextField goField = new JTextField(7);
			goField.addKeyListener(new KeyAdapter()
			{
				public void keyReleased(KeyEvent event)
				{
					if (event.getKeyCode() == KeyEvent.VK_ENTER)
						try {goTo(goField.getText());}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				}
			});
			gotoPanel.add(goField);
			topPanel.rightPanel.add(gotoPanel);
		}
		add(topPanel, BorderLayout.NORTH);
	}
	
	public static Book findTitle(DocExploreDataLink link, String title) throws DataLinkException
	{
		List<Integer> bookIds = link.getLink().getAllBookIds();
		for (int bookId : bookIds)
			if (title.equals(link.getLink().getBookTitle(bookId)))
				return link.getBook(bookId);
		return null;
	}
	
	protected void refreshTitle()
	{
		try
		{
			boolean isPoster = PosterUtils.isPoster(book);
			int nPages = book.getLastPageNumber();
			topPanel.setTitle(book.getName(), (isPoster ? "" : nPages+" "+Lang.s(nPages == 1 ? "generalPage" : "generalPages"))); 
			topPanel.setTitleIcon(ImageUtils.getIcon(isPoster ? "scroll-64x64.png" : "book-64x64.png"));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	@Override public void refresh()
	{
		refreshTitle();
		configurationEditor.refresh();
	}
	
	public void goTo(String s) throws Exception
	{
		s = s.toLowerCase();
		if (s.length() < 7)
			host.onDocumentEditorRequest(book.getPage(Integer.parseInt(s)));
		else
		{
			Pair<Page, Point> pair = decode(book, s);
			DocumentPanel panel = host.onDocumentEditorRequest(pair.first);
			if (panel != null)
				panel.onActionRequest("goto", s);
		}
	}
	
	public static String encode(Page page, int x, int y) throws DataLinkException
	{
		String pageNum = Integer.toString(page.getPageNumber(), 36);
		while (pageNum.length() < 3)
			pageNum = '0'+pageNum;
		
		Dimension dim = DocExploreDataLink.getImageDimension(page);
		double xc = x*1./dim.width;
		double yc = y*1./dim.height;
		String xs = Integer.toString((int)(xc*(36*36-1)), 36);
		while (xs.length() < 2)
			xs = '0'+xs; 
		String ys = Integer.toString((int)(yc*(36*36-1)), 36);
		while (ys.length() < 2)
			ys = '0'+ys;
		return (pageNum+xs+ys);
	}
	
	public static Pair<Page, Point> decode(Book book, String s) throws Exception
	{
		int pageNum = Integer.parseInt(s.substring(0, 3), 36);
		Page page = book.getPage(pageNum);
		Dimension dim = DocExploreDataLink.getImageDimension(page);
		int x = (int)(dim.width*Integer.parseInt(s.substring(3, 5), 36)*1./(36*36-1));
		int y = (int)(dim.height*Integer.parseInt(s.substring(5, 7), 36)*1./(36*36-1));
		return new Pair<Page, Point>(page, new Point(x, y));
	}
	
	@Override public Component getComponent() {return this;}
	public void onActionRequest(String action) throws Exception {onActionRequest(action, null);}
	@SuppressWarnings("unchecked")
	@Override public void onActionRequest(String action, Object param) throws Exception
	{
		if (action.equals("goto"))
			goTo((String)param);
		else if (action.equals("up"))
		{
			host.onCloseRequest();
			host.onDocumentEditorRequest(null);
		}
		else if (action.equals(importFilesRequestEvent))
		{
			if (!PosterUtils.isPoster(book))
			{
				Page moveAfter = (Page)((Object [])param)[0];
				List<File> files = (List<File>)((Object [])param)[1];
				List<Page> pages = host.getAppHost().getActionRequestListener().onAppendPagesRequest(book, files);
				host.getAppHost().getActionRequestListener().onMovePagesRequest(pages, moveAfter);
			}
		}
		else if (action.equals(movePagesRequestEvent))
		{
			if (!PosterUtils.isPoster(book))
			{
				Page moveAfter = (Page)((Object [])param)[0];
				List<Page> pages = (List<Page>)((Object [])param)[1];
				host.getAppHost().getActionRequestListener().onMovePagesRequest(pages, moveAfter);
			}
		}
		else configurationEditor.onActionRequest(action, param);
	}
	@Override public void onShow() {}
	@Override public void onHide() {}
	@Override public void onCloseRequest() {book.unloadMetaData(); configurationEditor.onCloseRequest();}

	@Override public void onEditStitchesRequest(Fragment f1, Fragment f2) {StitcherUtils.editStitches(host.getAppHost(), book, (FragmentView)configurationEditor, f1, f2);}
	@Override public void onRenderRequest() {StitcherUtils.renderStitches(host.getAppHost(), book, (FragmentView)configurationEditor);}
	@Override public void onRenderEnded(List<MetaData> parts) {StitcherUtils.removeStitches(host.getAppHost(), book); StitcherUtils.updatePoster(host.getAppHost(), book, parts);}
	@Override public void onSaveRequest(boolean force)
	{
		if (force || JOptionPane.showConfirmDialog(this, Lang.s("saveStitchMsg"), Lang.s("saveStitchLabel"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			if (configurationEditor instanceof FragmentView)
				StitcherUtils.saveStitches(this, (FragmentView)configurationEditor, book, host.getAppHost().getLink());
			else if (configurationEditor instanceof StitchEditor)
				StitcherUtils.saveStitches(this, ((StitchEditor)configurationEditor).view, book, host.getAppHost().getLink());
		}
	}
	@Override public void onCancelRequest()
	{
		if (configurationEditor instanceof RenderEditor)
			StitcherUtils.cancelRender(host.getAppHost(), book, ((RenderEditor)configurationEditor).fragmentView);
		else if (configurationEditor instanceof StitchEditor)
			StitcherUtils.stopEdit(host.getAppHost(), book, ((StitchEditor)configurationEditor).view);
		else StitcherUtils.removeStitches(host.getAppHost(), book);
	}
	@Override public void onDetectLayoutRequest() {StitcherUtils.detectLayout((FragmentView)configurationEditor);}

	@Override public boolean allowFileExports() {return false;}
	@Override public File getCurFile() {return null;}
	@Override public DocExploreDataLink getLink() {return host.getAppHost().getLink();}
	@Override public boolean allowsSidePanel() {return sidePanel;}
}
