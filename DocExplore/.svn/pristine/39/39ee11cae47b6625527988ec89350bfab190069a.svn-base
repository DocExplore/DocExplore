package org.interreg.docexplore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class ExpandingItemList extends JPanel
{
	private static final long serialVersionUID = 7037284856951544790L;
	
	public ExpandingItemList()
	{
		super(new LooseGridLayout(0, 1, 0, 0, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, false));
	}

	public void addItem(ExpandingItem item)
	{
		add(item);
		item.list = this;
	}
	public void removeItem(ExpandingItem item)
	{
		remove(item);
		item.list = null;
	}
	
	public void contractAll()
	{
		for (Component comp : getComponents())
			((ExpandingItem)comp).contract();
	}
	
	@SuppressWarnings("serial")
	public static void main(String [] args)
	{
		JFrame win = new JFrame("test");
		win.setPreferredSize(new Dimension(640, 480));
		ExpandingItemList list = new ExpandingItemList();
		win.add(new JScrollPane(list));
		for (int i=0;i<5;i++)
		{
			final int index = i;
			list.addItem(new ExpandingItem()
			{
				{fillContractedState();}
				
				protected void fillExpandedState()
				{
					setLayout(new BorderLayout());
					add(new JLabel("Comp "+index, SwingConstants.LEFT), BorderLayout.NORTH);
					add(new JLabel("Comp "+index, SwingConstants.CENTER), BorderLayout.CENTER);
					add(new JLabel("Comp "+index, SwingConstants.RIGHT), BorderLayout.SOUTH);
					setBackground(index%2 == 0 ? new Color(.9f, .9f, 1) : new Color(.9f, 1, .9f));
				}
				
				protected void fillContractedState()
				{
					setLayout(new FlowLayout(FlowLayout.LEFT));
					add(new JLabel("Comp "+index));
					setBackground(index%2 == 0 ? new Color(.95f, .95f, 1) : new Color(.95f, 1, .95f));
				}
			});
		}
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setVisible(true);
	}
}
