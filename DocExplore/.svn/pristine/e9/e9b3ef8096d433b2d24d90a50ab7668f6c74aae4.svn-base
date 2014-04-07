package org.interreg.docexplore.reader.net;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class StreamedXML extends StreamedResource
{
	public Document doc;
	
	protected StreamedXML(ReaderClient client, final String uri, File file)
	{
		super(client, uri, file);
		
		this.doc = null;
	}

	public void handle(InputStream stream) throws Exception
	{
		if (file == null)
			this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this);
		else this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
	}

	public static final Allocator<StreamedXML> allocator = new Allocator<StreamedXML>()
	{
		public StreamedXML allocate(ReaderClient client, String uri, File file) {return new StreamedXML(client, uri, file);}
		public StreamedXML cast(StreamedResource stream) {return (StreamedXML)stream;}
	};
}
