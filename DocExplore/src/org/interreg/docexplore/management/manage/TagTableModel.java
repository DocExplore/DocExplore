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
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
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
				JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), Lang.s("keyEmptyMessage"));
				return;
			}
			MetaData tag = tags.get(rowIndex);
			for (MetaData prev : tags)
				if (prev != tag && DocExploreDataLink.getBestTagName(prev).equals(name))
				{
					JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), Lang.s("keyUsedMessage"));
					return;
				}
			DocExploreDataLink.setTagName(tag, aValue.toString(), langs.get(columnIndex));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
	}
}
