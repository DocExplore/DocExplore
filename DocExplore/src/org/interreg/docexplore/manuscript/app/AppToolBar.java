/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.app;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.gui.image.EditorView;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.manuscript.app.editors.CropOperation;
import org.interreg.docexplore.manuscript.app.editors.FreeShapeROIOperation;
import org.interreg.docexplore.manuscript.app.editors.GuiConstants;
import org.interreg.docexplore.manuscript.app.editors.ImageMetaDataEditor;
import org.interreg.docexplore.manuscript.app.editors.PageEditor;
import org.interreg.docexplore.manuscript.app.editors.RectROIOperation;
import org.interreg.docexplore.util.ImageUtils;

public class AppToolBar extends JPanel implements ToolbarButton.ToolbarButtonListener, ToolbarToggleButton.ToolbarToggleButtonListener
{
	private static final long serialVersionUID = 656205720944428635L;
	
	ManuscriptAppHost host;
	public ToolbarButton prev, next, up;
	protected JPanel toolPanel;
	
	protected AppToolBar(final ManuscriptAppHost win)
	{
		//super(new LooseGridLayout(1, 0, 0, 0, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(GuiConstants.toolColor);
		this.host = win;
	}
	
	protected void buildToolBar()
	{
		toolPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
		toolPanel.setOpaque(false);
		addNavigationButtons();
		addViewButtons();
		addEditBookButtons();
		addEditPageButtons();
		addEditImageButtons();
		addStitchButtons();
		add(toolPanel, BorderLayout.WEST);
	}
	
