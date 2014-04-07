package org.interreg.docexplore.reader.net;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LocalServer implements ConnectionServer
{
	LinkedBlockingQueue<LocalConnection> incoming = new LinkedBlockingQueue<LocalConnection>();
	
	int timeout = 10000;
	public void setSoTimeout(int time) throws Exception {this.timeout = time;}

	public String getLocalSocketAddress() {return "local";}

	public Connection accept() throws Exception {return incoming.poll(timeout, TimeUnit.MILLISECONDS);}
}
