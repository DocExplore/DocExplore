/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.net;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.interreg.docexplore.reader.net.ServerTask.ResponseSender;

public class ResourceRequest implements Request, ResponseSender
{
	private static final long serialVersionUID = -7956446480093821884L;
	
	public String uri;
	public int sent;
	public File resource;
	public boolean cancel;
	
	public ResourceRequest(String uri)
	{
		this.uri = uri;
		this.sent = 0;
		this.resource = null;
		this.cancel = false;
	}

	static int packetSize = 1024;
	public void run(ServerTask task) throws Exception
	{
		File file = new File(task.server.baseDir, uri);
		if (!file.exists())
		{
			task.submitResponse(new StreamErrorResponse(uri, "Resource not found"));
			return;
		}
		this.resource = file;
		
		try
		{
			FileInputStream input = new FileInputStream(file);
			boolean done = false;
			int cnt = 0;
			while (!done && !cancel)
			{
				byte [] buffer = new byte [packetSize];
				int read = input.read(buffer);
				done = read == -1;//< buffer.length;
				//System.out.println("read "+read);
				//try {Thread.sleep(10);} catch (Exception e) {}
				if (read > 0)
					task.submitResponse(this, new ResourcePacketResponse(uri, 
						read == buffer.length ? buffer : Arrays.copyOf(buffer, read), cnt++));
			}
			//try {Thread.sleep(40);} catch (Exception e) {}
			if (!cancel)
				task.submitResponse(new ResourceEOFResponse(uri, cnt-1));
			input.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void responseSent(ServerTask connection, Response response)
	{
		if (response instanceof ResourcePacketResponse)
			sent += ((ResourcePacketResponse)response).data.length;
	}
	
	public float progress()
	{
		if (resource != null)
			return sent*1.f/resource.length();
		else return 0;
	}
	
	public String toString() {return "   "+uri+"..."+(int)(100*progress())+"%";}
}
