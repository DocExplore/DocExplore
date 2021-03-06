/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
