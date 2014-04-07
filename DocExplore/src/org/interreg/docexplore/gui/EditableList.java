package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class EditableList extends JPanel
{
	JList list;
	DefaultListModel model;
	
	public EditableList(ListCellRenderer renderer, 
		String addTooltip,  
		String removeTooltip, 
		String upTooltip, 
		String downTooltip)
	{
		super(new BorderLayout());
		
		model = new DefaultListModel();
		list = new JList(model);
		list.setCellRenderer(renderer);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		JPanel listBottomButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listBottomButtons.add(new IconButton("add-24x24.png", 
			addTooltip, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					addItemRequested(e);
				}
			}));
		listBottomButtons.add(new IconButton("remove-24x24.png", 
			removeTooltip, new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					int [] selected = list.getSelectedIndices();
					if (selected.length == 0) return;
					Object [] items = new Object [selected.length];
					for (int i=0;i<items.length;i++)
						items[i] = model.get(selected[i]);
					model.removeRange(selected[0], selected[selected.length-1]);
					itemsRemoved(items);
				}
			}));
		
		JPanel listRightButtons = new JPanel(new VerticalFlowLayout());
		listRightButtons.add(new IconButton("up-24x24.png", 
			upTooltip, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int [] selected = list.getSelectedIndices();
				if (selected.length==0 || selected[0]==0) return;
				Object element = model.get(selected[0]-1);
				list.setValueIsAdjusting(true);
				model.removeElementAt(selected[0]-1);
				model.add(selected[selected.length-1], element);
				list.setSelectionInterval(selected[0]-1, selected[selected.length-1]-1);
				list.setValueIsAdjusting(false);
				
				selected = list.getSelectedIndices();
				Object [] items = new Object [selected.length];
				for (int i=0;i<items.length;i++)
					items[i] = model.get(selected[i]);
				itemsPushedUp(items);
			}
		}));
		listRightButtons.add(new IconButton("down-24x24.png", 
			downTooltip, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int [] selected = list.getSelectedIndices();
				if (selected.length==0 || selected[selected.length-1]==model.size()-1) return;
				Object element = model.get(selected[selected.length-1]+1);
				list.setValueIsAdjusting(true);
				model.removeElementAt(selected[selected.length-1]+1);
				model.insertElementAt(element, selected[0]);
				list.setSelectionInterval(selected[0]+1, selected[selected.length-1]+1);
				list.setValueIsAdjusting(false);
				
				selected = list.getSelectedIndices();
				Object [] items = new Object [selected.length];
				for (int i=0;i<items.length;i++)
					items[i] = model.get(selected[i]);
				itemsPushedDown(items);
			}
		}));
		
		final JScrollPane listScrollPane = new JScrollPane(list, 
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(listScrollPane, BorderLayout.CENTER);
		add(listBottomButtons, BorderLayout.SOUTH);
		add(listRightButtons, BorderLayout.EAST);
		
		list.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting()) return;
				itemSelected(list.getSelectedIndex() < 0 ? null : 
					model.get(list.getSelectedIndex()));
			}
		});
	}
	
	public void addItem(Object item)
	{
		model.addElement(item);
		itemAdded(item);
	}
	public void clear() {model.clear();}
	public ListModel getModel() {return model;}
	
	public void itemsRemoved(Object [] items) {}
	public void itemAdded(Object items) {}
	public void itemSelected(Object object) {}
	public void itemsPushedUp(Object [] items) {}
	public void itemsPushedDown(Object [] items) {}
	public void addItemRequested(ActionEvent e) {}
}
