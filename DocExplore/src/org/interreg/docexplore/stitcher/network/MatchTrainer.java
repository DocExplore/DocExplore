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
