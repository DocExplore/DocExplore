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
