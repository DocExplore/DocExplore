package org.interreg.docexplore.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.interreg.docexplore.util.ImageUtils;

public class IconToggleButton extends JToggleButton
{
	private static final long serialVersionUID = 8486184751157518154L;

	public IconToggleButton(Icon icon)
	{
		super(icon);
		setPreferredSize(new Dimension(getIcon().getIconWidth()+12, getIcon().getIconHeight()+12));
	}
	public IconToggleButton(String iconName)
	{
		this(ImageUtils.getIcon(iconName));
	}
	
	public IconToggleButton(Icon icon, String toolTip)
	{
		this(icon);
		this.setToolTipText(toolTip);
	}
	public IconToggleButton(String iconName, String toolTip)
	{
		this(iconName);
		this.setToolTipText(toolTip);
	}
	
	public IconToggleButton(String iconName, ActionListener action)
	{
		this(iconName);
		addActionListener(action);
	}
	
	public IconToggleButton(String iconName, String toolTip, ActionListener action)
	{
		this(iconName, action);
		this.setToolTipText(toolTip);
	}
}
