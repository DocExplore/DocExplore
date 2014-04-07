package org.interreg.docexplore.datalink.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BookFile extends ObjectFile
{
	private static final long serialVersionUID = -1077536567331699846L;
	
	String name;
	
	public BookFile()
	{
	}
	
	public BookFile(int id, String name)
	{
		super(id);
		this.name = name;
	}
	
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.name = ois.readUTF();
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeUTF(name);
	}
}
