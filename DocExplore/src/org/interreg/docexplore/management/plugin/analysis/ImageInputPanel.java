/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.plugin.analysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class ImageInputPanel extends JPanel
{
	public static class PluginImageInput extends JPanel
	{
		BufferedImage image;
		JToggleButton includeButton;
		
		public PluginImageInput(BufferedImage image)
		{
			super(new BorderLayout());
			
			this.image = image;
			add(new JLabel(new ImageIcon(ImageUtils.createIconSizeImage(image, 256))), BorderLayout.NORTH);
			
			JPanel buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(includeButton = new JToggleButton(ImageUtils.getIcon("ok-24x24.png")));
			includeButton.setSelected(true);
			buttonPanel.add(new JButton(new AbstractAction("", ImageUtils.getIcon("remove-24x24.png"))
			{
				public void actionPerformed(ActionEvent e)
				{
					Container parent = PluginImageInput.this.getParent();
					parent.remove(PluginImageInput.this);
					parent.validate();
					parent.repaint();
				}
			}));
			add(buttonPanel, BorderLayout.SOUTH);
		}
	}
	
	JPanel content;
	public JCheckBox autoOpen;
	
	public ImageInputPanel()
	{
		super(new BorderLayout());
		
		content = new JPanel(new WrapLayout());
		add(new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		JPanel infoPanel = new JPanel(new BorderLayout());
		
		JPanel helpPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		helpPanel.add(new JLabel(XMLResourceBundle.getBundledString("pluginAnalysisInputFirstMessage")));
		helpPanel.add(new JLabel(ImageUtils.getIcon("analysis-24x24.png")));
		helpPanel.add(new JLabel(XMLResourceBundle.getBundledString("pluginAnalysisInputSecondMessage")));
		infoPanel.add(helpPanel, BorderLayout.NORTH);
		
		JPanel autoPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		autoPanel.add(autoOpen = new JCheckBox(XMLResourceBundle.getBundledString("pluginAutoOpenLabel"), true));
		infoPanel.add(autoPanel, BorderLayout.SOUTH);
		
		add(infoPanel, BorderLayout.SOUTH);
	}
	
	public void addInput(BufferedImage image)
	{
		content.add(new PluginImageInput(image));
	}
	
	public BufferedImage [] getIncludedImages()
	{
		List<BufferedImage> images = new LinkedList<BufferedImage>();
		for (Component comp : content.getComponents())
			if (comp instanceof PluginImageInput && ((PluginImageInput)comp).includeButton.isSelected())
				images.add(((PluginImageInput)comp).image);
		return images.toArray(new BufferedImage [images.size()]);		
	}
}
