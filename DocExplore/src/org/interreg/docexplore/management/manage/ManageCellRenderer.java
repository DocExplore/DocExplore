package org.interreg.docexplore.management.manage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.ImageUtils;

public class ManageCellRenderer implements ListCellRenderer
{
	Color selectionColor;
	int targetRow;
	int targetDiff;
	
	public ManageCellRenderer()
	{
		this.selectionColor = new Color(10, 36, 106);
		this.targetRow = -1;
	}
	
	Icon loading = ImageUtils.getIcon("page_search-24x24.png");
	Color odd = new Color(1f, .95f, .95f), even = new Color(.95f, .95f, 1f);
	Color oddDark = new Color(.75f, .45f, .45f), evenDark = new Color(.45f, .45f, .75f);
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus)
	{
		JLabel label = new JLabel(((Book)value).getName(), ImageUtils.getIcon("book-48x48.png"), SwingConstants.LEFT);
		
		JPanel p1 = new JPanel(new BorderLayout());
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p1.add(p2);
		p2.add(label);
		p2.setOpaque(false);
		
		p1.setBackground(index%2==0 ? even : odd);
		if (selected)
		{
			p1.setBackground(list.getSelectionBackground());
			label.setForeground(list.getSelectionForeground());
		}
		else
		{
			p1.setBackground(index%2==0 ? even : odd);
			label.setForeground(Color.black);
			p1.setBorder(BorderFactory.createLineBorder(index%2==0 ? evenDark : oddDark, 2));
		}
		
		return p1;
	}
	
}
