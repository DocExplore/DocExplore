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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.search.SearchHandler.SearchSummary;

public class ResultPanel extends JPanel
{
	private static final long serialVersionUID = 4925686361745901459L;
	
	static class ResultLine extends JPanel
	{
		private static final long serialVersionUID = -4437376532734868706L;

		SearchResult result;
		
		public ResultLine(SearchResult result)
		{
			super(new LooseGridLayout(2, 1, 0, 0, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
			
			this.result = result;
			add(result);
		}
	}
	
	SearchHandler handler;
	
	ResultMerger results;
	int offset, limit;
	
	Vector<ResultLine> resultLines;
	JPanel linePanel;
	JPanel navigationPanel;
	
	public ResultPanel(final SearchHandler handler)
	{
		super(new LooseGridLayout(2, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		
		this.handler = handler;
		this.results = null;
		this.offset = 0;
		this.limit = 10;
		this.resultLines = new Vector<ResultLine>();
		this.linePanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		this.navigationPanel = new JPanel(new LooseGridLayout(1, 0, 5, 5, false, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		
		JPanel buttonPanel = new JPanel(new LooseGridLayout(1, 2, 5, 5, false, false, 
			SwingConstants.LEFT, SwingConstants.TOP, false, false));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCloseLabel")) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0)
			{
//				clearResults();
				handler.win.removeLeftPanel(handler.win.searchComponent);
			}
		}));
		navigationPanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		linePanel.add(new JLabel(XMLResourceBundle.getBundledString("noResultsMsg")));
		
		add(linePanel);
		add(navigationPanel);
		add(buttonPanel);
		
		setPreferredSize(new Dimension(350, 800));
	}
	
	int nButtons = 15;
	void fillNavigation()
	{
		navigationPanel.removeAll();
		if (results == null || results.size() <= limit)
		{
			navigationPanel.setBorder(null);
			return;
		}
		navigationPanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		
		int nPages = (results.size()-1)/limit+1;
		int currentPage = offset/limit;
		int nearPages = nButtons/2-2;
		
		int nearStart = currentPage-nearPages;
		int nearEnd = currentPage+nearPages;
		
		if (nearStart < 2) {nearEnd += 2-nearStart; nearStart = 2;}
		if (nearEnd > nPages-3) {nearStart -= nearEnd-(nPages-3); nearEnd = nPages-3;}
		if (nearStart <= 2) nearStart = 1;
		if (nearEnd >= nPages-3) nearEnd = nPages-2;
		
		navigationPanel.add(createNavigationButton(0));
		if (nearStart > 2)
			navigationPanel.add(createNavigationButton(nearStart/2, "..."));
		for (int i=nearStart;i<=nearEnd;i++)
			navigationPanel.add(createNavigationButton(i));
		if (nearEnd < nPages-2)
			navigationPanel.add(createNavigationButton((nPages-1+nearEnd)/2, "..."));
		if (nPages > 1)
			navigationPanel.add(createNavigationButton(nPages-1));
	}
	
	JComponent createNavigationButton(final int page) {return createNavigationButton(page, ""+(page+1));}
	JComponent createNavigationButton(final int page, String label)
	{
		label = "<u>"+label+"</u>";
		int currentPage = offset/limit;
		if (page == currentPage)
			label = "<b>"+label+"</b>";
		label = "<html>"+label+"</html>";
		
		JComponent res = null;
		if (page != currentPage)
		{
			final JLabel button = new JLabel(label);
			button.setForeground(Color.blue);
			button.addMouseListener(new MouseAdapter()
			{
				public void mouseEntered(MouseEvent e) {button.getTopLevelAncestor().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
				public void mouseExited(MouseEvent e) {button.getTopLevelAncestor().setCursor(Cursor.getDefaultCursor());}
				public void mouseClicked(MouseEvent e) {button.getTopLevelAncestor().setCursor(Cursor.getDefaultCursor()); setOffset(limit*page);}
			});
			res = button;
		}
		else res = new JLabel(label);
		
		res.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		return res;
	}
	
	ResultLine getLine(int i) throws DataLinkException
	{
		SearchResult result = new SearchResult(results.getStub(i), 
			SearchResult.highlight(results.getMetaDataText(i), results.criteria, results.relevance), 
			results.getScore(i), 0);
		result.handler = handler;
		return new ResultLine(result);
	}
	
	public void setOffset(int offset)
	{
		if (resultLines.size() == 0)
			linePanel.remove(0);
		else while (resultLines.size() > 0)
		{
			linePanel.remove(resultLines.get(resultLines.size()-1));
			resultLines.remove(resultLines.size()-1);
		}
		
		this.offset = offset;
		
		if (results == null)
			linePanel.add(new JLabel(XMLResourceBundle.getBundledString("noResultsMsg")));
		else for (int i=offset;i<offset+limit;i++)
			if (i < results.size())
		{
			try
			{
				ResultLine line = getLine(i);
				resultLines.add(line);
				linePanel.add(line);
			}
			catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		}
		
		fillNavigation();
		
		getParent().validate();
	}
	
	public void clearResults() {setResults(null);}
	public void setResults(SearchSummary summary)
	{
		this.results = summary!=null && summary.results.size()>0 ? new ResultMerger(summary) : null;
		setOffset(0);
	}
}
