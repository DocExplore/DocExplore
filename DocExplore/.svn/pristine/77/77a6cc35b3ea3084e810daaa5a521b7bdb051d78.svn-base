package org.interreg.docexplore.datalink;

public class DataLinkException extends Exception
{
	private static final long serialVersionUID = 1502967786133735161L;
	
	DataLink link;
	
	public DataLinkException(DataLink link, String msg)
	{
		super(msg);
		this.link = link;
	}
	
	public DataLinkException(DataLink link, String msg, Exception cause)
	{
		super(msg, cause);
		this.link = link;
	}
	
	public DataLinkException(DataLink link, Exception cause)
	{
		super(cause);
		this.link = link;
	}
}
