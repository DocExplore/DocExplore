/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.management.manage.SelectPagesPanel;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Page;

public class BookPagesEditor extends JPanel implements BookEditor.ConfigurationEditor
{
	private static final long serialVersionUID = -3584515946932650576L;
	
	BookEditor bookEditor;
	JPanel pagePanel;
	
	Point dragPos;
	PageLabel dragSource = null;
	Icon dragIcon = null;
	PageLabel lastSelected = null;
	int lastMovedIndex = -2;
	
	public BookPagesEditor(final BookEditor bookEditor) throws DataLinkException
	{
		super(new BorderLayout());
		
		this.bookEditor = bookEditor;
		WrapLayout layout = new WrapLayout();
		layout.setHgap(1);
		this.pagePanel = new JPanel(layout);
		//pagePanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		pagePanel.setBackground(Color.white);
		
		JScrollPane scrollPane = new JScrollPane(pagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
		
		refresh();
	}
	
	public List<Page> getSelectedPages()
	{
		Vector<Page> pages = new Vector<Page>();
		for (Component component : pagePanel.getComponents())
			if (((PageLabel)component).selected)
				pages.add(((PageLabel)component).page);
		return pages;
	}
	
	public void refresh()
	{
		try
		{
			pagePanel.removeAll();
			int nPages = bookEditor.book.getLastPageNumber();
			for (int i=1;i<=nPages;i++)
				pagePanel.add(new PageLabel(this, bookEditor.book.getPage(i)));
		}
		catch (DataLinkException e) {ErrorHandler.defaultHandler.submit(e);}
		
		new Thread() {public void run()
		{
			try
			{
				int cnt = 0;
				for(Component comp : pagePanel.getComponents())
				{
					PageLabel label = (PageLabel)comp;
					Page page = label.page;
					label.mini.setIcon(new ImageIcon(DocExploreDataLink.getImageMini(page)));
					
					cnt++;
					if (cnt%10 == 0)
						{validate(); repaint();}
				}
				validate();
				repaint();
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}}.start();
		
		validate();
		repaint();
	}
	
	@Override public void onActionRequest(String action) throws Exception
	{
		
	}
	
	Color none = new Color(0, 0, 0, 0);
	protected void paintChildren(Graphics g)
	{
		super.paintChildren(g);
		
		if (dragSource == null)
			{dragIcon = null; return;}
		if (dragIcon == null)
		{
			Icon icon = dragSource.mini.getIcon();
			BufferedImage di = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D gdi = di.createGraphics();
			gdi.setBackground(none);
			gdi.clearRect(0, 0, di.getWidth(), di.getHeight());
			icon.paintIcon(this, gdi, 0, 0);
			for (int i=0;i<di.getWidth();i++)
				for (int j=0;j<di.getHeight();j++)
			{
				int argb = di.getRGB(i, j);
				di.setRGB(i, j, ((((argb >> 24) & 0xff)/2) << 24)+(argb & 0x00ffffff));
			}
			dragIcon = new ImageIcon(di);
		}
		
		int x = dragPos.x-dragIcon.getIconWidth()/2, y = dragPos.y-dragIcon.getIconHeight()/2;
		dragIcon.paintIcon(this, g, x, y);
	}
	
	@Override public void onDeletePagesRequest()
	{
		List<Page> pages = getSelectedPages();
		bookEditor.host.getActionListener().onDeletePagesRequest(pages);
	}

	@Override public void onAddPagesRequest()
	{
		List<File> files = SelectPagesPanel.show();
		if (files == null)
			return;
		bookEditor.host.getActionListener().onAppendPagesRequest(bookEditor.book, files);
	}

	@Override public Component getComponent() {return this;}
	
	boolean readOnly = false;
	@Override public void setReadOnly(boolean b) {readOnly = b;}
}
