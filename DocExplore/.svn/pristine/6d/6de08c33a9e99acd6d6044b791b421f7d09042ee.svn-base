package org.interreg.docexplore.reader.net;

public class ResourceEOFResponse implements Response
{
	private static final long serialVersionUID = -4948507619156874704L;
	
	String uri;
	long lastIndex;
	
	public ResourceEOFResponse(String uri, long lastIndex)
	{
		this.uri = uri;
		this.lastIndex = lastIndex;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource resource = client.getResource(uri);
		if (resource != null)
			resource.setLastIndex(lastIndex);
	}
}
