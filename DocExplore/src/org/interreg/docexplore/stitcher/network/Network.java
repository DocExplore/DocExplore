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
