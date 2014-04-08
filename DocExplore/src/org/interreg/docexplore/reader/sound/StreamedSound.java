/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
