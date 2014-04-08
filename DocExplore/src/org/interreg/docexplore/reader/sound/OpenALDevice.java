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
