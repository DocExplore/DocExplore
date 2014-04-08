/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.Startup;
import org.interreg.docexplore.Startup.PluginConfig;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.reader.book.BookEngine;
import org.interreg.docexplore.reader.book.BookSpecification;
import org.interreg.docexplore.reader.book.ParchmentEngine;
import org.interreg.docexplore.reader.book.ROISpecification;
import org.interreg.docexplore.reader.gfx.Bindable;
import org.interreg.docexplore.reader.gfx.StreamedImage;
import org.interreg.docexplore.reader.gfx.StreamedTexture;
import org.interreg.docexplore.reader.gfx.Texture;
import org.interreg.docexplore.reader.gui.Dialog;
import org.interreg.docexplore.reader.gui.GuiLayer;
import org.interreg.docexplore.reader.net.Connection;
import org.interreg.docexplore.reader.net.ConnectionServer;
import org.interreg.docexplore.reader.net.LocalConnection;
import org.interreg.docexplore.reader.net.LocalServer;
import org.interreg.docexplore.reader.net.ReaderClient;
import org.interreg.docexplore.reader.net.ReaderServer;
import org.interreg.docexplore.reader.net.SocketConnection;
import org.interreg.docexplore.reader.net.SocketServer;
import org.interreg.docexplore.reader.net.StreamedXML;
import org.interreg.docexplore.reader.plugin.InputPlugin;
import org.interreg.docexplore.reader.shelf.ShelfEngine;
import org.interreg.docexplore.reader.sound.SoundManager;
import org.interreg.docexplore.util.ImageUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL10;

public class ReaderApp extends DocExploreTool implements ApplicationListener
{
	public static boolean local = false;
	
	public static interface Module
	{
		public void update();
		public void render();
	}
	
	public Startup startup;
	Thread renderThread;
	List<Module> modules;
	public ShelfEngine shelf;
	public BookEngine bookEngine;
	public ParchmentEngine parchmentEngine;
	public LinkedList<Runnable> renderTasks;
	public ReaderClient client;
	public ReaderServer server = null; //only if local
	public InputManager input;
	public SoundManager sound;
	public DebugGraphics debugGfx;
	public ReaderMainTask mainTask;
	public ActivityLogger logger;
	
	public Bindable emptyTex;
	public Dialog waitDialog = null;
	
	public GuiLayer gui;
	
	public ReaderApp(Startup startup)
	{
		this(startup, null);
	}
	public ReaderApp(Startup startup, ReaderServer server)//only if local
	{
		this.startup = startup;
		this.server = server;
	}
	
	public void create()
	{
		try
		{
			Gdx.graphics.setVSync(true);
			this.renderThread = Thread.currentThread();
			renderThread.setName("Render Thread");
			this.client = new ReaderClient(this, startup);
			client.start("127.0.0.1", 8787);
			this.renderTasks = new LinkedList<Runnable>();
			//this.sound = new SoundManager();
			
			client.registerStreamType(StreamedImage.class, StreamedImage.allocator);
			client.registerStreamType(StreamedTexture.class, StreamedTexture.allocator);
			//client.registerStreamType(StreamedSound.class, StreamedSound.allocator);
			client.registerStreamType(StreamedXML.class, StreamedXML.allocator);
			
			Gdx.gl10.glEnable(GL10.GL_BLEND);
			Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl10.glEnable(GL10.GL_COLOR_MATERIAL);
			Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
			Gdx.gl10.glEnable(GL10.GL_MULTISAMPLE);
			Gdx.gl10.glCullFace(GL10.GL_BACK);
			
			this.modules = new LinkedList<Module>();
			modules.add(shelf = new ShelfEngine(this));
			modules.add(bookEngine = new BookEngine(this, 1, .7f));
			modules.add(parchmentEngine = new ParchmentEngine(this));
			modules.add(gui = new GuiLayer(this));
			modules.add(input = new InputManager(this));
			modules.add(debugGfx = new DebugGraphics(this));
			input.addListenerFirst(gui);
			
			bookEngine.addRoiLayoutListener(new BookEngine.ROILayoutListener() {public void roiLayoutChanged(ROISpecification [] rois) {input.notifyLayoutChange(rois);}});
			
			List<PluginConfig> plugins = startup.filterPlugins(InputPlugin.class);
			for (PluginConfig config : plugins)
			{
				InputPlugin plugin = (InputPlugin)config.clazz.newInstance();
				plugin.setHost(input);
			}
			
			if (!startup.nativeCursor)
				try {Mouse.setNativeCursor(new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1).put(0, 0), null));}
				catch (Exception e) {e.printStackTrace();}
			
