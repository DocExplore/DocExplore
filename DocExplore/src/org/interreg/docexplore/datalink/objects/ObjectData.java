package org.interreg.docexplore.datalink.objects;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.util.Pair;

public class ObjectData
{
	//<object id, metadata key id>
	final public List<Pair<Integer, Integer>> metaData;
	
	public ObjectData(List<Pair<Integer, Integer>> metaData)
	{
		this.metaData = new LinkedList<Pair<Integer, Integer>>();
		this.metaData.addAll(metaData);
	}
}
