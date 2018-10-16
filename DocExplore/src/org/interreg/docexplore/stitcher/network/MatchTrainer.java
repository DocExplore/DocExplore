/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.stitcher.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class MatchTrainer
{
	public static Network train(Network network, DataSet data, double hitRate)
	{
		Network best = null, init = new Network(network);
		double bestErr = 0;
		
		while (best == null)
		{
			long hits = 0;
			best = null;
			
			network.setTo(init);
			for (long iteration=0;;iteration++)
			{
				double err = 0, logErr = 0;
				for (int i=0;i<data.data.size();i++)
				{
					double [][] sample = data.data.get(i);
					double [] output = network.compute(sample[0]);
					for (int j=0;j<output.length;j++)
						err += (output[j]-sample[1][j])*(output[j]-sample[1][j]);
					for (int j=0;j<output.length;j++)
						output[j] = output[j] < .5 ? 0 : 1;
					for (int j=0;j<output.length;j++)
						logErr += (output[j]-sample[1][j])*(output[j]-sample[1][j]);
				}
				if (best == null || err < bestErr)
				{
					hits++;
					System.out.printf("err: %.7f, logerr: %.1f, hits: %.4f\n", err, logErr, hits*1./(iteration+1));
					if (best == null)
						best = new Network(network);
					else best.setTo(network);
					bestErr = err;
				}
				if (iteration > 100000 && hits*1./(iteration+1) < hitRate)
					break;
				
				network.setTo(best);
				network.mutate(.01*Math.random()*Math.random());
				
				iteration++;
			}
		}
		return best;
	}
	
	public static void main(String [] args) throws Exception
	{
		DataSet data = new DataSet();
		
		List<double [][]> samples = null;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
			new File("C:\\Users\\aburn\\Documents\\work\\git\\DocExplore\\DocExplore\\src\\org\\interreg\\docexplore\\stitcher\\data")));
		samples = (List)in.readObject();
		System.out.println(samples.size()+" training samples");
		for (double [][] sample : samples)
			data.add(sample);
		
		Network network = new Network(5, new double [][][] {
			new double [5][6],
			new double [1][6]
		});
		
		for (int i=0;i<13;i++)
		{
			double [] val = new double [12];
			if (i < 12)
				val[i] = 1;
			data.addIdentity(val);
		}
		
		double lim = .01;
		int cnt = 1;
		network = train(network, data, lim/cnt);
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
			new File("C:\\Users\\aburn\\Documents\\work\\git\\DocExplore\\DocExplore\\src\\org\\interreg\\docexplore\\stitcher\\model")));
		out.writeObject(network);
		out.close();
	}
}
