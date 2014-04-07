package org.interreg.docexplore.gui;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class VLabel extends JPanel
{
	private static final long serialVersionUID = 5205739493767168478L;

	public VLabel(Icon icon, String text)
	{
		super(new VerticalFlowLayout(false));
		
		add(new JLabel(icon, SwingConstants.CENTER), 0);
		add(new JLabel(text, SwingConstants.CENTER), 1);
	}
	public VLabel() {this(null, "");}
	
	public Icon getIcon()
	{
		return ((JLabel)getComponent(0)).getIcon();
	}
	public void setIcon(Icon icon)
	{
		((JLabel)getComponent(0)).setIcon(icon);
	}
	
	public String getText()
	{
		return ((JLabel)getComponent(1)).getText();
	}
	public void setText(String text)
	{
		((JLabel)getComponent(1)).setText(text);
	}
}
