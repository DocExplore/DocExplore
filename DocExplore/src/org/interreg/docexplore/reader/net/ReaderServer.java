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
