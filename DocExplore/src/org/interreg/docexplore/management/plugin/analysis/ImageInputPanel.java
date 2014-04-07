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
