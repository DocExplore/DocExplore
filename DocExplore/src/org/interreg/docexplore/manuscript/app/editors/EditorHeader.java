package org.interreg.docexplore.manuscript.app.editors;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.gui.WrapLayout;

@SuppressWarnings("serial")
public class EditorHeader extends JPanel
{
	private JLabel titleLabel;
	public JPanel titlePanel, rightPanel;
	
	public EditorHeader()
	{
		super(new BorderLayout());
		
		setBackground(Color.white);
		
		WrapLayout leftLayout = new WrapLayout(WrapLayout.LEFT);
		leftLayout.setHgap(20);
		this.titlePanel = new JPanel(leftLayout);
		this.titleLabel = new JLabel("", SwingConstants.LEFT);
		titlePanel.add(titleLabel);
		JPanel leftPanel = new JPanel(new LooseGridLayout(1, 1, 0, 0, false, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		leftPanel.setOpaque(false);
		leftPanel.add(titlePanel);
		add(leftPanel, BorderLayout.WEST);
		
		JPanel centerPanel = new JPanel(new LooseGridLayout(1, 1, 0, 0, true, false, SwingConstants.RIGHT, SwingConstants.CENTER, true, true));
		centerPanel.setOpaque(false);
		WrapLayout rightLayout = new WrapLayout(WrapLayout.RIGHT);
		rightLayout.setHgap(20);
		this.rightPanel = new JPanel(rightLayout);
		centerPanel.add(rightPanel);
		rightPanel.setOpaque(false);
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public void setTitle(String title, String subTitle)
	{
		titleLabel.setText("<html><big>"+title+"</big><br>"+subTitle+"</html></big>");
	}
	public void setTitleIcon(Icon icon)
	{
		titleLabel.setIcon(icon);
	}
}
