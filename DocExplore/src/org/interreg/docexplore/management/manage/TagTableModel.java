package org.interreg.docexplore.management.manage;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;

public class TagTableModel implements TableModel
{
	DocExploreDataLink link;
	
	List<String> langs = new Vector<String>();
	List<MetaData> tags = new Vector<MetaData>();
	
	public TagTableModel(DocExploreDataLink link) throws DataLinkException
	{
		this.link = link;
		
		langs.add("en");
		langs.add("fr");
		load();
	}
	
	void load() throws DataLinkException
	{
		tags.clear();
		
		tags.addAll(link.tagKey.getMetaData(MetaData.textType));
		
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this));
	}

	List<TableModelListener> listeners = new LinkedList<TableModelListener>();
	public void addTableModelListener(TableModelListener l) {listeners.add(l);}
	public void removeTableModelListener(TableModelListener l) {listeners.remove(l);}

	public Class<?> getColumnClass(int columnIndex) {return String.class;}

	public int getColumnCount() {return langs.size();}
	public String getColumnName(int columnIndex)
	{
		return langs.get(columnIndex);
	}
	public int getRowCount() {return tags.size();}
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		try {return DocExploreDataLink.getTagName(tags.get(rowIndex), langs.get(columnIndex));}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
		return "";
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {return true;}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		try
		{
			String name = aValue.toString().trim();
			if (name.trim().length() == 0)
			{
				JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("keyEmptyMessage"));
				return;
			}
			MetaData tag = tags.get(rowIndex);
			for (MetaData prev : tags)
				if (prev != tag && DocExploreDataLink.getBestTagName(prev).equals(name))
				{
					JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("keyUsedMessage"));
					return;
				}
			DocExploreDataLink.setTagName(tag, aValue.toString(), langs.get(columnIndex));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
	}
}
