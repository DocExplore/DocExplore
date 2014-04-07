package org.interreg.docexplore.management.annotate;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;

public class ListFilter extends JPanel
{
	private static final long serialVersionUID = 759016953691550306L;
	
	public static interface Listener
	{
		public void filterChanged(ListFilter filter);
	}

	boolean useFilter;
	int valueType;
	String keyType;
	String contents;
	
	JComboBox keys;
	
	public ListFilter(DocExploreDataLink link)
	{
		super(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createTitledBorder(XMLResourceBundle.getBundledString("annotateFilterLabel")));
		
		this.useFilter = false;
		this.valueType = 0;
		this.keyType = "";
		this.contents = "";
		
		JPanel mainPanel = new JPanel(new LooseGridLayout(0, 2, 5, 5, SwingConstants.LEFT, SwingConstants.CENTER));
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("annotateUseFilterLabel")));
		((JCheckBox)mainPanel.add(new JCheckBox())).addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			useFilter = ((JCheckBox)e.getSource()).isSelected();
			notifyFilterChanged();
		}});
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("annotateValueTypeLabel")));
		((JComboBox)mainPanel.add(new JComboBox(new Object [] {
			XMLResourceBundle.getBundledString("annotateAnyValueLabel"), 
			XMLResourceBundle.getBundledString("annotateTypeText"), 
			XMLResourceBundle.getBundledString("annotateTypeImage")}))).addItemListener(
				new ItemListener() {public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() != ItemEvent.SELECTED)
				return;
			valueType = ((JComboBox)e.getSource()).getSelectedIndex();
			notifyFilterChanged();
		}});
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("annotateKeyTypeLabel")));
		this.keys = (JComboBox)mainPanel.add(new JComboBox(new DefaultComboBoxModel()));
		refreshKeys(link);
		keys.addItemListener(
		new ItemListener() {public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() != ItemEvent.SELECTED)
				return;
			keyType = e.getItem().toString();
			notifyFilterChanged();
		}});
		
		mainPanel.add(new JLabel(XMLResourceBundle.getBundledString("annotateContentLabel")));
		final JTextField contentsField = new JTextField(40);
		contentsField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent arg0) {prepareRefresh();}
			public void insertUpdate(DocumentEvent arg0) {prepareRefresh();}
			public void changedUpdate(DocumentEvent arg0) {prepareRefresh();}
			
			Thread refreshTask = null;
			long schedule = -1;
			Object monitor = new Object();
			void prepareRefresh()
			{
				contents = contentsField.getText();
				synchronized (monitor)
				{
					schedule = System.currentTimeMillis()+1000;
					if (refreshTask == null)
					{
						refreshTask = new Thread() {public void run()
						{
							while (true)
							{
								synchronized (monitor)
								{
									if (System.currentTimeMillis() > schedule)
									{
										refreshTask = null;
										break;
									}
								}
								try {Thread.sleep(300);}
								catch (Exception e) {}
							}
							notifyFilterChanged();
						}};
						refreshTask.start();
					}
				}
			}
		});
		mainPanel.add(contentsField);
		
		add(mainPanel);
	}
	
	List<Listener> listeners = new LinkedList<Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	
	public void notifyFilterChanged()
	{
		for (Listener listener : listeners)
			listener.filterChanged(this);
	}
	
	public void refreshKeys(DocExploreDataLink link)
	{
		try
		{
			keys.removeAllItems();
			((DefaultComboBoxModel)keys.getModel()).addElement(XMLResourceBundle.getBundledString("annotateAnyKeyLabel"));
			Set<String> keyNames = new TreeSet<String>(new Comparator<String>()
				{Collator collator = Collator.getInstance(Locale.getDefault());
				public int compare(String o1, String o2) {return collator.compare(o1, o2);}});
			for (MetaDataKey key : link.getAllKeys())
			{
				String name = key.getName();
				if (name == null)
					name = key.getName("");
				if (name == null)
					continue;
				keyNames.add(name);
			}
			for (String name : keyNames)
				((DefaultComboBoxModel)keys.getModel()).addElement(name);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public boolean matches(MetaData annotation) throws DataLinkException
	{
		if (!useFilter)
			return true;
		
		if (valueType != 0 && 
			(valueType==1 && annotation.getType().equals(MetaData.textType) || 
			valueType==2 && annotation.getType().equals(MetaData.imageType)))
				return false;
		
		if (keys.getSelectedIndex() > 0)
		{
			String name = annotation.getKey().getName();
			if (name == null)
				name = annotation.getKey().getName("");
			if (name == null || !keyType.equals(name))
				return false;
		}
		
		if (contents.length() > 0)
		{
			if (annotation.getType().equals(MetaData.textType))
				return false;
			if (!annotation.getString().contains(contents))
				return false;
		}
		
		return true;
	}
}
