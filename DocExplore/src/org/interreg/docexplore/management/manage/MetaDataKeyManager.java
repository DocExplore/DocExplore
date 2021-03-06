/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.manage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.gui.MMTApp;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.GuiUtils.ProgressRunnable;

@SuppressWarnings("serial")
public class MetaDataKeyManager extends JPanel
{
	DocExploreDataLink link;
	MetaDataKeyTableModel model;
	JButton deleteButton, mergeButton;
	JLabel mergeText;
	MetaDataKey merging = null;
	
	public MetaDataKeyManager(final MMTApp win, float [] progress) throws DataLinkException
	{
		super(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.CENTER, SwingConstants.TOP, true, false));
		
		link = win.host.getLink();
		final Set<MetaDataKey> reserved = new HashSet<MetaDataKey>();
		reserved.add(link.dimKey);
		reserved.add(link.linkKey);
		reserved.add(link.miniKey);
		reserved.add(link.sourceKey);
		reserved.add(link.tagKey);
		reserved.add(link.tagsKey);
		reserved.add(link.transcriptionKey);
		model = new MetaDataKeyTableModel(link, reserved, progress);
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
					final MetaDataKey mergeTo = model.keys.get(table.getSelectedRow());
					if (JOptionPane.showConfirmDialog(MetaDataKeyManager.this, 
						"<html>"+Lang.s("keyMergeMessage").replace("%from", merging.getBestName()).replace("%to", mergeTo.getBestName())+"</html>", 
						Lang.s("keyMergeLabel"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					{
						deleteButton.setEnabled(table.getSelectedRow() >= 0);
						mergeButton.setEnabled(table.getSelectedRow() >= 0);
						mergeText.setText("");
						merging = null;
						return;
					}
					
					GuiUtils.blockUntilComplete(new ProgressRunnable()
					{
						float mprogress = 0;
						float [] lprogress = {0};
						public void run()
						{
							try
							{
								List<Integer> mdIds = merging.getMetaDataIds(null);
								int cnt = 0;
								for (int mdId : mdIds)
								{
									link.getLink().setMetaDataKey(mdId, mergeTo.getId());
									cnt++;
									mprogress = cnt*1f/mdIds.size();
								}
								mprogress = 1;
								link.deleteKey(merging);
							}
							catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
							
							try {model.load(lprogress);}
							catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
						}
						public float getProgress() {return .5f*mprogress+.5f*lprogress[0];}
					}, MetaDataKeyManager.this);
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
		mergePanel.add(deleteButton = new JButton(new AbstractAction(Lang.s("keyDeleteLabel")) {public void actionPerformed(ActionEvent arg0)
		{
			if (model.counts.get(table.getSelectedRow()) > 0)
				JOptionPane.showMessageDialog(MetaDataKeyManager.this, Lang.s("keyUsedMessage"), "", JOptionPane.ERROR_MESSAGE);
			else
			{
				final MetaDataKey key = model.keys.get(table.getSelectedRow());
				GuiUtils.blockUntilComplete(new ProgressRunnable()
				{
					float [] lprogress = {0};
					public void run()
					{
						try {link.deleteKey(key);}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
						try {model.load(lprogress);}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
					}
					public float getProgress() {return lprogress[0];}
				}, MetaDataKeyManager.this);
				deleteButton.setEnabled(table.getSelectedRow() >= 0);
				mergeButton.setEnabled(table.getSelectedRow() >= 0);
			}
		}}));
		mergePanel.add(mergeButton = new JButton(new AbstractAction(Lang.s("keyMergeLabel")) {public void actionPerformed(ActionEvent arg0)
		{
			if (merging == null)
			{
				merging = model.keys.get(table.getSelectedRow());
				try {mergeText.setText("<html>"+Lang.s("keySelectMessage").replace("%from", merging.getBestName())+"</html>");}
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
	
	public static void show(final MMTApp win, float [] progress) throws DataLinkException
	{
		final JDialog dialog = new JDialog(win, Lang.s("keyManagerLabel"), true);
		
		MetaDataKeyManager manager = new MetaDataKeyManager(win, progress);
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		closePanel.add(new JButton(new AbstractAction(Lang.s("generalCloseLabel")) 
			{public void actionPerformed(java.awt.event.ActionEvent arg0) {dialog.setVisible(false);}}));
		manager.add(closePanel);
		dialog.add(manager);
		
		dialog.pack();
		dialog.setResizable(false);
		GuiUtils.centerOnComponent(dialog, win);
		dialog.setVisible(true);
	}
}
