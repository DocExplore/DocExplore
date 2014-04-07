package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.io.IOException;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.gui.ErrorHandler;

public class DataLinkFS2Source implements DataLink.DataLinkSource
{
	private static final long serialVersionUID = 7381964118246575300L;
	public String file;
	
	@SuppressWarnings("unused")
	private DataLinkFS2Source() {this.file = null;}
	public DataLinkFS2Source(String file)
	{
		this.file = file;
	}
	
	DataLinkFS2 link = null;
	public DataLink getDataLink()
	{
		if (link == null) try {link = new DataLinkFS2(this);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return link;
	}
	
	File getFile()
	{
		File file = new File(this.file);
		if (!file.isAbsolute())
			file = new File(DocExploreTool.getHomeDir(), file.getPath());
		return file;
	}

	public String getDescription() {return "File system '"+file+"'";}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {out.writeUTF(file);}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {this.file = in.readUTF();}
	
	public boolean equals(Object o)
	{
		if (o instanceof DataLinkFS2Source)
			return file.equals(((DataLinkFS2Source)o).file);
		return false;
	}
}