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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.annotate.TagHolder;
import org.interreg.docexplore.management.search.SearchHandler.MetaDataKeyEntry;
import org.interreg.docexplore.management.search.SearchHandler.SearchSummary;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.Pair;

/**
 * A component that displays an arbitrary number of search criteria fields and a list of
 * results.
 * @author Burnett
 */
public class SearchComponent extends JPanel
{
	private static final long serialVersionUID = 3964599118359675255L;
	
	SearchHandler handler;
	//SearchPanel searchPanel;
	ResultPanel resultPanel;
	JLabel animLabel;
	
	/**
	 * Creates a new search component with a given handler.
	 * @param handler A handler for the search.
	 */
	public SearchComponent(SearchHandler handler)
	{
		super(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, true, false));
		
		this.handler = handler;
		//this.searchPanel = new SearchPanel(handler);
		this.resultPanel = new ResultPanel(handler);
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
//		searchPanel.getContentPane().setLayout(new LooseGridLayout(0, 1, 2, 2, false, false, 
//			SwingConstants.LEFT, SwingConstants.TOP, true, false));
//		
//		searchPanel.searchButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				new Thread() {public void run() {doSearch();}}.start();
//			}
//		});
		
		this.animLabel = new JLabel();
		
		JPanel mainPanel = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, 
			SwingConstants.LEFT, SwingConstants.TOP,
			true, false));
		mainPanel.setOpaque(false);
//		mainPanel.add(searchPanel);
		mainPanel.add(animLabel);
		mainPanel.add(resultPanel);
		mainPanel.setBackground(Color.white);
		JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		scrollPane.setBackground(Color.white);
		add(scrollPane, BorderLayout.CENTER);
		
//		searchPanel.addLine();
//		searchPanel.expand();
	}
	
	/**
	 * Reflects updates in the list of search keys. This will trigger a call to the 
	 * {@link SearchHandler#getSearchKeys() getSearchKeys} method of the current handler.
	 */
	public void updateKeys()
	{
//		searchPanel.updateKeys();
	}
	
