/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