			emptyTex = new Texture(ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(
				BookSpecification.class.getPackage().getName().replace('.', '/')+"/emptyPageTex.png")), false);
			
			waitDialog = new Dialog(this);
			waitDialog.set(XMLResourceBundle.getBundledString("waitLabel"), XMLResourceBundle.getBundledString("imagesLabel"));
			gui.addWidget(waitDialog);
			
			this.logger = new ActivityLogger(new File(DocExploreTool.getHomeDir(), "activity.log"), 10000);
			logger.addEntry("Startup");
			
			this.mainTask = new ReaderMainTask(this);
			mainTask.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void dispose()
	{
		//engine.dispose();
		System.exit(0);
	}

	public void pause()
	{
	}
	
	public void submitRenderTask(Runnable runnable)
	{
		if (Thread.currentThread() == renderThread)
			runnable.run();
		else synchronized (renderTasks)
		{
			renderTasks.add(runnable);
		}
	}
	public void submitRenderTaskAndWait(Runnable runnable)
	{
		if (Thread.currentThread() == renderThread)
			runnable.run();
		else synchronized (runnable)
		{
			synchronized (renderTasks) {renderTasks.add(runnable);}
			try {runnable.wait();} catch (InterruptedException e) {}
		}
	}

	Vector<Runnable> currentTasks = new Vector<Runnable>();
	public void render()
	{
		try
		{
			//long now = System.nanoTime();
			
			for (Module module : modules)
				module.update();
			
			//System.out.println("u:  "+(System.nanoTime()-now)*.000001);
			//now = System.nanoTime();
			
			synchronized (renderTasks)
			{
				//if (!renderTasks.isEmpty())
				//	currentTasks.add(renderTasks.removeFirst());
				currentTasks.addAll(renderTasks);
				renderTasks.clear();
			}
			for (Runnable runnable : currentTasks)
				{runnable.run(); synchronized (runnable) {runnable.notifyAll();}}
			currentTasks.clear();
			
			//System.out.println("rt: "+(System.nanoTime()-now)*.000001);
			//now = System.nanoTime();
			
			GL10 gl = Gdx.gl10;
			gl.glClearColor(1, 1, 1, 1);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			for (Module module : modules)
				module.render();
			
			//System.out.println("r:  "+(System.nanoTime()-now)*.000001);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(0);
		}
	}

	public void resize(int arg0, int arg1)
	{
	}

	public void resume()
	{
	}
	
	static LocalServer localServer = null;
	public static ConnectionServer createConnectionServer(int port) throws Exception
	{
		if (local)
			return localServer;
		else return new SocketServer(ServerSocketFactory.getDefault().createServerSocket(port));
	}
	public static Connection createClientConnection(String host, int port) throws Exception
	{
		if (local)
			return new LocalConnection(localServer);
		else return new SocketConnection(SocketFactory.getDefault().createSocket(host, port));
	}
	
	public static void main(String [] args) throws Exception
	{ 
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			if ("Nimbus".equals(info.getName()))
				{UIManager.setLookAndFeel(info.getClassName()); break;}
		
		Startup startup = new Startup("Reader", null, false, false, true, true);
		
		ReaderApp.local = true;
		ReaderApp.localServer = new LocalServer();
		ReaderServer server = new ServerApp(startup).server;
		
		try {Thread.sleep(1000);}
		catch (Exception e) {}
		
		Vector<DisplayMode> displayModes = new Vector<DisplayMode>(Arrays.asList(LwjglApplicationConfiguration.getDisplayModes()));
		boolean fullscreen = false;
		DisplayMode mode = null;
		
		if (startup.winSize != null)
		{
			fullscreen = startup.fullscreen;
			for (DisplayMode dm : displayModes)
				if (dm.width == startup.winSize[0] && dm.height == startup.winSize[1])
					{mode = dm; break;}
		}
		
		if (mode == null)
		{
			final JDialog modeDialog = new JDialog((Frame)null, "Mode", true);
			JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
			mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JList modeList = new JList(displayModes);
			modeList.setSelectedIndex(0);
			mainPanel.add(new JScrollPane(modeList), BorderLayout.NORTH);
			JCheckBox fsBox = new JCheckBox("Fullscreen");
			mainPanel.add(fsBox, BorderLayout.CENTER);
			JButton okButton = new JButton("OK");
			JPanel buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(okButton, BorderLayout.SOUTH);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
			final boolean [] ok = {false};
			okButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
				{ok[0] = true; modeDialog.setVisible(false);}});
			modeDialog.add(mainPanel);
			modeDialog.pack();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	 
			GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gconf);
			modeDialog.setLocation((screenSize.width-(screenInsets.left+screenInsets.right)-modeDialog.getWidth())/2, 
				(screenSize.height-(screenInsets.bottom+screenInsets.top)-modeDialog.getHeight())/2);
			modeDialog.setVisible(true);
			if (!ok[0])
				return;
			mode = (DisplayMode)modeList.getSelectedValue();
			fullscreen = fsBox.isSelected();
		}
		
		//new ResourceMonitor().setVisible(true);
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.stencil = 0;
		config.width = mode.width;
		config.height = mode.height;
		config.useGL20 = false;
		config.fullscreen = fullscreen;
		config.samples = 0;
		config.vSyncEnabled = true;
		config.title = "ReaderApp";
		LwjglApplicationConfiguration.disableAudio = true;
		new LwjglApplication(new ReaderApp(startup, server), config);
	}
}
