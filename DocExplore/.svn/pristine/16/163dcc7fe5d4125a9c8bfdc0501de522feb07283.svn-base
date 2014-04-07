package org.interreg.docexplore.management.process;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.interreg.docexplore.gui.EditableList;
import org.interreg.docexplore.gui.LooseGridLayout;

@SuppressWarnings("serial")
public class FilterSequencePanel extends EditableList
{
	static class SequenceElement
	{
		Filter filter;
		FilterParameter [] params;
		JPanel [] panels;
		JPanel fullPanel;
		
		public SequenceElement(Filter filter)
		{
			this.filter = filter;
			this.params = filter.getParameters();
			this.panels = new JPanel [params.length];
			this.fullPanel = new JPanel(new LooseGridLayout(0, 1, 10, 10, true, false));
			for (int i=0;i<params.length;i++)
			{
				JPanel panel = params[i].createPanel();
				panels[i] = panel;
				fullPanel.add(panel);
			}
		}
		
		public void apply(BufferedImage in, BufferedImage out)
		{
			Object [] paramObjs = new Object [params.length];
			for (int i=0;i<params.length;i++)
				paramObjs[i] = params[i].getValue(panels[i]);
			filter.apply(in, out, paramObjs);
		}
		
		public String toString() {return filter.getName();}
	}
	
	FilterBank bank;
	JPanel paramPanel;
	
	public FilterSequencePanel(FilterBank bank, JPanel paramPanel)
	{
		super(new DefaultListCellRenderer(), "add", "remove", "up", "down");
		
		this.bank = bank;
		this.paramPanel = paramPanel;
	}

	public void itemSelected(Object object)
	{
		SequenceElement elem = (SequenceElement)object;
		paramPanel.removeAll();
		if (elem != null)
		{
			paramPanel.add(elem.fullPanel);
			elem.fullPanel.invalidate();
		}
		paramPanel.getTopLevelAncestor().validate();
		paramPanel.repaint();
	}

	public void addItemRequested(ActionEvent e)
	{
		JPopupMenu menu = bank.buildMenu(this);
		Component comp = (Component)e.getSource();
		menu.show(comp, comp.getWidth()/2, comp.getHeight()/2);
	}

	public void addItem(Object item)
	{
		Filter filter = (Filter)item;
		super.addItem(new SequenceElement(filter));
	}

	public void itemsRemoved(Object[] items) {repaint();}
	public void itemAdded(Object items) {repaint();}
}
