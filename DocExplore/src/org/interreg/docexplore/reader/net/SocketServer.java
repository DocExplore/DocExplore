package org.interreg.docexplore.reader.net;

import java.net.ServerSocket;

public class SocketServer implements ConnectionServer
{
	ServerSocket socket;
	
	public SocketServer(ServerSocket socket)
	{
		this.socket = socket;
	}
	
	public void setSoTimeout(int time) throws Exception {socket.setSoTimeout(time);}
	public String getLocalSocketAddress() {return socket.getLocalSocketAddress().toString();}
	public Connection accept() throws Exception {return new SocketConnection(socket.accept());}
}
