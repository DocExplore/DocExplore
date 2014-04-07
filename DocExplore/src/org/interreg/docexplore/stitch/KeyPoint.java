package org.interreg.docexplore.stitch;

import java.io.Serializable;

public class KeyPoint implements Serializable
{
	private static final long serialVersionUID = 3903534242060954579L;
	
	double x, y;
	KeyPoint link = null;
	
	public KeyPoint(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void clearLink()
	{
		if (link != null)
			link.link = null;
		link = null;
	}
	public void linkTo(KeyPoint point)
	{
		clearLink();
		point.clearLink();
		link = point;
		point.link = this;
	}
}