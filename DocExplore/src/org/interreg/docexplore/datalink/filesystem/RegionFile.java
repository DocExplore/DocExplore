package org.interreg.docexplore.datalink.filesystem;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Vector;

public class RegionFile extends ObjectFile
{
	private static final long serialVersionUID = -5400368443866131417L;
	
	int pageId;
	int [][] outline;
	
	public RegionFile()
	{
	}

	public RegionFile(int id, int pageId, int [][] outline)
	{
		super(id);
		this.pageId = pageId;
		this.outline = outline;
	}
	
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		this.pageId = ois.readInt();
		this.outline = (int [][])ois.readObject();
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(pageId);
		oos.writeObject(outline);
	}
	
	public List<Point> getOutline()
	{
		List<Point> res = new Vector<Point>(outline.length);
		for (int [] point : outline)
			res.add(new Point(point[0], point[1]));
		return res;
	}
}
