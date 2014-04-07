package org.interreg.docexplore.management.search;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

class SearchCriteria extends JPanel
{
	private static final long serialVersionUID = 7832898122585807869L;
	
	List<Object> fields;
	JTextField searchValue;
	JComboBox searchKey;
	
	public SearchCriteria(List<Object> keys)
	{
		super(new FlowLayout(FlowLayout.LEFT, 2, 2));
		
		this.fields = keys;
		this.searchValue = new JTextField(20);
		this.searchKey = new JComboBox(keys.toArray());
		
		add(searchValue);
		add(searchKey);
	}
	
	public void setKeys(List<Object> keys)
	{
		Object prevKey = searchKey.getSelectedItem();
		searchKey.removeAllItems();
		
		for (Object key : keys)
			searchKey.addItem(key);
		
		searchKey.setSelectedItem(prevKey);
	}
}
