package org.interreg.docexplore.datalink.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class MetaDataKeyFile implements Serializable
{
	private static final long serialVersionUID = -320963498063419770L;
	
	int id;
	Map<String, String> names;
	
	public MetaDataKeyFile()
	{
	}
	
	public MetaDataKeyFile(int id)
	{
		this.id = id;
		this.names = new TreeMap<String, String>();
	}
	
	@SuppressWarnings("unchecked")
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.id = ois.readInt();
		this.names = (Map<String, String>)ois.readObject();
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(id);
		oos.writeObject(names);
	}
}
