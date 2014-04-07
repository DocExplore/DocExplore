package org.interreg.docexplore.management.image;

import java.awt.Point;

public class CropOperation extends SquareOperation
{
	public void pointDropped(PageViewer ic, Point point, int modifiers)
	{
		second = new Point(point);
		int tlx = Math.min(first.x, second.x), tly = Math.min(first.y, second.y);
		int brx = Math.max(first.x, second.x), bry = Math.max(first.y, second.y);
		ic.cropPage(tlx, tly, brx, bry);
		this.first = null;
		this.second = null;
	}
}
