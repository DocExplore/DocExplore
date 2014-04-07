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
