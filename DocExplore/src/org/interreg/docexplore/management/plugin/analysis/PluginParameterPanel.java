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
