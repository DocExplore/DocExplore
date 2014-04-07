package org.interreg.docexplore.management.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;

@SuppressWarnings("serial")
public class ViewPanel extends JPanel
{
	JLabel unfilteredCanvas, filteredCanvas;
	BufferedImage unfiltered, filtered;
	
	public ViewPanel()
	{
		super(new BorderLayout());
		
		this.unfilteredCanvas = new JLabel() {public void paintComponent(Graphics g)
		{
			//g.setColor(getBackground());
			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(), getHeight());
			if (unfiltered != null)
				g.drawImage(unfiltered, 0, 0, getWidth()-1, getHeight()-1, 
					0, 0, unfiltered.getWidth()-1, unfiltered.getHeight()-1, null);
		}};
		this.filteredCanvas = new JLabel() {public void paintComponent(Graphics g)
		{
			//g.setColor(getBackground());
			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(), getHeight());
			if (filtered != null)
				g.drawImage(filtered, 0, 0, getWidth()-1, getHeight()-1, 
					0, 0, filtered.getWidth()-1, filtered.getHeight()-1, null);
		}};
		unfilteredCanvas.setPreferredSize(new Dimension(100, 100));
		filteredCanvas.setPreferredSize(new Dimension(100, 100));
		
		JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
		imagePanel.add(unfilteredCanvas);
		imagePanel.add(filteredCanvas);
		add(imagePanel, BorderLayout.NORTH);
	}
	
	public void updatePreview(FilterPanel filterPanel)
	{
		if (unfiltered == null || filterPanel.sequencePanel.getModel().getSize() < 1)
			return;
		
		BufferedImage buffer = new BufferedImage(unfiltered.getWidth(), unfiltered.getHeight(), BufferedImage.TYPE_INT_RGB);
		buffer.createGraphics().drawImage(unfiltered, 0, 0, null);
		filtered = new BufferedImage(unfiltered.getWidth(), unfiltered.getHeight(), BufferedImage.TYPE_INT_RGB);
		ListModel list = filterPanel.sequencePanel.getModel();
		
		for (int i=0;i<list.getSize();i++)
		{
			filtered.createGraphics().drawImage(buffer, 0, 0, null);
			FilterSequencePanel.SequenceElement elem = (FilterSequencePanel.SequenceElement)list.getElementAt(i);
			elem.apply(buffer, filtered);
			
			BufferedImage tmp = filtered;
			filtered = buffer;
			buffer = tmp;
		}
		
		filtered = buffer;
		filteredCanvas.repaint();
	}
}