//	private synchronized void doSearch()
//	{
//		animLabel.setText(XMLResourceBundle.getBundledString("searchingMsg"));
//		final SearchSummary [] pointer = new SearchSummary [1];
//		
//		Thread searchThread = new Thread()
//		{
//			public void run()
//			{
//				List<Pair<MetaDataKeyEntry, String> > criteria = new Vector<Pair<MetaDataKeyEntry, String>>();
//				Vector<String> keyWords = new Vector<String>();
//				
//				for (SearchLine line : searchPanel.searchLines)
//					if (line.criteria.searchValue.getText().trim().length() > 0)
//					{
//						criteria.add(new Pair<MetaDataKeyEntry, String>(
//							(MetaDataKeyEntry)line.criteria.searchKey.getSelectedItem(), line.criteria.searchValue.getText()));
//						keyWords.add(line.criteria.searchValue.getText());
//					}
//				
//				Set<Class<? extends AnnotatedObject>> types = new HashSet<Class<? extends AnnotatedObject>>();
//				if (searchPanel.bookBox.isSelected()) types.add(Book.class);
//				if (searchPanel.pageBox.isSelected()) types.add(Page.class);
//				if (searchPanel.regionBox.isSelected()) types.add(Region.class);
//				//if (searchPanel.metaDataBox.isSelected()) types.add(MetaData.class);
//				
//				pointer[0] = handler.doSearch(criteria, searchPanel.anyMethod.isSelected(), types);
//			}
//		};
//		searchThread.start();
//		
//		float val = 0f, inc = .05f;
//		while (searchThread.isAlive())
//		{
//			animLabel.setForeground(new Color(val, val, val));
//			val += inc;
//			if (val > 1) {val = 1f; inc = -.05f;}
//			else if (val < 0) {val = 0f; inc = .05f;}
//			
//			try {Thread.sleep(50);} catch (InterruptedException e) {}
//		}
//		animLabel.setText(null);
//		
//		resultPanel.setResults(pointer[0]);
//		//resultPanel.expand();
//		validate();
//	}
	
	public String lastTerm = null;
	public synchronized void doSearch(final String term)
	{
		if (!isShowing())
			handler.win.addLeftPanel(SearchComponent.this, .25);
		
		GuiUtils.blockUntilComplete(new Runnable() {public void run()
		{
			if (lastTerm == null || !lastTerm.equals(term))
			{
				animLabel.setText(XMLResourceBundle.getBundledString("searchingMsg"));
				final SearchSummary [] pointer = new SearchSummary [1];
				lastTerm = term;
				
				if (term.length() > 0)
				{
					Thread searchThread = new Thread()
					{
						public void run()
						{
							pointer[0] = handler.doSearch(term);
						}
					};
					searchThread.start();
					
					float val = 0f, inc = .05f;
					while (searchThread.isAlive())
					{
						animLabel.setForeground(new Color(val, val, val));
						val += inc;
						if (val > 1) {val = 1f; inc = -.05f;}
						else if (val < 0) {val = 0f; inc = .05f;}
						
						try {Thread.sleep(50);} catch (InterruptedException e) {}
					}
				}
				animLabel.setText(null);
				
				resultPanel.setResults(pointer[0]);
			}
			
			validate();
		}}, handler.win);
	}
	
	@SuppressWarnings("serial")
	public void doSearchByAnnotationName() throws DataLinkException
	{
		final boolean [] ok = {false};
		final JDialog dialog = new JDialog(handler.win, XMLResourceBundle.getBundledString("annotateKeyLabel"), true);
		JPanel mainPanel = new JPanel(new BorderLayout());
		dialog.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("searchKeyLabel")), BorderLayout.NORTH);
		
		Collection<MetaDataKey> allKeys = handler.win.getDocExploreLink().getAllKeys();
		Set<MetaDataKeyEntry> keys = new TreeSet<MetaDataKeyEntry>(new Comparator<MetaDataKeyEntry>()
			{Collator collator = Collator.getInstance(Locale.getDefault());
			public int compare(MetaDataKeyEntry o1, MetaDataKeyEntry o2) {return collator.compare(o1.toString(), o2.toString());}});
		for (MetaDataKey key : allKeys)
			keys.add(new MetaDataKeyEntry(key));
		
		JComboBox keyBox = new JComboBox(keys.toArray());
		keyBox.setEditable(false);
		mainPanel.add(keyBox, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		final JButton okButton, cancelButton;
		buttonPanel.add(okButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) {
			public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(cancelButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {
			public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		okButton.setDefaultCapable(true);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
		mainPanel.getActionMap().put("ESC", new AbstractAction() {public void actionPerformed(ActionEvent e) {cancelButton.doClick();}});
		mainPanel.getActionMap().put("OK", new AbstractAction() {public void actionPerformed(ActionEvent e) {okButton.doClick();}});
		//keyBox.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {okButton.doClick();}});
		
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		if (!ok[0])
			return;
		
		final MetaDataKeyEntry value = (MetaDataKeyEntry)keyBox.getSelectedItem();
		if (!isShowing())
			handler.win.addLeftPanel(this, .25);
		
		GuiUtils.blockUntilComplete(new Runnable() {public void run()
		{
			animLabel.setText(XMLResourceBundle.getBundledString("searchingMsg"));
			final SearchSummary [] pointer = new SearchSummary [1];
				
			Thread searchThread = new Thread() {@SuppressWarnings("unchecked")
			public void run()
			{
				try
				{
					pointer[0] = handler.doSearch(
						Arrays.asList(new Pair<MetaDataKeyEntry, String>(value, "")),
						false,
						Arrays.asList(Book.class, Page.class, Region.class));
				}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
			}};
			searchThread.start();
			
			float val = 0f, inc = .05f;
			while (searchThread.isAlive())
			{
				animLabel.setForeground(new Color(val, val, val));
				val += inc;
				if (val > 1) {val = 1f; inc = -.05f;}
				else if (val < 0) {val = 0f; inc = .05f;}
				try {Thread.sleep(50);} catch (InterruptedException e) {}
			}
			animLabel.setText(null);
			resultPanel.setResults(pointer[0]);
			
			validate();
		}}, handler.win);
	}
	
	@SuppressWarnings("serial")
	public void doSearchByTag() throws DataLinkException
	{
		final boolean [] ok = {false};
		final JDialog dialog = new JDialog(handler.win, XMLResourceBundle.getBundledString("tagTagLabel"), true);
		JPanel mainPanel = new JPanel(new BorderLayout());
		dialog.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("searchTagLabel")), BorderLayout.NORTH);
		
		Collection<MetaData> allTags = handler.win.getDocExploreLink().tagKey.getMetaData(MetaData.textType);
		Set<TagHolder> keys = new TreeSet<TagHolder>(new Comparator<TagHolder>()
			{Collator collator = Collator.getInstance(Locale.getDefault());
			public int compare(TagHolder o1, TagHolder o2) {return collator.compare(o1.toString(), o2.toString());}});
		for (MetaData tag : allTags)
			keys.add(new TagHolder(tag));
		
		JComboBox tagBox = new JComboBox(keys.toArray());
		tagBox.setEditable(false);
		mainPanel.add(tagBox, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		final JButton okButton, cancelButton;
		buttonPanel.add(okButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) {
			public void actionPerformed(ActionEvent e) {ok[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(cancelButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {
			public void actionPerformed(ActionEvent e) {dialog.setVisible(false);}}));
		okButton.setDefaultCapable(true);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
		mainPanel.getActionMap().put("ESC", new AbstractAction() {public void actionPerformed(ActionEvent e) {cancelButton.doClick();}});
		mainPanel.getActionMap().put("OK", new AbstractAction() {public void actionPerformed(ActionEvent e) {okButton.doClick();}});
		//keyBox.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {okButton.doClick();}});
		
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		if (!ok[0])
			return;
		
		final TagHolder value = (TagHolder)tagBox.getSelectedItem();
		if (!isShowing())
			handler.win.addLeftPanel(this, .25);
		
		GuiUtils.blockUntilComplete(new Runnable() {public void run()
		{
			animLabel.setText(XMLResourceBundle.getBundledString("searchingMsg"));
			final SearchSummary [] pointer = new SearchSummary [1];
				
			Thread searchThread = new Thread() {@SuppressWarnings("unchecked")
				public void run()
				{
					try
					{
						pointer[0] = handler.doSearch(
							Arrays.asList(new Pair<MetaDataKeyEntry, String>(new MetaDataKeyEntry(handler.win.getDocExploreLink().tagKey), value.toString())),
							false,
							Arrays.asList(Book.class, Page.class, Region.class));
					}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				}};
			searchThread.start();
			
			float val = 0f, inc = .05f;
			while (searchThread.isAlive())
			{
				animLabel.setForeground(new Color(val, val, val));
				val += inc;
				if (val > 1) {val = 1f; inc = -.05f;}
				else if (val < 0) {val = 0f; inc = .05f;}
				try {Thread.sleep(50);} catch (InterruptedException e) {}
			}
			animLabel.setText(null);
			resultPanel.setResults(pointer[0]);
			
			validate();
		}}, handler.win);
	}
}
