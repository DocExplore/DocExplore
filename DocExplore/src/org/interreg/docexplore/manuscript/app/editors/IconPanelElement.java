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
