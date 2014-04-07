package org.interreg.docexplore.management.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.manage.ManageComponent;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.ImageUtils;

public class PageLabel extends JPanel
{
	private static final long serialVersionUID = -3005585743647321920L;
	
	Page page;
	JLabel mini;
	BookViewer viewer;
	boolean selected = false;
	
	public PageLabel(final BookViewer viewer, final Page page)
	{
		super(new BorderLayout());
		
		this.viewer = viewer;
		this.page = page;
		this.mini = new JLabel(ImageUtils.getIcon("page_search-48x48.png"));
		mini.setHorizontalAlignment(SwingConstants.CENTER);
		mini.setVerticalAlignment(SwingConstants.BOTTOM);
		setPreferredSize(new Dimension(72, 80));
		add(mini, BorderLayout.NORTH);
		JLabel num = new JLabel("<html><b>"+page.getPageNumber()+"</b></html>", SwingConstants.CENTER);
		num.setVerticalAlignment(SwingConstants.TOP);
		add(num, BorderLayout.CENTER);
		setOpaque(false);
		
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		setFocusable(true);
	}
	
	static MouseAdapter adapter = new MouseAdapter()
	{
		@SuppressWarnings("serial")
		public void mouseReleased(MouseEvent e)
		{
			PageLabel source = (PageLabel)e.getSource();
			source.requestFocus();
			BookViewer viewer = source.viewer;
			Component comp = source.viewer.pagePanel.getComponentAt(SwingUtilities.convertPoint(source, e.getPoint(), source.viewer.pagePanel));
			final PageLabel label = comp != null && comp instanceof PageLabel ? (PageLabel)comp : null;
			Point p = label != null ? SwingUtilities.convertPoint(source, e.getPoint(), label) : null;
			
			if (label != null && viewer.dragSource == null || viewer.dragSource == label)
			{
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1)
				{
					boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
					boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
					
					if (!ctrl)
						for (Component pageLabel : label.viewer.pagePanel.getComponents())
							((PageLabel)pageLabel).setSelected(false);
					
					if (shift)
					{
						int from = label.viewer.lastSelected != null ? label.viewer.lastSelected.getIndex() : 0;
						int to = label.getIndex();
						for (int i=from;i!=to;i+=(from<to ? 1 : -1))
							((PageLabel)label.viewer.pagePanel.getComponent(i)).setSelected(true);
						label.setSelected(true);
					}
					else label.setSelected(ctrl ? !label.selected : true);
					
					if (!shift)
						label.viewer.lastSelected = label;
				}
				else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {label.viewer.win.addTab(label.page);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
					final ManageComponent manageComp = label.viewer.win.manageComponent;
					final List<Page> pages = viewer.getSelectedPages();
					
					JPopupMenu popMenu = new JPopupMenu();
					if (pages.size() > 0 && !label.viewer.isLocked)
						popMenu.add(new AbstractAction(
							XMLResourceBundle.getBundledString("manageDeletePageLabel")) {public void actionPerformed(ActionEvent arg0)
							{
								manageComp.handler.pagesDeleted(pages);
//								if (manageComp.handler.pagesDeleted(pages))
//									try {label.viewer.setDocument(label.viewer.book);}
//									catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
							}});
//					popMenu.add(new AbstractAction(
//						XMLResourceBundle.getBundledString("manageProcessPageLabel")) {public void actionPerformed(ActionEvent arg0)
//							{manageComp.handler.pagesProcessed(pages);}});
//					popMenu.add(new AbstractAction(
//						XMLResourceBundle.getBundledString("manageExportPageLabel")) {public void actionPerformed(ActionEvent arg0)
//							{manageComp.handler.pagesExported(pages);}});
					popMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			else if (label != null)
			{
				try
				{
					final ManageComponent manageComp = label.viewer.win.manageComponent;
					final List<Page> pages = viewer.getSelectedPages();
					int after = label.page.getPageNumber()-(p.x > label.getWidth()/2 ? 0 : 1);
					manageComp.handler.pagesMoved(pages, after > 0 ? label.page.getBook().getPage(after) : null);
					
//					Page from = label.viewer.dragSource.page;
//					int toIndex = p.x < label.getWidth()/2 ? label.page.getPageNumber() : label.page.getPageNumber()+1;
//					Page before = toIndex <= from.getBook().getLastPageNumber() ? from.getBook().getPage(toIndex) : null;
//					label.viewer.win.manageComponent.handler.pageMoved(from, from.getBook(), before);
//					label.viewer.setDocument(from.getBook());
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
			
			viewer.dragSource = null;
			setMovedIndex(source.viewer, -2);
			viewer.pagePanel.repaint();
		}
		
		public void mousePressed(MouseEvent e)
		{
			PageLabel label = (PageLabel)e.getSource();
			if (label.viewer.isLocked)
				return;
			label.viewer.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.viewer);
			label.viewer.dragSource = label;
			
			boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
			boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
			if (!label.selected && !ctrl && !shift)
			{
				for (Component pageLabel : label.viewer.pagePanel.getComponents())
					((PageLabel)pageLabel).setSelected(false);
				label.setSelected(true);
				label.viewer.pagePanel.repaint();
			}
		}
		
		public void mouseDragged(MouseEvent e)
		{
			PageLabel label = (PageLabel)e.getSource();
			if (label.viewer.dragSource == null)
				return;
			label.viewer.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.viewer);
			
			Point p = SwingUtilities.convertPoint(label, e.getPoint(), label.viewer.pagePanel);
			Component comp = label.viewer.pagePanel.getComponentAt(p);
			if (comp == null || !(comp instanceof PageLabel))
				setMovedIndex(label.viewer, -2);
			else
			{
				PageLabel under = (PageLabel)label.viewer.pagePanel.getComponentAt(p);
				if (SwingUtilities.convertPoint(label.viewer, p, under).x < under.getWidth()/2)
					setMovedIndex(label.viewer, under.getIndex()-1);
				else setMovedIndex(label.viewer, under.getIndex());
			}
			
			label.viewer.repaint();
		}
		
		void setMovedIndex(BookViewer viewer, int index)
		{
			int left = viewer.lastMovedIndex, right = viewer.lastMovedIndex+1;
			if (left >= 0)
				((PageLabel)viewer.pagePanel.getComponent(left)).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.blue));
			if (right >= 0 && right < viewer.pagePanel.getComponentCount())
				((PageLabel)viewer.pagePanel.getComponent(right)).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.blue));
			
			left = index; right = index+1;
			if (left >= 0)
				((PageLabel)viewer.pagePanel.getComponent(left)).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.blue));
			if (right >= 0 && right < viewer.pagePanel.getComponentCount())
				((PageLabel)viewer.pagePanel.getComponent(right)).setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue));
			
			viewer.lastMovedIndex = index;
		}
	};
	
	public int getIndex()
	{
		for (int i=0;i<viewer.pagePanel.getComponentCount();i++)
			if (viewer.pagePanel.getComponent(i) == this)
				return i;
		return -1;
	}
	
	public void setSelected(boolean selected)
	{
		if (this.selected == selected)
			return;
		
		this.selected = selected;
		if (selected)
		{
			setOpaque(true);
			setBackground(new Color(.8f, .9f, 1f));
		}
		else setOpaque(false);
	}
}