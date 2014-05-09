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
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.interreg.docexplore.Startup;
import org.interreg.docexplore.Startup.PluginConfig;
import org.interreg.docexplore.reader.ReaderApp;
import org.interreg.docexplore.reader.plugin.ServerPlugin;

/**
 * The "server" side reader component that handles requests for resources
 * @author Alexander Burnett
 *
 */
public class ReaderServer
{
	Startup startup;
	public ConnectionServer socket;
	boolean running;
	public LinkedBlockingQueue<ServerTask> tasks;
	ExecutorService pool;
	public final File baseDir;
	List<ServerPlugin> plugins;
	
	public ReaderServer(int port, File baseDir, Startup startup) throws Exception
	{
		this.startup = startup;
		//this.socket = ServerSocketFactory.getDefault().createServerSocket(port);
		this.socket = ReaderApp.createConnectionServer(port);
		socket.setSoTimeout(3000);
		this.running = false;
		this.tasks = new LinkedBlockingQueue<ServerTask>();
		this.pool = Executors.newCachedThreadPool();
		this.baseDir = baseDir;
		
		this.plugins = new LinkedList<ServerPlugin>();
		for (PluginConfig config : startup.filterPlugins(ServerPlugin.class))
		{
			ServerPlugin plugin = (ServerPlugin)config.clazz.newInstance();
			plugins.add(plugin);
			plugin.setHost(this, config.jarFile, config.dependencies);
			
			System.out.println("Loaded server plugin "+config.clazz.getName());
		}
	}
	
	public synchronized void open()
	{
		this.running = true;
		
		new Thread()
		{
			public void run()
			{
				try
				{
					//continually waits for new connections and start a ServerTask when a connection is made
					while (running)
					{
						Connection client = null;
						try {client = socket.accept();}
						catch (SocketTimeoutException e) {}
						catch (Exception e) {e.printStackTrace();}
						
						if (client != null)
							synchronized (ReaderServer.this)
							{
								if (running)
									tasks.add(new ServerTask(ReaderServer.this, client));
							}
					}
				}
				catch (Throwable t)
				{
					t.printStackTrace();
					System.exit(0);
				}
			}
		}.start();
	}
	
	long timeout = 5000;
	public synchronized void close()
	{
		this.running = false;
		for (ServerTask task : tasks)
			task.abort();
		tasks.clear();
		pool.shutdown();
	}
	
	synchronized void taskAborted(ServerTask task)
	{
		tasks.remove(task);
	}
	
	public String toString() {return socket.getLocalSocketAddress().toString();}
}
