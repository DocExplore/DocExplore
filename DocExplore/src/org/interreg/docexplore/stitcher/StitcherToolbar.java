package org.interreg.docexplore.stitcher;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.interreg.docexplore.gui.WrapLayout;

@SuppressWarnings("serial")
public class StitcherToolbar extends JPanel
{
	Stitcher stitcher;
	
	public StitcherToolbar(Stitcher stitcher)
	{
		super(new WrapLayout(WrapLayout.CENTER));
		
		this.stitcher = stitcher;
		add(new JButton("Detect layout"));
		add(new JButton("Compute links"));
	}
}
