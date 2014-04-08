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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.interreg.docexplore.util.Pair;

public class ServerTask
{
	public static interface ResponseSender
	{
		public void responseSent(ServerTask connection, Response response);
	}
	
	public final ReaderServer server;
	boolean disposed;
	boolean running;
	LinkedBlockingQueue<Pair<ResponseSender, Response>> outputQueue;
	Connection client;
	int in, out;
	public Set<Request> requests;
	
	public ServerTask(final ReaderServer server, final Connection client)
	{
		this.server = server;
		this.disposed = false;
		this.running = true;
		this.outputQueue = new LinkedBlockingQueue<Pair<ResponseSender, Response>>();
		this.client = client;
		this.in = this.out = 0;
		this.requests = new HashSet<Request>();
		
		//input thread
		new Thread()
		{
			public void run()
			{
				ObjectInputStream input = null;
				synchronized (client)
				{
					try
					{
						input = new ObjectInputStream(client.getInputStream())
						{
							protected Class<?> resolveClass(ObjectStreamClass o) throws IOException, ClassNotFoundException
							{
								try {return super.resolveClass(o);}
								catch (Exception e) {}
								return server.startup.pluginClassloader.loadClass(o.getName());
							}
						};
					}
					catch (Exception e) {e.printStackTrace(); running = false;}
				}
				
				while (running)
				{
					Request request = null;
					try {request = (Request)input.readObject();}
					catch (Exception e) {e.printStackTrace();}
					
					if (request != null)
					{
						in++;
						final Request _request = request;
						synchronized (requests) {requests.add(request);}
						final Future<Request> future = server.pool.submit(new Callable<Request>() {public Request call()
						{
							try {_request.run(ServerTask.this);}
							catch (Exception e)
							{
								e.printStackTrace();
								synchronized (requests) {requests.remove(_request);}
							}
							return _request;
						}});
						new Thread()
						{
							public void run()
							{
								Request res = null;
								try {res = future.get();}
								catch (Exception e) {e.printStackTrace();}
								if (res != null)
									synchronized (requests) {requests.remove(res);}
							}
						}.start();
					}
					
//					try {input.reset();}
//					catch (Exception e) {e.printStackTrace();}
				}
				
				if (input != null)
					try {input.close();}
					catch (Exception e) {e.printStackTrace();}
			}
		}.start();
		
		//output thread
		new Thread()
		{
			public void run()
			{
				ObjectOutputStream output = null;
				synchronized (client)
				{
					try {output = new ObjectOutputStream(client.getOutputStream());}
					catch (Exception e) {e.printStackTrace(); running = false;}
				}
				
				while (running)
				{
					Pair<ResponseSender, Response> response = null;
					try {response = outputQueue.poll(3, TimeUnit.SECONDS);}
					catch (Exception e) {}
					
					if (response != null) try
					{
						out++;
						output.writeObject(response.second);
						if (response.first != null)
							response.first.responseSent(ServerTask.this, response.second);
					}
					catch (Exception e) {e.printStackTrace();}
					
					try
					{
						output.flush();
						output.reset();
					}
					catch (Exception e) {e.printStackTrace();}
				}
				
				if (output != null)
					try {output.close();}
					catch (Exception e) {e.printStackTrace();}
			}
		}.start();
	}
	
	public void submitResponse(ResponseSender sender, Response response)
	{
		outputQueue.add(new Pair<ResponseSender, Response>(sender, response));
	}
	public void submitResponse(Response response)
	{
		outputQueue.add(new Pair<ResponseSender, Response>(null, response));
	}
	
	void dispose()
	{
		this.disposed = true;
	}
	
	public synchronized void abort()
	{
		running = false;
		if (client != null) synchronized (client)
		{
			try {client.close();}
			catch (Exception e) {e.printStackTrace();}
			client = null;
		}
		server.taskAborted(this);
	}
	
	public String toString() {return client.getInetAddress()+" ("+in+" - "+out+")";}
}
