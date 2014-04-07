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
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
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
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel"))
			{public void actionPerformed(ActionEvent arg0) {res[0] = true; dialog.setVisible(false);}}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel"))
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
