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
