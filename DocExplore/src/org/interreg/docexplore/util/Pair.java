package org.interreg.docexplore.util;

public class Pair<T, U>
{
	public T first;
	public U second;
	
	public Pair()
	{
		this.first = null;
		this.second = null;
	}
	
	public Pair(T first, U second)
	{
		this.first = first;
		this.second = second;
	}
	
	public String toString()
	{
		return "Pair<"+first+", "+second+">";
	}
}
