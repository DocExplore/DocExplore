package org.interreg.docexplore.stitcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

@SuppressWarnings("serial")
public class FragmentDescriptionView extends NavView
{
	StitchEditor editor;
	FragmentDescription desc = null;
	
	public FragmentDescriptionView(StitchEditor editor)
	{
		super();
		this.editor = editor;
		this.scale = 1;
	}
	
	@Override protected void onViewChange()
	{
		editor.repaint();
	}
	
	public void setImageDescription(FragmentDescription desc)
	{
		this.desc = desc;
		if (desc != null)
			fitView(0, 0, desc.image.getWidth(), desc.image.getHeight(), .1);
		else repaint();
	}
	
	Rectangle2D.Double rect = new Rectangle2D.Double();
	@Override protected void drawView(Graphics2D g, double pixelSize)
	{
		if (desc != null)
		{
			Font font = g.getFont();
			g.setFont(font.deriveFont((float)(16f/pixelSize)));
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(desc.fragment.file.getName(), g);
			double sh = g.getFont().getSize2D();
			g.setColor(new Color(0f, 0f, 0f, .5f));
			double x0 = toViewX(0), y0 = toViewY(0)+sh;
			g.fill(new Rectangle2D.Double(x0, y0-sh, bounds.getWidth(), 1.5*sh));
			g.setColor(Color.white);
			g.drawString(desc.fragment.file.getName(), (float)x0, (float)y0);
			
			g.drawImage(desc.image, 0, 0, null);
			
			g.setColor(Color.red);
			for (int i=0;i<desc.features.size();i++)
			{
				rect.setRect(desc.features.get(i).x-1, desc.features.get(i).y-1, 2, 2);
				g.draw(rect);
			}
		}
	}
}
