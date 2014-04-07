package org.interreg.docexplore.reader;


import com.badlogic.gdx.Input;

public class TimeoutMonitor
{
	ReaderApp app;
	long elapsed = 0;
	final long limit;
	
	public TimeoutMonitor(ReaderApp app)
	{
		this.app = app;
		limit = 1000*app.startup.idle;
	}
	
	public void start()
	{
		if (limit > 0)
			new Thread()
			{
				public void run()
				{
					long last = System.currentTimeMillis();
					while (true)
					{
						try {Thread.sleep(500);}
						catch (Exception e) {}
						
						long now = System.currentTimeMillis();
						elapsed += now-last;
						last = now;
						
						if (elapsed > limit)
						{
							elapsed = 0;
							if (!app.shelf.active)
								app.input.notifyKeyTyped(Input.Keys.ESCAPE);
						}
					}
				}
			}.start();
	}
	
	public void reset() {elapsed = 0;}
}
