package org.interreg.docexplore.reader.net;

public interface ConnectionServer
{
	public void setSoTimeout(int time) throws Exception;
	public String getLocalSocketAddress();
	public Connection accept() throws Exception;
}
