package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.FlowLayout;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.gui.ToolbarButton;
import org.interreg.docexplore.management.gui.ToolbarToggleButton;
import org.interreg.docexplore.management.image.CropOperation;
import org.interreg.docexplore.management.image.FreeShapeOperation;
import org.interreg.docexplore.management.image.SquareOperation;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;

@SuppressWarnings("serial")
public class PageEditorToolbar extends JPanel
{
	public PageEditorToolbar(final PageEditor editor, final JScrollPane scrollPane)
	{
		super(new WrapLayout(FlowLayout.LEFT));
		
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("previous-24x24.png", XMLResourceBundle.getBundledString("imageToolbarPreviousHistoric"))
		{
			public void clicked()
			{
				try
				{
					final int pageNum = editor.view.curPage.getPageNumber();
					if (pageNum < 2)
						return;
					final Page page = editor.view.curPage.getBook().getPage(pageNum-1);
					SwingUtilities.invokeLater(new Runnable() {public void run()
					{
						editor.view.explorer.explore(page.getBook().getCanonicalUri());
						editor.view.explorer.explore(page.getCanonicalUri());
					}});
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("next-24x24.png", XMLResourceBundle.getBundledString("imageToolbarNextHistoric"))
		{
			public void clicked()
			{
				try
				{
					final int pageNum = editor.view.curPage.getPageNumber();
					if (pageNum > editor.view.curPage.getBook().getLastPageNumber()-1)
						return;
					final Page page = editor.view.curPage.getBook().getPage(pageNum+1);
					SwingUtilities.invokeLater(new Runnable() {public void run()
					{
						editor.view.explorer.explore(page.getBook().getCanonicalUri());
						editor.view.explorer.explore(page.getCanonicalUri());
					}});
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}
		}));
		
		final double zoomFactor = 1.5;
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("zoom-in-24x24.png", XMLResourceBundle.getBundledString("imageToolbarZoomIn"))
		{
			public void clicked()
			{
				Point point = scrollPane.getViewport().getViewPosition();
				editor.applyZoomShift(zoomFactor);
				point.x *= zoomFactor; point.y *= zoomFactor;
				scrollPane.getViewport().setViewPosition(point);
			}
		}));
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("zoom-out-24x24.png", XMLResourceBundle.getBundledString("imageToolbarZoomOut"))
		{
			public void clicked()
			{
				Point point = scrollPane.getViewport().getViewPosition();
				editor.applyZoomShift(1/zoomFactor);
				point.x /= zoomFactor; point.y /= zoomFactor;
				scrollPane.getViewport().setViewPosition(point);
			}
		}));
		
		ToolbarButton fit = new ToolbarButton("fit-24x24.png", XMLResourceBundle.getBundledString("imageToolbarFit"))
		{
			public void clicked() {editor.fit();}
		};
		add(fit);
		editor.addMainWindowListener(fit);
		
		add(new JSeparator(SwingConstants.VERTICAL));
		
		final List<ToolbarToggleButton> roiButtons = new LinkedList<ToolbarToggleButton>();
		
		ToolbarToggleButton addFree = new ToolbarToggleButton("add-free-roi-24x24.png", XMLResourceBundle.getBundledString("imageToolbarAddFreeRoi"))
		{
			public void toggled()
			{				
				for (ToolbarToggleButton button : roiButtons)
					if (button != this)
						button.setSelected(false);
				editor.setOperation(new FreeShapeOperation());
			}
			public void untoggled()
			{
				editor.setOperation(null);
			}
		};
		roiButtons.add((ToolbarToggleButton)add(addFree));
		editor.addMainWindowListener(addFree);
		
		ToolbarToggleButton addRect = new ToolbarToggleButton("add-rect-roi-24x24.png", XMLResourceBundle.getBundledString("imageToolbarAddRectRoi"))
		{
			public void toggled()
			{				
				for (ToolbarToggleButton button : roiButtons)
					if (button != this)
						button.setSelected(false);
				editor.setOperation(new SquareOperation());
			}
			public void untoggled()
			{
				editor.setOperation(null);
			}
		};
		roiButtons.add((ToolbarToggleButton)add(addRect));
		editor.addMainWindowListener(addRect);
		
		editor.addMainWindowListener((ToolbarButton)add(new ToolbarButton("del-roi-24x24.png", XMLResourceBundle.getBundledString("imageToolbarDelRoi"))
		{
			public void clicked()
			{
				if (editor.document != null && editor.document instanceof Region)
					editor.deleteRegion((Region)editor.document);
			}
			public void activeDocumentChanged(AnnotatedObject document)
			{
				setEnabled(document != null && document instanceof Region);
			}
		}));
		
		ToolbarToggleButton crop = new ToolbarToggleButton("resize-page-24x24.png", XMLResourceBundle.getBundledString("imageToolbarCrop"))
		{
			public void toggled()
			{				
				for (ToolbarToggleButton button : roiButtons)
					if (button != this)
						button.setSelected(false);
				editor.setOperation(new CropOperation());
			}
			public void untoggled()
			{
				editor.setOperation(null);
			}
		};
		roiButtons.add((ToolbarToggleButton)add(crop));
		editor.addMainWindowListener(crop);
	}
}
