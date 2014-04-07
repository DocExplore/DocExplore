package org.interreg.docexplore.reader.net;

public class ResourcePacketResponse implements Response
{
	private static final long serialVersionUID = -7199762616807066561L;

	public final String uri;
	public final byte [] data;
	public final long index;
	
	public ResourcePacketResponse(String uri, byte [] data, long index)
	{
		this.uri = uri;
		this.data = data;
		this.index = index;
	}
	
	public void run(ReaderClient client) throws Exception
	{
		StreamedResource resource = client.getResource(uri);
		if (resource != null)
			resource.addBuffer(data, index);
	}
}
