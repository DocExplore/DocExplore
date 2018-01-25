/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.process;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.util.GuiUtils;

@SuppressWarnings("serial")
public class MetaDataKeyBox extends JPanel
{
	public static class KeyHolder
	{
		MetaDataKey key;
		String name;
		
		public KeyHolder(MetaDataKey key, String name)
		{
			this.key = key;
			this.name = name;
		}
		
		public String toString() {return name;}
	}
	
	DocExploreDataLink link;
	JComboBox fields;
	
	public MetaDataKeyBox(DocExploreDataLink link, boolean editable) throws DataLinkException
	{
		super(new BorderLayout());
		
		this.link = link;
		Vector<KeyHolder> keys = new Vector<KeyHolder>();
		
		if (link != null && link.getLink() != null)
		{
			Collection<MetaDataKey> allKeys = link.getAllKeys();
			for (MetaDataKey key : allKeys)
				if (key != link.tagKey && key != link.transcriptionKey && key != link.linkKey && key.getName() != null)
					keys.add(new KeyHolder(key, key.getName()));
		}
		
		this.fields = new JComboBox(keys);
		fields.setEditable(editable);
		add(fields);
	}
	
	public MetaDataKey getSelectedKey() throws DataLinkException
	{
		Object item = fields.getSelectedItem();
		if (item instanceof KeyHolder)
			return ((KeyHolder)item).key;
		if (item instanceof String && link != null)
			return link.getOrCreateKey((String)item);
		return null;
	}
	
	public static MetaDataKey showDialog(DocExploreDataLink link, String title, String message, boolean editable) throws DataLinkException
	{
		final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), title, true);
		dialog.setLayout(new BorderLayout());
		
		JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final MetaDataKeyBox box = new MetaDataKeyBox(link, editable);
		boxPanel.add(new JLabel(message));
		boxPanel.add(box);
		
		final boolean [] res = {false};
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("generalOkLabel"))
			{public void actionPerformed(ActionEvent arg0) {res[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(new JButton(new AbstractAction(Lang.s("generalCancelLabel"))
			{public void actionPerformed(ActionEvent arg0) {res[0] = false; dialog.setVisible(false);}}));
		
		dialog.add(boxPanel, BorderLayout.NORTH);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		GuiUtils.centerOnScreen(dialog);
		dialog.setVisible(true);
		
		if (res[0])
			return box.getSelectedKey();
		return null;
	}
}
