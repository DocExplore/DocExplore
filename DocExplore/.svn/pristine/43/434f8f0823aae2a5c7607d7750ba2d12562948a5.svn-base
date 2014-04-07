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
