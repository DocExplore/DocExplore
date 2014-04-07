package org.interreg.docexplore.reader.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketConnection implements Connection
{
	Socket socket;
	
	public SocketConnection(Socket socket)
	{
		this.socket = socket;
	}

	public InputStream getInputStream() throws Exception {return socket.getInputStream();}
	public OutputStream getOutputStream() throws Exception {return socket.getOutputStream();}
	public void close() throws Exception {socket.close();}
	public String getInetAddress() {return socket.getInetAddress().toString();}
}
