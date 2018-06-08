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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.ImageUtils;

public class PageLabel extends JPanel
{
	private static final long serialVersionUID = -3005585743647321920L;
	
	Page page;
	JLabel mini;
	BookPagesEditorOld pagesEditor;
	boolean selected = false;
	
	public PageLabel(final BookPagesEditorOld pagesEditor, final Page page)
	{
		super(new BorderLayout());
		
		this.pagesEditor = pagesEditor;
		this.page = page;
		this.mini = new JLabel(ImageUtils.getIcon("page_search-48x48.png"));
		mini.setHorizontalAlignment(SwingConstants.CENTER);
		mini.setVerticalAlignment(SwingConstants.CENTER);
		//setPreferredSize(new Dimension(72, 80));
		add(mini, BorderLayout.NORTH);
		
		int nRegions = 0;
		try {nRegions = page.getRegions().size();}
		catch (Exception e) {e.printStackTrace();}
		JLabel num = new JLabel("<html><b>"+page.getPageNumber()+"</b>"+(nRegions > 0 ? " ("+nRegions+")" : "")+"</html>", SwingConstants.CENTER);
		
		num.setVerticalAlignment(SwingConstants.TOP);
		add(num, BorderLayout.CENTER);
		setOpaque(false);
		
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		setFocusable(true);
	}
	
	static MouseAdapter adapter = new MouseAdapter()
	{
		public void mouseReleased(MouseEvent e)
		{
			PageLabel source = (PageLabel)e.getSource();
			source.requestFocus();
			BookPagesEditorOld pagesEditor = source.pagesEditor;
			Component comp = source.pagesEditor.pagePanel.getComponentAt(SwingUtilities.convertPoint(source, e.getPoint(), source.pagesEditor.pagePanel));
			final PageLabel label = comp != null && comp instanceof PageLabel ? (PageLabel)comp : null;
			Point p = label != null ? SwingUtilities.convertPoint(source, e.getPoint(), label) : null;
			
			if (label != null && (pagesEditor.dragSource == null || pagesEditor.dragSource == label))
			{
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1)
				{
					boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
					boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
					
					if (!ctrl)
						for (Component pageLabel : label.pagesEditor.pagePanel.getComponents())
							((PageLabel)pageLabel).setSelected(false);
					
					if (shift)
					{
						int from = label.pagesEditor.lastSelected != null ? label.pagesEditor.lastSelected.getIndex() : 0;
						int to = label.getIndex();
						for (int i=from;i!=to;i+=(from<to ? 1 : -1))
							((PageLabel)label.pagesEditor.pagePanel.getComponent(i)).setSelected(true);
						label.setSelected(true);
					}
					else label.setSelected(ctrl ? !label.selected : true);
					
					if (!shift)
						label.pagesEditor.lastSelected = label;
				}
				else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {label.pagesEditor.bookEditor.host.onDocumentEditorRequest(label.page);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
//					final ManageComponent manageComp = label.viewer.win.manageComponent;
					final List<Page> pages = pagesEditor.getSelectedPages();
					
					JPopupMenu popMenu = new JPopupMenu();
					if (pages.size() > 0 && !label.pagesEditor.readOnly)
						;
					
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
					List<Page> pages = pagesEditor.getSelectedPages();
					int after = label.page.getPageNumber()-(p.x > label.getWidth()/2 ? 0 : 1);
					pagesEditor.bookEditor.host.getAppHost().getActionRequestListener().onMovePagesRequest(pages, after > 0 ? label.page.getBook().getPage(after) : null);
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
			
			pagesEditor.dragSource = null;
			setMovedIndex(source.pagesEditor, -2);
			pagesEditor.pagePanel.repaint();
		}
		
		public void mousePressed(MouseEvent e)
		{
			PageLabel label = (PageLabel)e.getSource();
			if (label.pagesEditor.readOnly)
				return;
			label.pagesEditor.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.pagesEditor);
			label.pagesEditor.dragSource = label;
			
			boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
			boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
			if (!label.selected && !ctrl && !shift)
			{
				for (Component pageLabel : label.pagesEditor.pagePanel.getComponents())
					((PageLabel)pageLabel).setSelected(false);
				label.setSelected(true);
				label.pagesEditor.pagePanel.repaint();
			}
		}
		
		public void mouseDragged(MouseEvent e)
		{
			PageLabel label = (PageLabel)e.getSource();
			if (label.pagesEditor.dragSource == null)
				return;
			label.pagesEditor.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.pagesEditor);
			
			Point p = SwingUtilities.convertPoint(label, e.getPoint(), label.pagesEditor.pagePanel);
			Component comp = label.pagesEditor.pagePanel.getComponentAt(p);
			if (comp == null || !(comp instanceof PageLabel))
				setMovedIndex(label.pagesEditor, -2);
			else
			{
				PageLabel under = (PageLabel)label.pagesEditor.pagePanel.getComponentAt(p);
				if (SwingUtilities.convertPoint(label.pagesEditor, p, under).x < under.getWidth()/2)
					setMovedIndex(label.pagesEditor, under.getIndex()-1);
				else setMovedIndex(label.pagesEditor, under.getIndex());
			}
			
			label.pagesEditor.repaint();
		}
		
		void setMovedIndex(BookPagesEditorOld pagesEditor, int index)
		{
			int left = pagesEditor.lastMovedIndex, right = pagesEditor.lastMovedIndex+1;
			if (left >= 0)
				((PageLabel)pagesEditor.pagePanel.getComponent(left)).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.blue));
			if (right >= 0 && right < pagesEditor.pagePanel.getComponentCount())
				((PageLabel)pagesEditor.pagePanel.getComponent(right)).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.blue));
			
			left = index; right = index+1;
			if (left >= 0)
				((PageLabel)pagesEditor.pagePanel.getComponent(left)).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.blue));
			if (right >= 0 && right < pagesEditor.pagePanel.getComponentCount())
				((PageLabel)pagesEditor.pagePanel.getComponent(right)).setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.blue));
			
			pagesEditor.lastMovedIndex = index;
		}
	};
	
	public int getIndex()
	{
		for (int i=0;i<pagesEditor.pagePanel.getComponentCount();i++)
			if (pagesEditor.pagePanel.getComponent(i) == this)
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