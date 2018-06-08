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
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.manage.SelectPagesPanel;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.StitcherUtils;
import org.interreg.docexplore.manuscript.TileConfiguration;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;
import org.interreg.docexplore.manuscript.app.DocumentEvents;
import org.interreg.docexplore.util.GuiUtils;

public class PosterPartsEditor extends DragDropPanel implements ConfigurationEditor
{
	private static final long serialVersionUID = -3584515946932650576L;
	
	protected DocumentEditorHost host;
	Book book;
	JPanel partPanel;
	
	Point dragPos;
	PosterPartElement dragSource = null;
	Icon dragIcon = null;
	PosterPartElement lastMovedComp = null;
	
	public JPanel previewButtons;
	public PosterPreview posterPreview;
	
	public PosterPartsEditor(final DocumentEditorHost host, final Book book) throws DataLinkException
	{
		super(new BorderLayout(5, 5), false, true);
		setBackground(Color.white);
		
		this.host = host;
		this.book = book;
		this.partPanel = new JPanel(new LooseGridLayout(1, 1, 0, 0, true, true, SwingConstants.CENTER, SwingConstants.CENTER, false, false));
		partPanel.setBackground(Color.white);
		
		WrapLayout topLayout = new WrapLayout();
		topLayout.setHgap(10);
		JPanel topPanel = new JPanel(topLayout);
		topPanel.setOpaque(false);
		
		topPanel.add(posterPreview = new PosterPreview(host, book));
		//posterPreview.setHorizontalAlignment(SwingConstants.CENTER);
		posterPreview.addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {posterPreview.fit();}});
		
		this.previewButtons = new JPanel(new LooseGridLayout(0, 1, 5, 5, false, false, SwingConstants.CENTER, SwingConstants.CENTER, false, false));
		previewButtons.setOpaque(false);
		IconButton refreshButton = new IconButton("refresh-24x24.png", Lang.s("manageRefreshLabel"));
		previewButtons.add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e)
		{
			GuiUtils.blockUntilComplete(new GuiUtils.ProgressRunnable() {@Override public void run()
			{
				try
				{
					if (book.getLastPageNumber() > 0)
					{
						TileConfiguration config = new TileConfiguration();
						config.build(host.getAppHost().getLink(), book, progress);
						
						Page page = book.getPage(1);
						page.setMetaDataString(host.getAppHost().getLink().dimKey, config.getFullWidth()+","+config.getFullHeight());
						//DocExploreDataLink.getImageMini(page);
						book.setMetaDataString(host.getAppHost().getLink().upToDateKey, "true");
					}
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				posterPreview.refresh(getParent());
			}
			float [] progress = {0}; @Override public float getProgress() {return progress[0];}}, PosterPartsEditor.this);
		}});
		topPanel.add(previewButtons);
		add(topPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(partPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		
		addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {posterPreview.refresh(getParent());}});
		posterPreview.refresh(getParent());
		
		refresh();
	}
	
	public List<PosterPartElement> getSelectedPartLabels()
	{
		Vector<PosterPartElement> parts = new Vector<PosterPartElement>();
		for (Component component : partPanel.getComponents())
			if (component instanceof PosterPartElement && ((PosterPartElement)component).selected)
				parts.add((PosterPartElement)component);
		return parts;
	}
	public List<MetaData> getSelectedParts()
	{
		List<PosterPartElement> icons = getSelectedPartLabels();
		List<MetaData> parts = new ArrayList<MetaData>(icons.size());
		for (PosterPartElement icon : icons)
			parts.add(icon.part);
		return parts;
	}
	
	static Icon emptyIcon = new ImageIcon(new BufferedImage(48, 48, BufferedImage.TYPE_3BYTE_BGR));
	public void refresh()
	{
		try
		{
			partPanel.removeAll();
			MetaData [][] mds = PosterUtils.getBaseTilesArray(host.getAppHost().getLink(), book);
			if (mds.length > 0 && mds[0].length > 0)
			{
				int [] rowLengths = new int [mds[0].length];
				for (int j=0;j<rowLengths.length;j++)
				{
					rowLengths[j] = 0;
					for (int i=mds.length;i>0;i--)
						if (mds[i-1][j] != null)
							{rowLengths[j] = i; break;}
				}
				int w = 2*mds.length+1, h = 2*mds[0].length+1;
				((LooseGridLayout)partPanel.getLayout()).setCols(w);
				((LooseGridLayout)partPanel.getLayout()).setRows(h);
				for (int j=0;j<h;j++)
					for (int i=0;i<w;i++)
				{
					int x = i/2, y = j/2;
					if (i%2 == 0 && j%2 == 0)
					{
						partPanel.add(new JLabel(""));
					}
					else if (i%2 == 1 && j%2 == 1)
					{
						if (mds[x][y] != null)
							partPanel.add(new PosterPartElement(this, mds[x][y], x, y));
						else partPanel.add(new JLabel(""));
					}
					else
					{
						if (j%2 == 1 && ((x >= 1 && mds[x-1][y] != null) || (x < mds.length && mds[x][y] != null)) ||
							i%2 == 1 && ((y >= 1 && mds[x][y-1] != null) || (y < mds[0].length && mds[x][y] != null)))
								partPanel.add(new PosterPartElement(this, null, x, y, j%2 == 0));
						else partPanel.add(new JLabel(""));
					}
				}
			}
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		
		new Thread() {public void run()
		{
			try
			{
				int cnt = 0;
				for(Component comp : partPanel.getComponents())
					if (comp instanceof PosterPartElement)
				{
					PosterPartElement label = (PosterPartElement)comp;
					MetaData part = label.part;
					if (part != null)
						try
						{
							if (part.getType().equals(MetaData.imageType))
								label.mini.setIcon(new ImageIcon(DocExploreDataLink.getImageMini(part)));
							else label.mini.setIcon(emptyIcon);
						}
						catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, true);}
					
					cnt++;
					if (cnt%10 == 0)
						{validate(); repaint();}
				}
				validate();
				repaint();
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}}.start();
		
		validate();
		repaint();
	}
	
	@Override public void onActionRequest(String action, Object param)
	{
		if (book == null)
			return;
		if (action.equals("fill"))
			host.getAppHost().getActionRequestListener().onFillPosterHolesRequest(book);
		else if (action.equals("mirror-hor"))
			host.getAppHost().getActionRequestListener().onHorizontalMirrorPartsRequest(book);
		else if (action.equals("mirror-ver"))
			host.getAppHost().getActionRequestListener().onVerticalMirrorPartsRequest(book);
		else if (action.equals("rotate-left"))
			host.getAppHost().getActionRequestListener().onRotatePartsLeftRequest(book);
		else if (action.equals("rotate-right"))
			host.getAppHost().getActionRequestListener().onRotatePartsRightRequest(book);
		else if (action.equals("stitch"))
			StitcherUtils.putInStitches(host.getAppHost(), book);
		else if (action.equals("add"))
		{
			List<File> files = SelectPagesPanel.show();
			if (files != null && !files.isEmpty())
				host.getAppHost().broadcastAction(action, new Object [] {book, files});
		}
		else if (action.equals("delete"))
		{
			List<MetaData> parts = getSelectedParts();
			if (parts != null && !parts.isEmpty())
				host.getAppHost().broadcastAction(DocumentEvents.deleteParts.event, new Object [] {book, parts});
		}
	}

	@Override public Component getComponent() {return this;}
		
	boolean readOnly = false;
	@Override public void setReadOnly(boolean b)
	{
		dropsEnabled = !b;
		readOnly = b;
		Component [] comps = partPanel.getComponents();
		for (int i=0;i<comps.length;i++)
			if (comps[i] instanceof PosterPartElement)
				((PosterPartElement)comps[i]).setReadOnly(b);
	}

	@Override public Collection<Object> setDraggedData(int x, int y) {return null;}
	@Override public void onIncomingDrag(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		if (source instanceof PosterPartElement && ((PosterPartElement)source).editor != this)
			partPanel.setBackground(GuiConstants.actionColor);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		partPanel.setBackground(Color.white);
		//append files
		if (source == null)
		{
			List<File> files = (List)data;
			host.getAppHost().getActionRequestListener().onAppendPartsRequest(book, files);
		}
		else if (source instanceof IconPanelElement && source instanceof PosterPartsEditor && source != this)
		{
			
		}
		//import books
		else if (source instanceof IconPanelElement && ((IconPanelElement)source).editor instanceof CollectionBooksEditor)
		{
			List<Book> books = (List)data;
			Book importedBook = null;
			DocExploreDataLink link = ((CollectionBooksEditor)((IconPanelElement)source).editor).collectionEditor.host.getAppHost().getLink();
			try
			{
				for (Book book : books)
					if (PosterUtils.isPoster(book))
						{importedBook = book; break;}
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			if (importedBook != null)
				host.getAppHost().broadcastAction(ManuscriptEditor.importPartsRequestEvent, new Object [] {importedBook, link});
		}
	}
	@Override public void onDragExited(Collection<Object> data, DragDropPanel source)
	{
		partPanel.setBackground(Color.white);
	}
	
	@Override public void onCloseRequest() {}
	@Override public boolean allowGoto() {return true;}
}
