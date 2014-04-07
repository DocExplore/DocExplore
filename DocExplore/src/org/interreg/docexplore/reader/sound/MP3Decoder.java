package org.interreg.docexplore.reader.sound;
import java.io.InputStream;

import repackaged.javazoom.jl.decoder.Bitstream;
import repackaged.javazoom.jl.decoder.Decoder;
import repackaged.javazoom.jl.decoder.Header;
import repackaged.javazoom.jl.decoder.SampleBuffer;

public class MP3Decoder
{
	Decoder decoder;
	Bitstream stream;
	//SampleBuffer buffer;
	
	public MP3Decoder(InputStream stream, int rate, boolean isMono)
	{
		this.stream = new Bitstream(stream);
		this.decoder = new Decoder();
		//this.buffer = new SampleBuffer(rate, isMono ? 1 : 2);
		//decoder.setOutputBuffer(buffer);
	}
	
	public short [] decode() throws Exception
	{
		Header header = stream.readFrame();
		if (header == null)
			return null;
		SampleBuffer buffer = (SampleBuffer)decoder.decodeFrame(header, stream);
		short [] res = buffer.getBuffer();
		stream.closeFrame();
		return res;
	}
}
