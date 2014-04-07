package org.interreg.docexplore.datalink.objects;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.util.Pair;

public class BookData extends ObjectData
{
	final public String name;
	final public List<Integer> pageNumbers;
	
	public BookData(String name, List<Integer> pageNumbers, List<Pair<Integer, Integer>> metaData)
	{
		super(metaData);
		this.name = name;
		this.pageNumbers = new LinkedList<Integer>();
		this.pageNumbers.addAll(pageNumbers);
	}
}
