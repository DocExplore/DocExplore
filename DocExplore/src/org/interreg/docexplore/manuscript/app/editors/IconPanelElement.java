/**
Copyright LITIS/EDA 2018
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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class IconPanelElement<DataType> extends DragDropPanel
{
	public final IconPanelEditor<DataType> editor;
	public final DataType data;
	public final JLabel mini;
	private final JLabel name;
	boolean selected = false;
	
	IconPanelElement(final IconPanelEditor<DataType> editor, DataType data)
	{
		super(new BorderLayout(), editor.iconsAcceptDrags(), editor.iconsAcceptDrops());
		
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setBackground(GuiConstants.emptyColor);
		setOpaque(false);
		
		this.editor = editor;
		this.data = data;
		
		this.mini = new JLabel(ImageUtils.getIcon("page_search-48x48.png"));
		mini.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		mini.setPreferredSize(new Dimension(editor.iconSize, editor.iconSize));
		mini.setHorizontalAlignment(SwingConstants.CENTER);
		mini.setVerticalAlignment(SwingConstants.CENTER);
		add(mini, BorderLayout.NORTH);
		
		this.name = new JLabel("<html><center><div width="+editor.iconSize+"><b>"+editor.labelFor(data)+"</b></div></center></html>");
		name.setVerticalAlignment(SwingConstants.TOP);
		name.setForeground(Color.black);
		add(name, BorderLayout.CENTER);
		
		addMouseListener(new MouseAdapter()
		{
			@Override public void mousePressed(MouseEvent e)
			{
				boolean ctrl = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
				boolean shift = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
				
				if (ctrl && !shift)
				{
					setSelected(!selected);
					editor.lastSelected = IconPanelElement.this;
				}
				else if (!ctrl && shift)
				{
					editor.unselectAll();
					int from = editor.lastSelected == null ? 0 : editor.iconIndex(editor.lastSelected);
					int to = editor.iconIndex(IconPanelElement.this);
					for (int i=Math.min(from, to);i<=Math.max(from, to);i++)
						editor.getIcon(i).setSelected(true);
				}
				else if (ctrl && shift)
				{
					int from = editor.lastSelected == null ? 0 : editor.iconIndex(editor.lastSelected);
					int to = editor.iconIndex(IconPanelElement.this);
					for (int i=Math.min(from, to);i<=Math.max(from, to);i++)
						editor.getIcon(i).setSelected(true);
					editor.lastSelected = IconPanelElement.this;
				}
				else if (!selected && !ctrl && !shift)
				{
					editor.unselectAll();
					setSelected(true);
					editor.lastSelected = IconPanelElement.this;
				}
			}
			
			@Override public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
					editor.onIconOpened(IconPanelElement.this);
			}
		});
	}
	
	public boolean isLeftOf(int x, int y) {return getX()+getWidth()/2-x < -getWidth()/4;}
	public boolean isRightOf(int x, int y) {return getX()+getWidth()/2-x > getWidth()/4;}
	public boolean isHigherThan(int x, int y) {return getY()+getHeight()/2-y < -getHeight()/4;}
	public boolean isLowerThan(int x, int y) {return getY()+getHeight()/2-y > getHeight()/4;}
	public boolean isCenteredOn(int x, int y) {return !isLeftOf(x, y) && !isRightOf(x, y) && !isHigherThan(x, y) && !isLowerThan(x, y);}
	
	void setSelected(boolean selected)
	{
		if (this.selected == selected)
			return;
		this.selected = selected;
		if (selected)
		{
			setBackground(GuiConstants.selectionColor);
			setOpaque(true);
			name.setForeground(Color.white);
		}
		else
		{
			setBackground(GuiConstants.emptyColor);
			setOpaque(false);
			name.setForeground(Color.black);
		}
		repaint();
	}

	@SuppressWarnings("unchecked")
	@Override public Collection<Object> setDraggedData(int x, int y) {return (Collection<Object>)editor.getSelectedElements();}

	@Override public void onIncomingDrag(Collection<Object> data, DragDropPanel source, int x, int y)
	{
	}
	@Override public void onIncomingDrop(Collection<Object> data, DragDropPanel source, int x, int y)
	{
	}
	@Override public void onDragExited(Collection<Object> data, DragDropPanel source)
	{
	}
}
