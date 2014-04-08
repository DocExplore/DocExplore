/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
