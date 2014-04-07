package org.interreg.docexplore.reader.sound;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.StreamedResource;

public class StreamedSound extends StreamedResource
{
	public List<short []> decoded;
	
	protected StreamedSound(ReaderClient client, String uri, File file)
	{
		super(client, uri, file);
		
		this.decoded = new LinkedList<short[]>();
	}
	
	public void handle(InputStream stream) throws Exception
	{
//		MP3Decoder decoder = new MP3Decoder(stream, 44100, false);
//		short [] transfer;
//		
//		while ((transfer = decoder.decode()) != null)
//			synchronized (decoded) {decoded.add(copy(transfer));}
	}
	
	static short [] copy(short [] buffer)
	{
		short [] res = new short [buffer.length];
		for (int i=0;i<buffer.length;i++)
			res[i] = buffer[i];
		return res;
	}
	
	public static Allocator<StreamedSound> allocator = new Allocator<StreamedSound>()
	{
		public StreamedSound cast(StreamedResource stream) {return (StreamedSound)stream;}
		public StreamedSound allocate(ReaderClient client, String uri, File file) {return new StreamedSound(client, uri, file);}
	};
}
