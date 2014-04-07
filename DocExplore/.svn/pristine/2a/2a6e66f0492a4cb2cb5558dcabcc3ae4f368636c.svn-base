package org.interreg.docexplore.reader.net;

public class StreamErrorResponse implements Response
{
	private static final long serialVersionUID = 7518910151956084881L;
	
	String uri;
	String message;
	
	public StreamErrorResponse(String uri, String message)
	{
		this.uri = uri;
		this.message = message;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource res = client.getResource(uri);
		if (res == null)
			return;
		else res.error = new Exception("Error message from server (stream '"+uri+"') : "+message);
		System.out.println("Error message from server (stream '"+uri+"') : "+message);
	}
}
