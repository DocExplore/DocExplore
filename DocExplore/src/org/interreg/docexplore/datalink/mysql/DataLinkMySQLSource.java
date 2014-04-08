/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.gui.ErrorHandler;

public class DataLinkMySQLSource implements DataLink.DataLinkSource
{
	private static final long serialVersionUID = 5759481061845198322L;

	public String url, database, user, password;
	
	public DataLinkMySQLSource(String url, String database, String user, String password)
	{
		this.url = url;
		this.database = database;
		this.user = user;
		this.password = password;
	}
	
	DataLinkMySQL link = null;
	public DataLink getDataLink() {return getDataLink(true);}
	public DataLink getDataLink(boolean report)
	{
		if (link == null) try {link = new DataLinkMySQL(this);}
		catch (Exception e) {if (report) ErrorHandler.defaultHandler.submit(e);}
		return link;
	}
	
	Connection createConnection() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.gjt.mm.mysql.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://"+url+"/"+database, user, password);
		return connection;
	}
	
	public String getDescription() {return "MySQL database '"+database+"' ("+user+"@"+url+")";}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeUTF(url);
		out.writeUTF(database);
		out.writeUTF(user);
		out.writeUTF(password);
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.url = in.readUTF();
		this.database = in.readUTF();
		this.user = in.readUTF();
		this.password = in.readUTF();
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof DataLinkMySQLSource)
		{
			DataLinkMySQLSource source = (DataLinkMySQLSource)o;
			return url.equals(source.url) && database.equals(source.database) && user.equals(source.user) && password.equals(source.password);
		}
		return false;
	}
}
