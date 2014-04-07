package org.interreg.docexplore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.interreg.docexplore.util.ImageUtils;

public class SplashScreen extends JPanel
{
	private static final long serialVersionUID = -4494494801280290970L;
	
	public final static String versionString = "2.0";

	JLabel status;
	
	@SuppressWarnings("serial")
	public SplashScreen(String logo)
	{
		super(new BorderLayout());
		
		setBackground(Color.white);
		status = new JLabel("");
		status.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		//JLabel version = new JLabel(versionString);
		//version.setVerticalAlignment(SwingConstants.BOTTOM);
		add(new JLabel(ImageUtils.getIcon(logo)) {protected void paintComponent(Graphics _g)
			{
				super.paintComponent(_g);
				Graphics2D g = (Graphics2D)_g;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setFont(Font.decode("Arial-italic-18"));
				g.setColor(Color.black);
				g.drawString("version "+versionString, 10, 20);
				
			}}, 
		BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
		//add(version, BorderLayout.EAST);
		setBorder(BorderFactory.createLineBorder(Color.black, 2));
	}
	
	public void setText(String text)
	{
		status.setText(text);
		status.repaint();
	}
}
