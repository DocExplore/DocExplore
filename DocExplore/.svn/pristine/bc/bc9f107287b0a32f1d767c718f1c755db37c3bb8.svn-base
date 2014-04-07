package org.interreg.docexplore.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * A custom layout in which components are vertically laid out from top to bottom.
 * @author Burnett
 */
public class VerticalFlowLayout implements LayoutManager
{
	int hgap, vgap;
	boolean fill;
	
	/**
	 * Creates a new vertical layout.
	 * @param hgap Margin on the sides.
	 * @param vgap Margin between components.
	 * @param fill Forces all the components to the same width.
	 */
	public VerticalFlowLayout(int hgap, int vgap, boolean fill)
	{
		this.hgap = hgap;
		this.vgap = vgap;
		this.fill = fill;
	}
	/**
	 * Creates a new vertical layout.
	 * @param fill Forces all the components to the same width.
	 */
	public VerticalFlowLayout(boolean fill)
	{
		this(5, 5, fill);
	}
	/**
	 * Creates a new vertical layout where components can have any width.
	 */
	public VerticalFlowLayout()
	{
		this(false);
	}
	
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int topInset = insets.top;
		int leftInset = insets.left;
		
		Dimension rawSize = parent.getSize();
		Dimension size = new Dimension((int)(rawSize.getWidth()-(leftInset+insets.right)), 
			(int)(rawSize.getHeight()-(topInset+insets.bottom)));
		
		Dimension rawPreferred = preferredLayoutSize(parent);
		Dimension preferred = new Dimension((int)(rawPreferred.getWidth()-(leftInset+insets.right)), 
				(int)(rawPreferred.getHeight()-(topInset+insets.bottom)));
		/*Dimension size = parent.getSize();
		Dimension preferred = preferredLayoutSize(parent);*/
		
		double gapHeight = vgap*(parent.getComponents().length+1);
		
		double heightRatio = 1;
		if (size.getHeight() < preferred.getHeight())
			heightRatio = (size.getHeight()-gapHeight)/(preferred.getHeight()-gapHeight);
		
		double currentHeight = vgap+topInset;
		for (Component component : parent.getComponents())//components)
		{
			Dimension componentSize = component.getPreferredSize();
			double height = componentSize.getHeight()*heightRatio;
			
			if (!fill && size.getWidth() >= componentSize.getWidth()+2*hgap)
			{
				double left = (size.getWidth()-componentSize.getWidth())/2;
				component.setBounds((int)left, (int)currentHeight, (int)componentSize.getWidth(), (int)height);
			}
			else component.setBounds(hgap+leftInset, (int)(currentHeight), parent.getWidth()-2*hgap-insets.right, (int)height);
				
			currentHeight += height+vgap;
		}
	}

	public Dimension minimumLayoutSize(Container parent)
	{
		int width = hgap, height = vgap;
		for (Component component : parent.getComponents())//components)
		{
			Dimension minimum = component.getMinimumSize();
			height += (int)(minimum.getHeight()+vgap);
			
			if (width < minimum.getWidth()+2*hgap) width = (int)(minimum.getWidth()+2*hgap);
		}
		Insets insets = parent.getInsets();
		return new Dimension(width+insets.left+insets.right, height+insets.top+insets.bottom);
	}

	public Dimension preferredLayoutSize(Container parent)
	{
		int width = hgap, height = vgap;
		for (Component component : parent.getComponents())//components)
		{
			Dimension preferred = component.getPreferredSize();
			height += (int)(preferred.getHeight()+vgap);
			
			if (width < preferred.getWidth()+2*hgap) width = (int)(preferred.getWidth()+2*hgap);
		}
		Insets insets = parent.getInsets();
		return new Dimension(width+insets.left+insets.right, height+insets.top+insets.bottom);
	}

	public void removeLayoutComponent(Component comp) {}
	public void addLayoutComponent(String name, Component comp) {}
}
