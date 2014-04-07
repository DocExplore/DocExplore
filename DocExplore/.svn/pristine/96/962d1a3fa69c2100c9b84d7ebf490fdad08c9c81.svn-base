package org.interreg.docexplore.management.plugin.analysis;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;

@SuppressWarnings("serial")
public class PluginParameterPanel extends JPanel
{
	public PluginParameterPanel()
	{
		super(new BorderLayout());
		
		add(new JScrollPane(area = new JTextArea(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		JPanel helpPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		helpPanel.add(new JLabel(XMLResourceBundle.getBundledString("pluginParameterFirstMessage")));
		helpPanel.add(new JLabel(XMLResourceBundle.getBundledString("pluginParameterSecondMessage")));
		add(helpPanel, BorderLayout.SOUTH);
	}
	
	JTextArea area;
	
	public Map<String, String> getParameterMap()
	{
		Map<String, String> res = new TreeMap<String, String>();
		
		String content = area.getText();
		//System.out.println(content);
		int begin = 0, index;
		while (begin < content.length())
		{
			index = begin;
			while (index < content.length() && (Character.isWhitespace(content.charAt(index)) || content.charAt(index) == ','))
				index++;
			begin = index;
			while (index < content.length() && (content.charAt(index) != ',' && content.charAt(index) != '\n'))
				index++;
			String param = content.substring(begin, index);
			String [] split = param.split("=");
			if (split.length == 2)
				res.put(split[0].trim(), split[1].trim());
			begin = index;
		}
		//for (Map.Entry<String, String> entry : res.entrySet())
		//	System.out.println(entry.getKey()+" => "+entry.getValue());
		return res;
	}
}
