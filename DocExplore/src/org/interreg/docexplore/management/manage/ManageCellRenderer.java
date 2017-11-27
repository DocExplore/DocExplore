/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.util.ImageUtils;

public class ManageCellRenderer implements ListCellRenderer
{
	MainWindow win;
	Color selectionColor;
	int targetRow;
	int targetDiff;
	
	public ManageCellRenderer(MainWindow win)
	{
		this.win = win;
		this.selectionColor = new Color(10, 36, 106);
		this.targetRow = -1;
	}
	
	Icon loading = ImageUtils.getIcon("page_search-24x24.png");
	Color odd = new Color(1f, .95f, .95f), even = new Color(.95f, .95f, 1f);
	Color oddDark = new Color(.75f, .45f, .45f), evenDark = new Color(.45f, .45f, .75f);
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus)
	{
		String display = null;
		try {display = ((Book)value).getMetaDataString(win.getDocExploreLink().displayKey);}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e, false);}
		JLabel label = new JLabel(((Book)value).getName(), display != null && display.equals("poster") ? ImageUtils.getIcon("scroll-48x48.png") : ImageUtils.getIcon("book-48x48.png"), SwingConstants.LEFT);
		
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
