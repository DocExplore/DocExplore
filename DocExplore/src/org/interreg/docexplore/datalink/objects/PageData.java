package org.interreg.docexplore.datalink.objects;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.util.Pair;

public class PageData extends ObjectData
{
	final public int pageId;
	final public List<Integer> regionIds;
	
	public PageData(int pageId, List<Integer> regionIds, List<Pair<Integer, Integer>> metaData)
	{
		super(metaData);
		this.pageId = pageId;
		this.regionIds = new LinkedList<Integer>();
		this.regionIds.addAll(regionIds);
	}
}
