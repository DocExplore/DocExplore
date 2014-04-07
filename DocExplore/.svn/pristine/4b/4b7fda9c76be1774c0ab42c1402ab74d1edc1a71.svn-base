package org.interreg.docexplore.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import org.interreg.docexplore.util.ImageUtils;

public class IconButton extends JButton
{
	private static final long serialVersionUID = 5582537475781109316L;
	
	public IconButton(String iconName)
	{
		this(ImageUtils.getIcon(iconName));
	}
	public IconButton(Icon icon)
	{
		super(icon);
		setPreferredSize(new Dimension(getIcon().getIconWidth()+12, getIcon().getIconHeight()+12));
	}
	
	public IconButton(String iconName, String toolTip)
	{
		this(iconName);
		this.setToolTipText(toolTip);
	}
	public IconButton(Icon icon, String toolTip)
	{
		this(icon);
		this.setToolTipText(toolTip);
	}
	
	public IconButton(String iconName, ActionListener action)
	{
		this(iconName);
		addActionListener(action);
	}
	
	public IconButton(String iconName, String toolTip, ActionListener action)
	{
		this(iconName, action);
		this.setToolTipText(toolTip);
	}
}
