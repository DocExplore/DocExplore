/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.util.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;

@SuppressWarnings("serial")
public class HistoryPanel extends JPanel implements HistoryManager.HistoryListener
{
	static class ActionElement extends JPanel
	{
		HistoryPanel panel;
		int index;
		ReversibleAction action;
		boolean selected = false;
		
		ActionElement(HistoryPanel panel, ReversibleAction action, int index)
		{
			super(new BorderLayout());
			
			this.panel = panel;
			this.index = index;
			this.action = action;
			if (index == panel.manager.cursor-1)
				add(new JLabel("<html><b>"+action.description()+"</b></html>"));
			else add(new JLabel(action.description()));
		}
		
		void setSelected(boolean selected)
		{
			if (selected == this.selected)
				return;
			this.selected = selected;
			if (selected)
			{
				setOpaque(true);
				setBackground(Color.blue);
			}
			else setOpaque(false);
			repaint();
		}
	}
	
	HistoryManager manager;
	JPanel content;
	JScrollPane scrollPane;
	List<ActionElement> elements = new Vector<HistoryPanel.ActionElement>();
	
	public HistoryPanel(final HistoryManager manager)
	{
		super(new BorderLayout());
		
		this.manager = manager;
		this.content = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		this.scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(200, 500));
		
		manager.addHistoryListener(this);
		historyChanged(manager);
		
		content.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				Component comp = content.getComponentAt(e.getPoint());
				if (comp == null || !(comp instanceof ActionElement))
				{
					setSelectedIndex(-1);
					return;
				}
				setSelectedIndex(((ActionElement)comp).index);
				
				if (e.getClickCount() == 2)
				{
					final int target = ((ActionElement)comp).index;
					new Thread() {public void run()
					{
						try
						{
							if (target+1 < manager.cursor)
								while (manager.cursor > target+1)
									manager.undo();
							else if (target+1 > manager.cursor)
								while (manager.cursor < target+1)
									manager.redo();
						}
						catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
					}}.start();
				}
			}
		});
	}
	
	void setSelectedIndex(int index)
	{
		for (int i=0;i<elements.size();i++)
			elements.get(i).setSelected(i == index);
	}

	public void historyChanged(HistoryManager manager)
	{
		content.removeAll();
		elements.clear();
		
		int cnt = 0;
		for (ReversibleAction action : manager.history)
		{
			ActionElement element = new ActionElement(this, action, cnt++);
			content.add(element);
			elements.add(element);
		}
		scrollPane.validate();
		scrollPane.repaint();
	}
}
