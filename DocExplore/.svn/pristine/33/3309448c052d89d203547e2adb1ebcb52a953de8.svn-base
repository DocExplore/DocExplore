package org.interreg.docexplore.reader.net;

public class ResourceCancelRequest implements Request
{
	private static final long serialVersionUID = 5846264353218769392L;
	
	public final String uri;
	
	public ResourceCancelRequest(String uri)
	{
		this.uri = uri;
	}

	public void run(ServerTask task) throws Exception
	{
		synchronized (task.requests)
		{
			for (Request request : task.requests)
				if (request instanceof ResourceRequest && ((ResourceRequest)request).uri.equals(uri))
					{((ResourceRequest)request).cancel = true; break;}
		}
	}
}
