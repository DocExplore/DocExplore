package org.interreg.docexplore.datalink.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PageFile extends ObjectFile
{
	private static final long serialVersionUID = 2948296703528151452L;
	
	int bookId;
	byte [] data;
	int pageNum;
	
	public PageFile()
	{
	}
	
	public PageFile(int id, int bookId, byte[] data, int pageNum)
	{
		super(id);
		this.bookId = bookId;
		this.data = data;
		this.pageNum = pageNum;
	}
	
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.bookId = ois.readInt();
		this.data = (byte [])ois.readObject();
		this.pageNum = ois.readInt();
	}

	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(bookId);
		oos.writeObject(data);
		oos.writeInt(pageNum);
	}
}
