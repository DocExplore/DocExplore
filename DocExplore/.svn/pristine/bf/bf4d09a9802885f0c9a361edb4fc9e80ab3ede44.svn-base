package org.interreg.docexplore.reader.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class LocalConnection implements Connection
{
	PipedInputStream input;
	PipedOutputStream output;
	LocalConnection linked;
	
	private LocalConnection(LocalConnection linked) throws Exception
	{
		this.linked = linked;
		this.input = new PipedInputStream(linked.output);
		this.output = new PipedOutputStream(linked.input);
	}
	
	public LocalConnection(LocalServer server) throws Exception
	{
		this.input = new PipedInputStream();
		this.output = new PipedOutputStream();
		this.linked = new LocalConnection(this);
		server.incoming.offer(linked);
	}
	
	public InputStream getInputStream() throws Exception {return input;}
	public OutputStream getOutputStream() throws Exception {return output;}

	public void close() throws Exception
	{
		input.close();
		output.close();
	}

	public String getInetAddress() {return "local";}
}
