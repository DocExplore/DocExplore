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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.interreg.docexplore.Startup;
import org.interreg.docexplore.Startup.PluginConfig;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.plugin.ClientPlugin;

/**
 * Client side of the reader app.
 * @author Alexander Burnett
 *
 */
public class ReaderClient
{
	Startup startup;
	public final ReaderApp app;
	boolean running;
	LinkedBlockingQueue<Request> outputQueue;
	public String serverAddress;
	public Connection socket;
	Map<String, StreamedResource> resources;
	Map<Class<?>, StreamedResource.Allocator<?>> streamTypes;
	ExecutorService pool;
	public List<ClientPlugin> plugins;
	
	public ReaderClient(ReaderApp app, Startup startup) throws Exception
	{
		this.startup = startup;
		this.app = app;
		this.socket = null;
		this.running = false;
		this.outputQueue = new LinkedBlockingQueue<Request>();
		this.resources = new TreeMap<String, StreamedResource>();
		this.pool = Executors.newFixedThreadPool(8);
		this.streamTypes = new HashMap<Class<?>, StreamedResource.Allocator<?>>();
		
		this.plugins = new LinkedList<ClientPlugin>();
		for (PluginConfig config : startup.filterPlugins(ClientPlugin.class))
		{
			ClientPlugin plugin = (ClientPlugin)config.clazz.newInstance();
			plugins.add(plugin);
			plugin.setHost(this, config.jarFile, config.dependencies);
			
			System.out.println("Loaded client plugin "+config.clazz.getName());
		}
	}
	
	/**
	 * Makes an association between a StreamedResource implementation and an allocator.
	 * @param clazz
	 * @param allocator
	 */
	public <T extends StreamedResource> void registerStreamType(Class<T> clazz, StreamedResource.Allocator<T> allocator)
	{
		streamTypes.put(clazz, allocator);
	}
	
	/**
	 * Initiates a request for a resource to the server. An allocator for the StreamedResource implementation must be registered prior to calls to this method.
	 * @param clazz class of the {@link StreamedResource} implementation
	 * @param uri
	 * @return Returns a {@link StreamedResource} that will have begun streaming or null if no allocator was registered.
	 */
	@SuppressWarnings("unchecked")
	public <T extends StreamedResource> T getResource(Class<T> clazz, String uri)
	{
		StreamedResource.Allocator<?> allocator = streamTypes.get(clazz);
		if (allocator == null)
			return null;
		return getResource((StreamedResource.Allocator<T>)allocator, uri);
	}
	
	/**
	 * Initiates a request for a resource to the server by allocating the resource and sending a resource request.
	 * @param allocator
	 * @param uri
	 * @return
	 */
	private <T extends StreamedResource> T getResource(StreamedResource.Allocator<T> allocator, String uri)
	{
		try
		{
			synchronized (resources)
			{
				StreamedResource registered = resources.get(uri);
				if (registered == null)
				{
					T stream = allocator.allocate(this, uri, app.server == null ? null : new File(app.server.baseDir, uri));
					resources.put(uri, stream);
					if (app.server == null)
						outputQueue.add(stream.request());
					return stream;
				}
				else return allocator.cast(registered);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error on resource '"+uri+"'", e);
		}
	}
	
	/**
	 * Cancels an ongoing resource stream by sending a {@link ResourceCancelRequest} to the server.
	 * @param uri
	 */
	void cancelResource(String uri)
	{
		//new Exception().printStackTrace();
		StreamedResource resource = null;
		synchronized (resources) {resource = resources.remove(uri);}
		if (resource != null)
		{
			resource.canceled = true;
			outputQueue.add(new ResourceCancelRequest(uri));
		}
	}
	
	/*void registerResource(StreamedResource resource)
	{
		synchronized (resources)
		{
			StreamedResource registered = resources.get(resource.uri);
			if (registered == null)
			{
				resource.buffer = new ResourceBuffer();
				resources.put(resource.uri, resource);
				outputQueue.add(new ResourceRequest(resource.uri));
			}
			else resource.buffer = registered.buffer;
		}
	}*/
	
	public StreamedResource getResource(String uri)
	{
		synchronized (resources) {return resources.get(uri);}
	}
	
	public void submitRequest(Request request)
	{
		synchronized (resources) {outputQueue.add(request);}
	}
	
	public synchronized void stop()
	{
		this.running = false;
		try {socket.close();}
		catch (Exception e) {e.printStackTrace();}
		pool.shutdown();
	}
	
	public synchronized void start(String host, int port) throws Exception
	{
		serverAddress = host;
		//socket = SocketFactory.getDefault().createSocket(host, port);
		socket = ReaderApp.createClientConnection(host, port);
		
		this.running = true;
		
		//input thread
		new Thread()
		{
			public void run()
			{
				ObjectInputStream input = null;
				try
				{
					input = new ObjectInputStream(socket.getInputStream())
					{
						protected Class<?> resolveClass(ObjectStreamClass o) throws IOException, ClassNotFoundException
						{
							try {return super.resolveClass(o);}
							catch (Exception e) {}
							return startup.pluginClassloader.loadClass(o.getName());
						}
					};
				}
				catch (Exception e) {e.printStackTrace(); running = false;}
				
				while (running)
				{
					Response response = null;
					try {response = (Response)input.readObject();}
					catch (Exception e) {e.printStackTrace(); System.exit(0);}
					
					if (response != null)
					{
						final Response _response = response;
						pool.execute(new Runnable() {public void run()
							{try {_response.run(ReaderClient.this);} catch (Exception e) {e.printStackTrace();}}});
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
				try {output = new ObjectOutputStream(socket.getOutputStream());}
				catch (Exception e) {e.printStackTrace(); running = false;}
				
				while (running)
				{
					Request request = null;
					try {request = outputQueue.poll(3, TimeUnit.SECONDS);}
					catch (Exception e) {}
					
					if (request != null) try
					{
						output.writeObject(request);
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
}
