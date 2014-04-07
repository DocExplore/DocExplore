package org.interreg.docexplore.authoring.explorer;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.interreg.docexplore.util.StringUtils;

@SuppressWarnings("serial")
public class ViewItem extends JPanel
{
	public static class Data
	{
		public String name, description;
		public final Object object;
		
		public Data(String name, String description, Object object)
		{
			this.name = name;
			this.description = description;
			this.object = object;
		}
	}
	public JLabel iconLabel, nameLabel;
	public Data data;
	
	public ViewItem(String name, String description, Object object)
	{
		super(new BorderLayout());
		
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.data = new Data(name, description, object);
		add(nameLabel = new JLabel("<html><b><center>"+
			StringUtils.breakDown(name, 32, 3, "<br/>")+
			"</center></b></html>", SwingConstants.CENTER), BorderLayout.SOUTH);
		this.iconLabel = null;
	}
	
	public void setForList()
	{
		if (iconLabel != null) remove(iconLabel);
		nameLabel.setText("<html><b>"+data.name+"</b>&nbsp;&nbsp;&nbsp;"+data.description+"</html>");
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
	}
	public void setForIcon(ExplorerView view)
	{
		if (iconLabel == null)
			iconLabel = new JLabel(view.getIcon(data.object), SwingConstants.CENTER);
		add(iconLabel, BorderLayout.NORTH);
		nameLabel.setText("<html><b><center>"+
			StringUtils.breakDown(data.name, 16, 3, "<br/>")+
			"</center></b></html>");
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	public int getBaseline(int width, int height)
	{
		if (iconLabel != null)
			return iconLabel.getHeight();
		return -1;
	}
}
