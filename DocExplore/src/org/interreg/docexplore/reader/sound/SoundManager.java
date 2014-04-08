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

import java.util.Vector;


import com.badlogic.gdx.audio.AudioDevice;

public class SoundManager
{
	public static class Playback
	{
		StreamedSound stream;
		int bufferIndex, sampleIndex;
		boolean paused;
		float volume;
		
		Playback(StreamedSound stream)
		{
			this.stream = stream;
			this.bufferIndex = 0;
			this.sampleIndex = 0;
			this.paused = false;
			this.volume = 1;
		}
		
		synchronized boolean readInto(short [] buffer)
		{
			if (stream != null && !paused) synchronized (stream.decoded)
			{
				if (bufferIndex >= stream.decoded.size())
					return stream.isComplete();
				short [] curBuffer = stream.decoded.get(bufferIndex);
				
				for (int i=0;i<buffer.length;i++)
				{
					buffer[i] += (short)(volume*curBuffer[sampleIndex++]);
					if (sampleIndex >= curBuffer.length)
					{
						sampleIndex = 0;
						bufferIndex++;
						if (bufferIndex >= stream.decoded.size())
							return stream.isComplete();
						curBuffer = stream.decoded.get(bufferIndex);
					}
				}
			}
			return stream == null;
		}
		
		public void pause() {this.paused = true;}
		public void resume() {this.paused = false;}
		public synchronized void stop()
		{
			new Thread()
			{
				public void run()
				{
					long start = System.currentTimeMillis();
					long delay = 3000;
					while (System.currentTimeMillis()-start < delay)
					{
						volume = 1-(System.currentTimeMillis()-start)*1f/delay;
						try {Thread.sleep(100);}
						catch (InterruptedException e) {}
					}
					
					//TODO: GC!!!
					//stream.dispose();
					stream = null;
				}
			}.start();
		}
	}
	
	Vector<Playback> playbacks;
	AudioDevice device;
	
	public SoundManager()
	{
		this.playbacks = new Vector<SoundManager.Playback>();
		this.device = new OpenALDevice(44100, false);//Gdx.audio.newAudioDevice(44100, true);
		
		new Thread()
		{
			public void run()
			{
				short [] buffer = new short [1024];
				while (true)
				{
					for (int i=0;i<buffer.length;i++)
						buffer[i] = 0;
					
					synchronized (playbacks)
					{
						for (int i=0;i<playbacks.size();i++)
							if (playbacks.get(i).readInto(buffer))
								{playbacks.remove(i); i--;}
					}
					
					device.writeSamples(buffer, 0, buffer.length);
				}
			}
		}.start();
	}
	
	public Playback play(StreamedSound stream)
	{
		Playback playback = new Playback(stream);
		synchronized (playbacks) {playbacks.add(playback);}
		return playback;
	}
}