	@SuppressWarnings("serial")
	protected void addNavigationButtons()
	{
		addButton(this.prev = new ToolbarButton(this, "prev", "previous-24x24.png", Lang.s("imageToolbarPreviousHistoric"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Page || document instanceof Region)
				{
					Page page = document instanceof Page ? (Page)document : document instanceof Region ? ((Region)document).getPage() : null;
					setEnabled(page != null && page.getPageNumber() > 1);
				}
				else setEnabled(false);
			}
		});
		addButton(this.up = new ToolbarButton(this, "up", "up-24x24.png", Lang.s("imageToolbarUp"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Book || document instanceof Page || document instanceof Region)
					setEnabled(true);
				else setEnabled(false);
			}
		});
		addButton(this.next = new ToolbarButton(this, "next", "next-24x24.png", Lang.s("imageToolbarNextHistoric"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (document == null)
					setEnabled(false);
				else if (document instanceof Page || document instanceof Region)
				{
					Page page = document instanceof Page ? (Page)document : document instanceof Region ? ((Region)document).getPage() : null;
					try {setEnabled(page.getPageNumber() < page.getBook().getLastPageNumber());}
					catch(Exception e2) {ErrorHandler.defaultHandler.submit(e2);}
				}
				else setEnabled(false);
			}
		});
		addGap();
	}
	@SuppressWarnings("serial")
	protected void addViewButtons()
	{
		addButton(new ToolbarButton(this, "fit", "fit-24x24.png", Lang.s("imageToolbarFit"))
		{
			@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try
				{
					setEnabled(document != null && (
						document instanceof Region 
						|| document instanceof Page 
						|| (document instanceof Book && PosterUtils.isPoster((Book)document) && PosterUtils.isInStitches((Book)document))
						|| (document instanceof Book && PosterUtils.isPoster((Book)document) && PosterUtils.isInRendering((Book)document))));
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex, true); setEnabled(false);}
			}
		});
		addGap();
	}
	@SuppressWarnings("serial")
	protected void addEditBookButtons()
	{
		addButton(new ToolbarButton(this, "add", "add-file-24x24.png", Lang.s("manageAppendPagesLabel"))
		{
			@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				setEnabled(document == null || document instanceof Book);
			}
		});
		addButton(new ToolbarButton(this, "delete", "delete-file-24x24.png", Lang.s("manageDeletePageLabel"))
		{
			@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document == null || document instanceof Book && ((Book)document).getLastPageNumber() > 0 || document instanceof Region);}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		addGap();
	}
	protected void addEditPageButtons()
	{
		addPageEditorOperationButton("add-free-roi", "add-free-roi-24x24.png", Lang.s("imageToolbarAddFreeRoi"), FreeShapeROIOperation.class);
		addPageEditorOperationButton("add-rect-roi", "add-rect-roi-24x24.png", Lang.s("imageToolbarAddRectRoi"), RectROIOperation.class);
		addPageEditorOperationButton("crop", "resize-page-24x24.png", Lang.s("imageToolbarCrop"), CropOperation.class, true);
		addGap();
	}
	@SuppressWarnings("serial")
	protected void addEditImageButtons()
	{
		addButton(new ToolbarButton(this, "fill", "fill-poster-24x24.png", Lang.s("imageToolbarFillPoster"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && PosterUtils.isPoster((Book)document) && !PosterUtils.isInStitches((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		addButton(new ToolbarButton(this, "mirror-hor", "mirror-hor-24x24.png", Lang.s("imageToolbarMirrorHor"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && PosterUtils.isPoster((Book)document) && !PosterUtils.isInStitches((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		addButton(new ToolbarButton(this, "mirror-ver", "mirror-ver-24x24.png", Lang.s("imageToolbarMirrorVer"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && PosterUtils.isPoster((Book)document) && !PosterUtils.isInStitches((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		addButton(new ToolbarButton(this, "rotate-left", "rotate-left-24x24.png", Lang.s("imageToolbarRotateLeft"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && PosterUtils.isPoster((Book)document) && !PosterUtils.isInStitches((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		addButton(new ToolbarButton(this, "rotate-right", "rotate-right-24x24.png", Lang.s("imageToolbarRotateRight"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && PosterUtils.isPoster((Book)document) && !PosterUtils.isInStitches((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
		addGap();
	}
	
	@SuppressWarnings("serial")
	protected void addStitchButtons()
	{
		addButton(new ToolbarButton(this, "stitch", "stitches-24x24.png", Lang.s("imageToolbarStitch"))
		{
			public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				try {setEnabled(document != null && document instanceof Book && PosterUtils.isPoster((Book)document) && !PosterUtils.isInStitches((Book)document));}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); setEnabled(false);}
			}
		});
	}
	
	public void addPageEditorOperationButton(String action, String iconName, String tooltip, final Class<? extends EditorView.Operation<?>> clazz)
	{
		addPageEditorOperationButton(action, ImageUtils.getIcon(iconName), tooltip, clazz);
	}
	public void addPageEditorOperationButton(String action, String iconName, String tooltip, final Class<? extends EditorView.Operation<?>> clazz, boolean worksForMetaData)
	{
		addPageEditorOperationButton(action, ImageUtils.getIcon(iconName), tooltip, clazz, worksForMetaData);
	}
	public void addPageEditorOperationButton(String action, Icon icon, String tooltip, final Class<? extends EditorView.Operation<?>> clazz)
	{
		addPageEditorOperationButton(action, icon, tooltip, clazz, false);
	}
	@SuppressWarnings("serial")
	public void addPageEditorOperationButton(String action, Icon icon, String tooltip, final Class<? extends EditorView.Operation<?>> clazz, final boolean worksForMetaData)
	{
		ToolbarToggleButton button = new ToolbarToggleButton(this, action, icon, tooltip)
		{
			@Override public void onActiveDocumentChanged(DocumentPanel panel, AnnotatedObject document)
			{
				if (panel != null && (panel.getEditor() instanceof PageEditor || worksForMetaData && panel.getEditor() instanceof ImageMetaDataEditor))
				{
					setEnabled(true);
					setSelected(((EditorView)panel.getEditor()).getOperation() != null && ((EditorView)panel.getEditor()).getOperation().getClass() == clazz);
				}
				else setEnabled(false);
			}
		};
		addToggleButton(button);
	}
	
	public void addToggleButton(ToolbarToggleButton button) {toolPanel.add(button); host.addAppListener(button);}
	public void addButton(ToolbarButton button) {toolPanel.add(button); host.addAppListener(button);}
	public void addGap() {toolPanel.add(new JLabel(" "));}
	
	@Override public void onToolbarButton(ToolbarButton button)
	{
		if (button.action != null)
			host.broadcastAction(button.action, null);
	}
	@Override public void onToolbarToggleButton(ToolbarToggleButton button, boolean selected)
	{
		if (button.action != null)
			host.broadcastAction(button.action, selected);
	}
	
	@SuppressWarnings("serial")
	public void addAccelerators(JRootPane root)
	{
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		root.getActionMap().put("LEFT", new AbstractAction() {public void actionPerformed(ActionEvent e) {prev.doClick();}});
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
		root.getActionMap().put("RIGHT", new AbstractAction() {public void actionPerformed(ActionEvent e) {next.doClick();}});
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		root.getActionMap().put("UP", new AbstractAction() {public void actionPerformed(ActionEvent e) {up.doClick();}});
	}
}
