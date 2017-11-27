/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.image;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ImageUtils;

public class PartLabel extends JPanel
{
	private static final long serialVersionUID = -3005585743647321920L;
	
	MetaData part;
	int pi, pj;
	JLabel mini;
	PosterPartsEditor editor;
	boolean selected = false;
	boolean insertRow;
	
	public PartLabel(final PosterPartsEditor editor, final MetaData part, int pi, int pj)
	{
		this(editor, part, pi, pj, false);
	}
	public PartLabel(final PosterPartsEditor editor, final MetaData part, int pi, int pj, boolean insertRow)
	{
		super(new BorderLayout());
		
		this.editor = editor;
		this.part = part;
		this.pi = pi;
		this.pj = pj;
		this.insertRow = insertRow;
		this.mini = part == null ? new JLabel("   ") : new JLabel(ImageUtils.getIcon("page_search-48x48.png"));
		mini.setHorizontalAlignment(SwingConstants.CENTER);
		mini.setVerticalAlignment(SwingConstants.CENTER);
		//setPreferredSize(new Dimension(72, 80));
		add(mini, BorderLayout.CENTER);
		
		if (part != null)
		{
			try {setToolTipText(part.getMetaDataString(editor.host.getLink().sourceKey));}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, true);}
			addMouseListener(adapter);
			addMouseMotionListener(adapter);
			setFocusable(true);
			setOpaque(false);
		}
		else
		{
			setBackground(dropZoneCol);
			setOpaque(true);
		}
	}
	
	static Color dropZoneCol = new Color(224, 224, 224);
	static MouseAdapter adapter = new MouseAdapter()
	{
		@SuppressWarnings("serial")
		public void mouseReleased(MouseEvent e)
		{
			PartLabel source = (PartLabel)e.getSource();
			source.requestFocus();
			PosterPartsEditor viewer = source.editor;
			Component comp = source.editor.partPanel.getComponentAt(SwingUtilities.convertPoint(source, e.getPoint(), source.editor.partPanel));
			final PartLabel label = comp != null && comp instanceof PartLabel ? (PartLabel)comp : null;
			//Point p = label != null ? SwingUtilities.convertPoint(source, e.getPoint(), label) : null;
			
			if (label != null && (viewer.dragSource == null || viewer.dragSource == label))
			{
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1)
				{
					for (Component partLabel : label.editor.partPanel.getComponents())
						if (partLabel instanceof PartLabel)
							((PartLabel)partLabel).setSelected(false);
					label.setSelected(true);
				}
				else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {label.editor.host.onDocumentEditorRequest(label.part);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
//					final ManageComponent manageComp = label.viewer.win.manageComponent;
//					final List<MetaData> pages = viewer.getSelectedParts();
//					
//					JPopupMenu popMenu = new JPopupMenu();
//					if (pages.size() > 0 && !label.viewer.isLocked)
//						popMenu.add(new AbstractAction(
//							XMLResourceBundle.getBundledString("manageDeletePageLabel")) {public void actionPerformed(ActionEvent arg0)
//							{
//								//manageComp.handler.pagesDeleted(pages);
//							}});
//					popMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			else if (label != null && label.part == null)
			{
				try
				{
					List<PartLabel> parts = viewer.getSelectedPartLabels();
					PartLabel part = parts.size() > 0 ? parts.get(0) : null;
					if (part != null)
					{
						if (part.pj != label.pj || label.pi < part.pi || label.pi > part.pi+1)
						{
							label.editor.host.getActionListener().onMovePartsRequest(label.editor.book, part.part, label.pi, label.pj, label.insertRow);
						}
					}
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
			
			viewer.dragSource = null;
			setMovedIndex(source.editor, null);
			viewer.partPanel.repaint();
		}
		
		public void mousePressed(MouseEvent e)
		{
			PartLabel label = (PartLabel)e.getSource();
			if (label.editor.readOnly)
				return;
			label.editor.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.editor);
			label.editor.dragSource = label;
			
			if (!label.selected)
			{
				for (Component partLabel : label.editor.partPanel.getComponents())
					if (partLabel instanceof PartLabel)
						((PartLabel)partLabel).setSelected(false);
				label.setSelected(true);
				label.editor.partPanel.repaint();
			}
		}
		
		public void mouseDragged(MouseEvent e)
		{
			PartLabel label = (PartLabel)e.getSource();
			if (label.editor.dragSource == null)
				return;
			label.editor.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.editor);
			
			Point p = SwingUtilities.convertPoint(label, e.getPoint(), label.editor.partPanel);
			Component comp = label.editor.partPanel.getComponentAt(p);
			if (comp == null || !(comp instanceof PartLabel))
				setMovedIndex(label.editor, null);
			else
			{
				PartLabel under = (PartLabel)label.editor.partPanel.getComponentAt(p);
				if (under.part == null)
					setMovedIndex(label.editor, under);
				else setMovedIndex(label.editor, null);
			}
			
			label.editor.repaint();
		}
		
		void setMovedIndex(PosterPartsEditor viewer, PartLabel part)
		{
			if (viewer.lastMovedComp != null)
				viewer.lastMovedComp.setBorder(null);
			if (part != null)
			{
				part.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.blue));
			}
			viewer.lastMovedComp = part;
		}
	};
	
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