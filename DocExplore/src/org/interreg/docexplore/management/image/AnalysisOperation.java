package org.interreg.docexplore.management.image;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.interreg.docexplore.internationalization.XMLResourceBundle;

public class AnalysisOperation extends SquareOperation
{
	public AnalysisOperation()
	{
		super();
	}
	
	public void pointClicked(PageViewer ic, Point point, int modifiers, int clickCount)
	{
		if (second != null)
			return;
		if (first == null)
			first = new Point(point);
		else
		{
			second = new Point(point);
			
			int x = Math.min(first.x, second.x), y = Math.min(first.y, second.y);
			int w = Math.abs(first.x-second.x), h = Math.abs(first.y-second.y);
			BufferedImage area = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			area.createGraphics().drawImage(ic.image.getSubimage(x, y, w, h), 0, 0, null);
			ic.notifyAnalysisRequested(area);
			
			this.first = null;
			this.second = null;
		}
	}
	
	public String getMessage() {return XMLResourceBundle.getBundledString("statusAnalysisMessage");}
}
