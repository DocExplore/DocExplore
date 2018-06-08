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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.ImageUtils;

public class PosterPartElement extends DragDropPanel
{
	private static final long serialVersionUID = -3005585743647321920L;
	
	MetaData part;
	int pi, pj;
	JLabel mini;
	PosterPartsEditor editor;
	boolean selected = false;
	boolean insertRow;
	
	public PosterPartElement(final PosterPartsEditor editor, final MetaData part, int pi, int pj)
	{
		this(editor, part, pi, pj, false);
	}
	public PosterPartElement(final PosterPartsEditor editor, final MetaData part, int pi, int pj, boolean insertRow)
	{
		super(new BorderLayout(), part != null, part == null);
		
		this.editor = editor;
		this.part = part;
		this.pi = pi;
		this.pj = pj;
		this.insertRow = insertRow;
		this.mini = part == null ? new JLabel("") : new JLabel(ImageUtils.getIcon("page_search-48x48.png"));
		mini.setHorizontalAlignment(SwingConstants.CENTER);
		mini.setVerticalAlignment(SwingConstants.CENTER);
		//setPreferredSize(new Dimension(72, 80));
		add(mini, BorderLayout.CENTER);
		
		addMouseListener(adapter);
		if (part != null)
		{
			try {setToolTipText(part.getMetaDataString(editor.host.getAppHost().getLink().sourceKey));}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, true);}
			addMouseMotionListener(adapter);
			setFocusable(true);
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(borderThickness, borderThickness, borderThickness, borderThickness));
		}
		else
		{
			mini.setPreferredSize(new Dimension(24, 24));
			setBackground(dropZoneCol);
			setOpaque(true);
		}
		setReadOnly(editor.readOnly);
	}
	
	static Color dropZoneCol = new Color(224, 224, 224);
	static MouseAdapter adapter = new MouseAdapter()
	{
		public void mouseReleased(MouseEvent e)
		{
			PosterPartElement source = (PosterPartElement)e.getSource();
			PosterPartsEditor viewer = source.editor;
			Component comp = source.editor.partPanel.getComponentAt(SwingUtilities.convertPoint(source, e.getPoint(), source.editor.partPanel));
			final PosterPartElement label = comp != null && comp instanceof PosterPartElement ? (PosterPartElement)comp : null;
			//Point p = label != null ? SwingUtilities.convertPoint(source, e.getPoint(), label) : null;
			
			if (label != null && label.part == null && e.getButton() == MouseEvent.BUTTON3)
			{
				if (label.editor.readOnly)
					return;
				viewer.host.getAppHost().getActionRequestListener().onAddEmptyPartRequest(label.editor.book, label.pi, label.pj, label.insertRow);
			}
			else if (source.part == null)
				return;
			else if (label != null && (viewer.dragSource == null || viewer.dragSource == label))
			{
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					try {label.editor.host.onDocumentEditorRequest(label.part);}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
			}
		}
		
		public void mousePressed(MouseEvent e)
		{
			PosterPartElement label = (PosterPartElement)e.getSource();
			label.editor.dragPos = SwingUtilities.convertPoint(label, e.getPoint(), label.editor);
			label.editor.dragSource = label;
			
			if (!label.selected)
			{
				for (Component partLabel : label.editor.partPanel.getComponents())
					if (partLabel instanceof PosterPartElement)
						((PosterPartElement)partLabel).setSelected(false);
				label.setSelected(true);
				label.editor.partPanel.repaint();
			}
		}
	};
	
	static int borderThickness = 4;
	public void setSelected(boolean selected)
	{
		if (this.selected == selected)
			return;
		
		this.selected = selected;
		if (selected)
		{
			setOpaque(true);
			setBorder(BorderFactory.createLineBorder(GuiConstants.selectionColor, borderThickness));
		}
		else
		{
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(borderThickness, borderThickness, borderThickness, borderThickness));
		}
	}
	
	public void setReadOnly(boolean b)
	{
		dragsEnabled = part != null;
		dropsEnabled = !b && part == null;
	}
	
	@Override public Collection<Object> setDraggedData(int x, int y)
	{
		List<Object> data = new ArrayList<Object>(1);
		data.add(this);
		return data;
	}
	@Override public void onIncomingDrag(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		if (data.size() == 1)
			setBackground(GuiConstants.actionColor);
	}
	@Override public void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y)
	{
		setBackground(dropZoneCol);
		if (source instanceof PosterPartElement)
		{
			PosterPartElement element = (PosterPartElement)source;
			if (element.editor == editor)
				editor.host.getAppHost().getActionRequestListener().onMovePartsRequest(editor.book, element.part, pi, pj, insertRow);
		}
		else if (source == null)
		{
			
		}
	}
	@Override public void onDragExited(Collection<Object> data, DragDropPanel source)
	{
		setBackground(dropZoneCol);
	}
}