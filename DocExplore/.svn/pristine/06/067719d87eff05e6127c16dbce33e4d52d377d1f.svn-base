package org.interreg.docexplore.reader.net;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.interreg.docexplore.reader.net.ServerTask.ResponseSender;

public class ResourceRequest implements Request, ResponseSender
{
	private static final long serialVersionUID = -7956446480093821884L;
	
	public String uri;
	public int sent;
	public File resource;
	public boolean cancel;
	
	public ResourceRequest(String uri)
	{
		this.uri = uri;
		this.sent = 0;
		this.resource = null;
		this.cancel = false;
	}

	static int packetSize = 1024;
	public void run(ServerTask task) throws Exception
	{
		File file = new File(task.server.baseDir, uri);
		if (!file.exists())
		{
			task.submitResponse(new StreamErrorResponse(uri, "Resource not found"));
			return;
		}
		this.resource = file;
		
		try
		{
			FileInputStream input = new FileInputStream(file);
			boolean done = false;
			int cnt = 0;
			while (!done && !cancel)
			{
				byte [] buffer = new byte [packetSize];
				int read = input.read(buffer);
				done = read == -1;//< buffer.length;
				//System.out.println("read "+read);
				//try {Thread.sleep(10);} catch (Exception e) {}
				if (read > 0)
					task.submitResponse(this, new ResourcePacketResponse(uri, 
						read == buffer.length ? buffer : Arrays.copyOf(buffer, read), cnt++));
			}
			//try {Thread.sleep(40);} catch (Exception e) {}
			if (!cancel)
				task.submitResponse(new ResourceEOFResponse(uri, cnt-1));
			input.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void responseSent(ServerTask connection, Response response)
	{
		if (response instanceof ResourcePacketResponse)
			sent += ((ResourcePacketResponse)response).data.length;
	}
	
	public float progress()
	{
		if (resource != null)
			return sent*1.f/resource.length();
		else return 0;
	}
	
	public String toString() {return "   "+uri+"..."+(int)(100*progress())+"%";}
}
