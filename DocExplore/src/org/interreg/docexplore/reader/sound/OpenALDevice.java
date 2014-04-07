package org.interreg.docexplore.reader.sound;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import org.lwjgl.openal.AL10;

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class OpenALDevice implements AudioDevice
{
	int sourceId;
	IntBuffer bufferIds;
	
	boolean isMono;
	int samplingRate;
	
	ShortBuffer transferBuffer;
	int currentBuffer, currentCursor;
	Vector<Integer> idleBuffers;
	
	int bufferSize = 1024;
	int nBuffers = 8;
	
	int errnum;
	
	public OpenALDevice(int samplingRate, boolean isMono)
	{
		this.sourceId = AL10.alGenSources();
		if ((errnum = AL10.alGetError()) != AL10.AL_NO_ERROR)
			throw new GdxRuntimeException("Couldn't create source - "+errnum);
		this.bufferIds = BufferUtils.newIntBuffer(nBuffers);
		AL10.alGenBuffers(bufferIds);
		if ((errnum = AL10.alGetError()) != AL10.AL_NO_ERROR)
			throw new GdxRuntimeException("Couldn't create buffers - "+errnum);
		
		this.isMono = isMono;
		this.samplingRate = samplingRate;
		
		this.transferBuffer = BufferUtils.newShortBuffer(bufferSize);
		this.currentBuffer = -1;
		this.currentCursor = 0;
		
		for (int i=0;i<transferBuffer.capacity();i++)
			transferBuffer.put(i, (short)0);
		
		this.idleBuffers = new Vector<Integer>(bufferIds.capacity());
		for (int i=0;i<bufferIds.capacity();i++)
		{
			AL10.alBufferData(bufferIds.get(i), isMono ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, 
				transferBuffer, samplingRate);
			idleBuffers.add(bufferIds.get(i));
		}
	}
	
	long timeout = 5000;
	int acquireBuffer()
	{
		if (!idleBuffers.isEmpty())
			return idleBuffers.remove(idleBuffers.size()-1);
		else if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
		{
			while (AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED) > 0)
				idleBuffers.add(AL10.alSourceUnqueueBuffers(sourceId));
			return idleBuffers.remove(idleBuffers.size()-1);
		}
		else
		{
			long start = System.currentTimeMillis();
			while (AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED) == 0)
			{
				if (System.currentTimeMillis()-start >= timeout)
					return -1;
				try {Thread.sleep(50);}
				catch (Exception e) {}
			}
			return AL10.alSourceUnqueueBuffers(sourceId);
		}
	}
	
	void submitBuffer(int bufferId)
	{
		AL10.alBufferData(bufferId, isMono ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, 
			transferBuffer, samplingRate);
		AL10.alSourceQueueBuffers(sourceId, bufferId);
		if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING)
			AL10.alSourcePlay(sourceId);
	}
	
	public void dispose()
	{
		AL10.alSourceStop(sourceId);
		AL10.alDeleteBuffers(bufferIds);
		AL10.alDeleteSources(sourceId);
	}

	public int getLatency() {return (int)(1000*bufferSize*nBuffers*(isMono ? 1. : .5)/samplingRate);}

	public boolean isMono() {return isMono;}

	public void writeSamples(short [] samples, int offset, int numSamples)
	{
		while (numSamples > 0)
		{
			if (currentBuffer == -1)
			{
				currentBuffer = acquireBuffer();
				if (currentBuffer == -1)
					//throw new GdxRuntimeException("OpenAL device timeout");
					return;
				currentCursor = 0;
			}
			
			int nWrite = Math.min(transferBuffer.capacity()-currentCursor, numSamples);
			transferBuffer.position(currentCursor);
			transferBuffer.put(samples, offset, nWrite);
			transferBuffer.position(0);
			currentCursor += nWrite;
			offset += nWrite;
			numSamples -= nWrite;
			
			if (currentCursor == transferBuffer.capacity())
			{
				submitBuffer(currentBuffer);
				currentBuffer = -1;
				currentCursor = 0;
			}
		}
	}

	public void writeSamples(float [] samples, int offset, int numSamples)
	{
	}

	public void setVolume(float arg0)
	{
	}
}
