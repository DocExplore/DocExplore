/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.connect;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.datalink.DataLink.DataLinkSource;
import org.interreg.docexplore.gui.ErrorHandler;

public class ConnectionHandler
{
	public static class PastConnection implements Serializable, Comparable<PastConnection>
	{
		private static final long serialVersionUID = -1553174984788668645L;
		
		public Calendar when;
		public DataLink.DataLinkSource source;
		
		public PastConnection(Calendar when, DataLinkSource source)
		{
			this.when = when;
			this.source = source;
		}
		
		@SuppressWarnings("serial")
		public JMenuItem buildItem(final ConnectionBasedMenu menu)
		{
			return new JMenuItem(new AbstractAction(toString())
			{
				public void actionPerformed(ActionEvent arg0)
				{
					try {menu.connectionSelected(source);}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				}
			});
		}

		public int compareTo(PastConnection pc) {return pc.when.compareTo(when);}
		
		public String toString() {return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(when.getTime())+" - "+source.getDescription();}
	}
	
	public static interface ConnectionHandlerListener
	{
		public void connectionsChanged(ConnectionHandler handler);
	}
	
	File connectionFile;
	public Set<PastConnection> connections;
	List<ConnectionHandlerListener> listeners;
	
	@SuppressWarnings("unchecked")
	public ConnectionHandler()
	{
		this.connectionFile = new File(DocExploreTool.getHomeDir(), "MMTConnections");
		this.connections = null;
		this.listeners = new LinkedList<ConnectionHandler.ConnectionHandlerListener>();
		
		if (connectionFile.exists()) try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(connectionFile));
			connections = (Set<ConnectionHandler.PastConnection>)ois.readObject();
			ois.close();
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		
		if (connections == null)
		{
			connections = new TreeSet<ConnectionHandler.PastConnection>();
		}
	}
	
	public void addConnectionHandlerListener(ConnectionHandlerListener listener) {listeners.add(listener);}
	public void removeConnectionHandlerListener(ConnectionHandlerListener listener) {listeners.remove(listener);}
	void notifyListeners()
	{
		for (ConnectionHandlerListener listener : listeners)
			listener.connectionsChanged(this);
	}
	
	public List<JMenuItem> buildItems(ConnectionBasedMenu menu)
	{
		List<JMenuItem> items = new LinkedList<JMenuItem>();
		for (PastConnection connection : connections)
			items.add(connection.buildItem(menu));
		return items;
	}
	
	public void addConnection(DataLinkSource source) throws FileNotFoundException, IOException
	{
		boolean duplicate = false;
		for (PastConnection connection : connections)
			if (source.equals(connection.source))
				{duplicate = true; connection.when = Calendar.getInstance(); break;}
		
		if (!duplicate)
			connections.add(new PastConnection(Calendar.getInstance(), source));
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(connectionFile, false));
		oos.writeObject(connections);
		oos.close();
		
		notifyListeners();
	}
}
