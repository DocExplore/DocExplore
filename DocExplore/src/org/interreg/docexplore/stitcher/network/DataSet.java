package org.interreg.docexplore.stitcher.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataSet implements Serializable
{
	private static final long serialVersionUID = 8347924010637532151L;
	
	List<double [][]> data;
	
	public DataSet()
	{
		this.data = new ArrayList<>();
	}
	
	public void add(double [][] sample)
	{
		data.add(sample);
	}
	
	public void addIdentity(double [] val)
	{
		add(new double [][] {val, val});
	}
}
