package org.interreg.docexplore.datalink.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public class ObjectFile implements Serializable
{
	private static final long serialVersionUID = 2687939775430611151L;
	
	int id;
	Set<Integer> metaData;
	
	public ObjectFile()
	{
	}
	
	public ObjectFile(int id)
	{
		this.id = id;
		this.metaData = new TreeSet<Integer>();
	}
	
	@SuppressWarnings("unchecked")
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.id = ois.readInt();
		this.metaData = (Set<Integer>)ois.readObject();
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(id);
		oos.writeObject(metaData);
	}
}
