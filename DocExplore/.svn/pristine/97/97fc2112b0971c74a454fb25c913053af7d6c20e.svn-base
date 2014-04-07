package org.interreg.docexplore.util;

import java.util.Collection;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class ParallelFor<T>
{
	public ParallelFor(int parallelism, final Collection<T> col)
	{
		final LinkedBlockingDeque<T> stack = new LinkedBlockingDeque<T>(col);
		int nTasks = Math.min(parallelism, col.size());
		final CyclicBarrier barrier = new CyclicBarrier(nTasks+1);
		for (int i=0;i<nTasks;i++)
			new Thread() {public void run()
			{
				T t = null;
				while ((t = stack.pollFirst()) != null)
					try {go(t);} catch (Exception e) {e.printStackTrace();}
				try {barrier.await();} catch (Exception e) {}
			}}.start();
		try {barrier.await();} catch (Exception e) {}
	}
	
	public abstract void go(T t) throws Exception;
}
