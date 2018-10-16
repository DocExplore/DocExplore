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

import java.io.Serializable;
import java.util.Arrays;

public class Network implements Serializable
{
	private static final long serialVersionUID = 7984505335918673739L;

	static class Neuron implements Serializable
	{
		private static final long serialVersionUID = 8109990607193113453L;
		double [] weights;
		
		Neuron(double [] weights) {this.weights = weights;}
		
		public Neuron copy()
		{
			return new Neuron(Arrays.copyOf(weights, weights.length));
		}
		
		public void setTo(Neuron neuron)
		{
			for (int i=0;i<weights.length;i++)
				weights[i] = neuron.weights[i];
		}
		
		public double compute(double [] inputs)
		{
			double sum = weights[0];
			for (int i=0;i<inputs.length;i++)
				sum += inputs[i]*weights[i+1];
			//return (sum/((sum*sum)/(Math.abs(sum)+1)+1)+1)/2;
			return 1/(1+Math.exp(-sum));
		}
		
		public void mutate(double amount)
		{
			for (int i=0;i<weights.length;i++)
				weights[i] += amount*(Math.random()-.5);
		}
	}
	
	Neuron [][] layers;
	double [][] outputs;
	
	public Network(int nInputs, double [][][] weights)
	{
		this.layers = new Neuron [weights.length][];
		for (int i=0;i<layers.length;i++)
		{
			layers[i] = new Neuron [weights[i].length];
			for (int j=0;j<layers[i].length;j++)
				layers[i][j] = new Neuron(weights[i][j]);
		}
		
		outputs = new double [layers.length+1][];
		for (int i=0;i<outputs.length;i++)
			outputs[i] = new double [i == 0 ? nInputs : layers[i-1].length];
	}
	
	public Network(Network network)
	{
		this.layers = new Neuron [network.layers.length][];
		for (int i=0;i<layers.length;i++)
		{
			layers[i] = new Neuron [network.layers[i].length];
			for (int j=0;j<layers[i].length;j++)
				layers[i][j] = network.layers[i][j].copy();
		}
		
		outputs = new double [layers.length+1][];
		for (int i=0;i<outputs.length;i++)
			outputs[i] = Arrays.copyOf(network.outputs[i], network.outputs[i].length);
	}
	
	public void setTo(Network network)
	{
		for (int i=0;i<layers.length;i++)
			for (int j=0;j<layers[i].length;j++)
				layers[i][j].setTo(network.layers[i][j]);
	}
	
	public void reset()
	{
		for (int i=0;i<outputs.length;i++)
			for (int j=0;j<outputs[i].length;j++)
				outputs[i][j] = 0;
	}
	
	public double [] compute(double [] inputs)
	{
		for (int i=0;i<outputs[0].length;i++)
			outputs[0][i] = inputs[i];
		for (int i=0;i<layers.length;i++)
			for (int j=0;j<layers[i].length;j++)
				outputs[i+1][j] = layers[i][j].compute(outputs[i]);
		return outputs[outputs.length-1];
	}
	
	public void mutate(double amount)
	{
		for (int i=0;i<layers.length;i++)
			for (int j=0;j<layers[i].length;j++)
				layers[i][j].mutate(amount);
	}
}
