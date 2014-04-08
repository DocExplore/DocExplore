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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaDataKey;

public class MetaDataKeyTableModel implements TableModel
{
	DocExploreDataLink link;
	
	List<String> langs = new Vector<String>();
	List<MetaDataKey> keys = new Vector<MetaDataKey>();
	List<Integer> counts = new Vector<Integer>();
	Set<MetaDataKey> reserved;
	
	public MetaDataKeyTableModel(DocExploreDataLink link, Set<MetaDataKey> reserved, float [] progress) throws DataLinkException
	{
		this.link = link;
		this.reserved = reserved;
		langs.add("en");
		langs.add("fr");
		load(progress);
	}
	
	void load(float [] progress) throws DataLinkException
	{
		keys.clear();
		counts.clear();
		
		keys.addAll(link.getAllKeys());
		keys.removeAll(reserved);
		progress[0] = 0;
		for (int i=0;i<keys.size();i++)
		{
			counts.add(keys.get(i).getMetaDataIds(null).size());
			progress[0] = (i+1)*1f/keys.size();
		}
		
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this));
	}

	List<TableModelListener> listeners = new LinkedList<TableModelListener>();
	public void addTableModelListener(TableModelListener l) {listeners.add(l);}
	public void removeTableModelListener(TableModelListener l) {listeners.remove(l);}

	public Class<?> getColumnClass(int columnIndex) {return String.class;}

	public int getColumnCount() {return langs.size()+1;}
	public String getColumnName(int columnIndex)
	{
		if (columnIndex < langs.size())
			return langs.get(columnIndex);
		else return XMLResourceBundle.getBundledString("keyCountLabel");
	}
	public int getRowCount() {return keys.size();}
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (columnIndex < langs.size())
		{
			try
			{
				return keys.get(rowIndex).getName(langs.get(columnIndex));
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true);}
			return "";
		}
		else return counts.get(rowIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {return columnIndex < langs.size() && !reserved.contains(keys.get(rowIndex));}

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
			MetaDataKey key = keys.get(rowIndex);
			MetaDataKey prev = link.getKey(name, langs.get(columnIndex));
			if (prev != null && prev != key && prev.getBestName().equals(name))
			{
				JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("keyUsedMessage"));
				return;
			}
			key.setName(name, langs.get(columnIndex));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
	}
}
