/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.search;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ExpandingPanel;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class SearchPanel extends ExpandingPanel
{
	private static final long serialVersionUID = 1490285818870292459L;

	static class SearchLine extends JPanel
	{
		private static final long serialVersionUID = -7655919799834227872L;
		
		SearchCriteria criteria;
		
		SearchLine(SearchCriteria criteria)
		{
			super(new FlowLayout(FlowLayout.LEFT, 5, 5)
				/*new LooseGridLayout(1, 0, 5, 5, false, false, 
				SwingConstants.LEFT, SwingConstants.TOP, true, true)*/);
			this.criteria = criteria;
			add(criteria, 0);
		}
		
		void addRemoveButton(final SearchPanel sc)
		{
			add(new IconButton("remove-24x24.png", new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (sc.searchLines.get(sc.searchLines.size()-1) == SearchLine.this)
						sc.searchLines.get(sc.searchLines.size()-2).addAddButton(sc);
					
					sc.searchLines.remove(SearchLine.this);
					sc.linePanel.remove(SearchLine.this);
					
					sc.getParent().validate();
				}
			}));
		}
		
		void addAddButton(final SearchPanel sc)
		{
			add(new IconButton("add-24x24.png", new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					sc.addLine();
					removeAddButton(sc);
				}
			}));
		}
		
		void removeAddButton(final SearchPanel sc)
		{
			remove(sc.searchLines.get(0) == this ? 1 : 2);
		}
	}
	
	SearchHandler handler;
	Vector<SearchLine> searchLines;
	JPanel linePanel;
	JButton searchButton;
	
	JCheckBox bookBox, pageBox, regionBox, metaDataBox;
	JRadioButton allMethod, anyMethod;
	
	public SearchPanel(SearchHandler handler)
	{
		super(XMLResourceBundle.getBundledString("searchLabel"));
		
		this.handler = handler;
		this.searchLines = new Vector<SearchLine>();
		this.searchButton = new JButton(XMLResourceBundle.getBundledString("searchLabel"));
		this.linePanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false));
		
		getContentPane().setLayout(new LooseGridLayout(2, 1, 5, 5, false, false));
		JPanel buttonPanel = new JPanel(new LooseGridLayout(1, 2, 5, 5, true, false, 
			SwingConstants.LEFT, SwingConstants.TOP, true, false));
		buttonPanel.add(searchButton);
		buttonPanel.add(new JButton(new AbstractAction(
			XMLResourceBundle.getBundledString("clearLabel"))
		{
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0)
			{
				while (searchLines.size() > 1)
					((JButton)searchLines.get(searchLines.size()-1).
						getComponent(1)).doClick();
			}
		}));
		
		JPanel typePanel = new JPanel(new BorderLayout());
		JPanel typeBoxPanel = new JPanel(new FlowLayout());
		typePanel.add(new JLabel(XMLResourceBundle.getBundledString("searchTypeLabel")), BorderLayout.NORTH);
		typeBoxPanel.add(this.bookBox = new JCheckBox(XMLResourceBundle.getBundledString("searchBooksLabel"), true));
		typeBoxPanel.add(this.pageBox = new JCheckBox(XMLResourceBundle.getBundledString("searchPagesLabel"), true));
		typeBoxPanel.add(this.regionBox = new JCheckBox(XMLResourceBundle.getBundledString("searchRoisLabel"), true));
		//typeBoxPanel.add(this.metaDataBox = new JCheckBox(XMLResourceBundle.getBundledString("searchMetaDataLabel"), true));
		typePanel.add(typeBoxPanel, BorderLayout.SOUTH);
		
		JPanel methodPanel = new JPanel(new BorderLayout());
		ButtonGroup methodGroup = new ButtonGroup();
		methodPanel.add(this.allMethod = new JRadioButton(XMLResourceBundle.getBundledString("searchMethodAllLabel"), true), BorderLayout.NORTH);
		methodPanel.add(this.anyMethod = new JRadioButton(XMLResourceBundle.getBundledString("searchMethodAnyLabel"), false), BorderLayout.SOUTH);
		methodGroup.add(allMethod);
		methodGroup.add(anyMethod);
		
		getContentPane().add(typePanel);
		getContentPane().add(methodPanel);
		getContentPane().add(linePanel);
		getContentPane().add(buttonPanel);
	}
	
	void updateKeys()
	{
		for (SearchLine line : searchLines)
			line.criteria.setKeys(handler.getSearchKeys());
	}
	
	void addLine()
	{
		SearchLine line = new SearchLine(new SearchCriteria(handler.getSearchKeys()));
		searchLines.add(line);
		if (searchLines.size() > 1) line.addRemoveButton(this);
		line.addAddButton(this);
		
		linePanel.add(line);
		
		getParent().validate();
	}
}
