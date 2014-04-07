package org.interreg.docexplore.management.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and 
 * a JButton to close the tab it belongs to 
 */ 
@SuppressWarnings("serial")
public class DocumentTab extends JPanel
{
	private JLabel label;
	
	public void setTitle(String title)
	{
		label.setText(title);
		label.repaint();
	}
	
	public DocumentTab(String title, final JTabbedPane pane)
	{
		//unset default FlowLayout' gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setOpaque(false);

		//make JLabel read titles from JTabbedPane
		label = new JLabel(title); 
		add(label);
		
		//add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		//tab button
		JButton button = new JButton()
		{
			private static final long serialVersionUID = 4327861473442684528L;

			//we don't want to update UI for this button
			public void updateUI() {}

			//paint the cross
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();

				//shift the image for pressed buttons
				if (getModel().isPressed()) g2.translate(1, 1);

				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				if (getModel().isRollover()) g2.setColor(Color.MAGENTA);

				int delta = 6;
				g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
				g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
				g2.dispose();
			}
		};
		int size = 17;
		button.setPreferredSize(new Dimension(size, size));
		//button.setToolTipText(XMLResourceBundle.getBundledString("generalCloseTab"));
		//Make the button looks the same for all Laf's
		button.setUI(new BasicButtonUI());
		//Make it transparent
		button.setContentAreaFilled(false);
		//No need to be focusable
		button.setFocusable(false);
		button.setBorder(BorderFactory.createEtchedBorder());
		button.setBorderPainted(false);
		//Making nice rollover effect
		//we use the same listener for all buttons
		button.addMouseListener(buttonMouseListener);
		button.setRolloverEnabled(true);
		//Close the proper tab by clicking the button
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Component comp = pane.getComponentAt(pane.indexOfTabComponent(DocumentTab.this));
				if (comp instanceof DocumentPanel)
					((DocumentPanel)comp).documentIsClosing(null);
				pane.remove(pane.indexOfTabComponent(DocumentTab.this));
			}
		});
		add(button);

		//add more space to the top of the component
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	private final static MouseListener buttonMouseListener = new MouseAdapter()
	{
		public void mouseEntered(MouseEvent e)
		{
			Component component = e.getComponent();
			if (component instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e)
		{
			Component component = e.getComponent();
			if (component instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};
}


