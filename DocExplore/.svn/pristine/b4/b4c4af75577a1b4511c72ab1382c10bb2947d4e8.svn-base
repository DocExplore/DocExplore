package org.interreg.docexplore.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.interreg.docexplore.util.GuiUtils;

public abstract class ExpandingItem extends JPanel
{
	private static final long serialVersionUID = -4835832257983193792L;

	boolean isExpanded;
	ExpandingItemList list;
	
	public ExpandingItem()
	{
		this.isExpanded = false;
		this.list = null;
		
		addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() != MouseEvent.BUTTON1 || list == null || isExpanded)
					return;
				list.contractAll();
				expand();
			}
		});
	}
	
	public void expand()
	{
		if (list == null || isExpanded)
			return;
		isExpanded = true;
		removeAll();
		GuiUtils.blockUntilComplete(new Runnable() {public void run() {fillExpandedState();}}, this);
		
		list.getParent().validate();
	}
	public void contract()
	{
		if (list == null || !isExpanded)
			return;
		isExpanded = false;
		removeAll();
		fillContractedState();
		
		list.getParent().validate();
	}
	
	protected abstract void fillContractedState();
	protected abstract void fillExpandedState();

//	protected void paintChildren(Graphics g)
//	{
//		//if (isOpaque())
//		{//System.out.println(getBackground().getRed()+","+getBackground().getGreen()+","+getBackground().getBlue()+","+getBackground().getAlpha());
//			g.setColor(getBackground());
//			g.fillRect(0, 0, getWidth(), getHeight());
//		}
//		
//		super.paintChildren(g);
//	}
	
	
}
