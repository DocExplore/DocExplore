package org.interreg.docexplore.datalink.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.TreeSet;

public class MetaDataFile extends ObjectFile
{
	private static final long serialVersionUID = 2049405248663941136L;
	
	int keyId;
	String type;
	byte [] value;
	Set<Integer> objects;
	
	public MetaDataFile()
	{
	}

	public MetaDataFile(int id, int keyId, String type, byte [] value)
	{
		super(id);
		this.keyId = keyId;
		this.type = type;
		this.value = value;
		this.objects = new TreeSet<Integer>();
	}
	
	@SuppressWarnings("unchecked")
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.keyId = ois.readInt();
		this.type = ois.readUTF();
		this.value = (byte [])ois.readObject();
		this.objects = (Set<Integer>)ois.readObject();
	}

	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(keyId);
		oos.writeUTF(type);
		oos.writeObject(value);
		oos.writeObject(objects);
	}
}
