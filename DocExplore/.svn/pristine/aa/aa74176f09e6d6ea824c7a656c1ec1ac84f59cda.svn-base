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
