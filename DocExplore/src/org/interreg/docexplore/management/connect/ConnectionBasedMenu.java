package org.interreg.docexplore.management.connect;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.interreg.docexplore.datalink.DataLink.DataLinkSource;
import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.datalink.fs2.DataLinkFS2Source;
import org.interreg.docexplore.datalink.mysql.DataLinkMySQLSource;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.connect.ConnectionHandler.ConnectionHandlerListener;

@SuppressWarnings("serial")
public abstract class ConnectionBasedMenu extends JMenu implements ConnectionHandlerListener
{
	List<JMenuItem> recentConnections;
	
	public ConnectionBasedMenu(ConnectionHandler connectionHandler, String name)
	{
		super(name);
		
		this.recentConnections = new LinkedList<JMenuItem>();
		
		add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuFileConnectMySQL"))
		{
			public void actionPerformed(ActionEvent e)
			{
				DataLinkMySQLSource source = MySQLConnectionDialog.show();
				if (source != null) try {connectionSelected(source);}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
			}
		});
		add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuFileConnectFile"))
		{
			File current = null;
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = current != null ? new JFileChooser(current) : new JFileChooser();
				chooser.setAcceptAllFileFilterUsed(true);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				if (chooser.showOpenDialog(ConnectionBasedMenu.this.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					current = chooser.getCurrentDirectory();
					try {connectionSelected(new DataLinkFS2Source(file.getAbsolutePath()));}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				}
			}
		});
//		add(new AbstractAction(XMLResourceBundle.getBundledString("generalMenuFileConnectFile")+" (depr.)")
//		{
//			File current = null;
//			public void actionPerformed(ActionEvent e)
//			{
//				JFileChooser chooser = current != null ? new JFileChooser(current) : new JFileChooser();
//				chooser.setAcceptAllFileFilterUsed(true);
//				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//				
//				if (chooser.showOpenDialog(ConnectionBasedMenu.this.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION)
//				{
//					File file = chooser.getSelectedFile();
//					current = chooser.getCurrentDirectory();
//					try {connectionSelected(new DataLinkFileSystem.DataLinkFileSystemSource(file.getAbsolutePath()));}
//					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
//				}
//			}
//		});
		addSeparator();
		
		updateRecentConnections(connectionHandler);
		connectionHandler.addConnectionHandlerListener(this);
	}
	
	public void updateRecentConnections(ConnectionHandler handler)
	{
		for (JMenuItem item : recentConnections)
			remove(item);
		recentConnections = handler.buildItems(this);
		for (JMenuItem item : recentConnections)
			add(item);
	}
	
	public abstract void connectionSelected(DataLinkSource source) throws DataLinkException;
	
	public void connectionsChanged(ConnectionHandler handler) {updateRecentConnections(handler);}
}
