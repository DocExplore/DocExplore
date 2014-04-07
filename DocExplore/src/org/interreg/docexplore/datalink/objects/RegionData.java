package org.interreg.docexplore.datalink.objects;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.util.Pair;

public class RegionData extends ObjectData
{
	final public List<Point> outline;
	
	public RegionData(List<Point> outline, List<Pair<Integer, Integer>> metaData)
	{
		super(metaData);
		this.outline = new LinkedList<Point>();
		this.outline.addAll(outline);
	}
}
