package org.interreg.docexplore.management.manage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

@SuppressWarnings("serial")
public class TagManager extends JPanel
{
	DocExploreDataLink link;
	TagTableModel model;
	JButton deleteButton, mergeButton;
	JLabel mergeText;
	MetaData merging = null;
	
	public TagManager(final MainWindow win) throws DataLinkException
	{
		super(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.CENTER, SwingConstants.TOP, true, false));
		
		link = win.getDocExploreLink();
		
		model = new TagTableModel(link);
		final JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {public void valueChanged(ListSelectionEvent e)
		{
			if (e.getValueIsAdjusting())
				return;
			if (merging == null)
			{
				deleteButton.setEnabled(table.getSelectedRow() >= 0);
				mergeButton.setEnabled(table.getSelectedRow() >= 0);
			}
			else if (table.getSelectedRow() >= 0) 
			{
				try
				{
					final MetaData mergeTo = model.tags.get(table.getSelectedRow());
					if (JOptionPane.showConfirmDialog(TagManager.this, 
						"<html>"+XMLResourceBundle.getBundledString("tagMergeMessage").replace("%from", DocExploreDataLink.getBestTagName(merging)).replace("%to", DocExploreDataLink.getBestTagName(mergeTo))+"</html>", 
						XMLResourceBundle.getBundledString("keyMergeLabel"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					{
						deleteButton.setEnabled(table.getSelectedRow() >= 0);
						mergeButton.setEnabled(table.getSelectedRow() >= 0);
						mergeText.setText("");
						merging = null;
						return;
					}
					
					GuiUtils.blockUntilComplete(new ProgressRunnable()
					{
						float [] progress = {0};
						public void run()
						{
							try
							{
								merge(merging, mergeTo, progress);
								merging.remove();
							}
							catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
						}
						public float getProgress() {return progress[0];}
					}, TagManager.this);
					GuiUtils.blockUntilComplete(new Runnable() {public void run()
					{
						try {model.load();}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					}}, TagManager.this);
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				
				mergeText.setText("");
				merging = null;
				deleteButton.setEnabled(table.getSelectedRow() >= 0);
				mergeButton.setEnabled(table.getSelectedRow() >= 0);
				return;
			}
		}});
		
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(640, 480));
		add(scrollPane);
		
		JPanel mergePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mergePanel.add(deleteButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("keyDeleteLabel")) {public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				final MetaData tag = model.tags.get(table.getSelectedRow());
				if (JOptionPane.showConfirmDialog(TagManager.this, 
					"<html>"+XMLResourceBundle.getBundledString("tagDeleteMessage").replace("%from", DocExploreDataLink.getBestTagName(tag))+"</html>", 
					XMLResourceBundle.getBundledString("keyMergeLabel"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				{
					deleteButton.setEnabled(table.getSelectedRow() >= 0);
					mergeButton.setEnabled(table.getSelectedRow() >= 0);
					mergeText.setText("");
					merging = null;
					return;
				}
				GuiUtils.blockUntilComplete(new ProgressRunnable()
				{
					float [] progress = {0};
					public void run()
					{
						try
						{
							delete(tag, progress);
							tag.remove();
						}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					}
					public float getProgress() {return progress[0];}
				}, TagManager.this);
				GuiUtils.blockUntilComplete(new Runnable() {public void run()
				{
					try {model.load();}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				}}, TagManager.this);
				deleteButton.setEnabled(table.getSelectedRow() >= 0);
				mergeButton.setEnabled(table.getSelectedRow() >= 0);
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}}));
		mergePanel.add(mergeButton = new JButton(new AbstractAction(XMLResourceBundle.getBundledString("keyMergeLabel")) {public void actionPerformed(ActionEvent arg0)
		{
			if (merging == null)
			{
				merging = model.tags.get(table.getSelectedRow());
				try {mergeText.setText("<html>"+XMLResourceBundle.getBundledString("tagSelectMessage").replace("%from", DocExploreDataLink.getBestTagName(merging))+"</html>");}
				catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				deleteButton.setEnabled(false);
			}
			else
			{
				mergeText.setText("");
				merging = null;
				deleteButton.setEnabled(table.getSelectedRow() >= 0);
			}
		}}));
		mergeButton.setEnabled(false);
		mergePanel.add(mergeText = new JLabel(""));
		add(mergePanel);
	}
	
	private void merge(MetaData from, MetaData to, float [] progress) throws DataLinkException
	{
		List<Integer> bookIds = link.getLink().getAllBookIds();
		int cnt = 0;
		for (int bookId : bookIds)
		{
			Book book = link.getBook(bookId);
			merge(book, from, to);
			
			for (int pageNum=1;pageNum<=book.getLastPageNumber();pageNum++)
			{
				Page page = book.getPage(pageNum);
				merge(page, from, to);
				
				for (Region region : page.getRegions())
				{
					merge(region, from, to);
					region.unloadMetaData();
				}
				page.unloadAll();
			}
			book.unloadMetaData();
			cnt++;
			progress[0] = cnt*1f/bookIds.size();
		}
	}
	private void merge(AnnotatedObject object, MetaData from, MetaData to) throws DataLinkException
	{
		List<MetaData> mds = object.getMetaDataListForKey(link.tagKey);
		boolean containsFrom = false, containsTo = false;
		for (MetaData md : mds)
		{
			containsFrom |= md.getId() == from.getId();
			containsTo |= md.getId() == to.getId();
		}
		if (containsFrom)
		{
			object.removeMetaData(from);
			if (!containsTo)
				object.addMetaData(to);
		}
	}
	
	private void delete(MetaData tag, float [] progress) throws DataLinkException
	{
		List<Integer> bookIds = link.getLink().getAllBookIds();
		int cnt = 0;
		for (int bookId : bookIds)
		{
			Book book = link.getBook(bookId);
			delete(book, tag);
			
			for (int pageNum=1;pageNum<=book.getLastPageNumber();pageNum++)
			{
				Page page = book.getPage(pageNum);
				delete(page, tag);
				
				for (Region region : page.getRegions())
				{
					delete(region, tag);
					region.unloadMetaData();
				}
				page.unloadAll();
			}
			book.unloadMetaData();
			cnt++;
			progress[0] = cnt*1f/bookIds.size();
		}
	}
	private void delete(AnnotatedObject object, MetaData tag) throws DataLinkException
	{
		List<MetaData> mds = object.getMetaDataListForKey(link.tagKey);
		boolean containsTag = false;
		for (MetaData md : mds)
			containsTag |= md.getId() == tag.getId();
		if (containsTag)
			object.removeMetaData(tag);
	}
	
	public static void show(final MainWindow win) throws DataLinkException
	{
		final JDialog dialog = new JDialog(win, XMLResourceBundle.getBundledString("tagManagerLabel"), true);
		
		TagManager manager = new TagManager(win);
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		closePanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCloseLabel")) 
			{public void actionPerformed(java.awt.event.ActionEvent arg0) {dialog.setVisible(false);}}));
		manager.add(closePanel);
		dialog.add(manager);
		
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnComponent(dialog, win);
		dialog.setVisible(true);
	}
}
