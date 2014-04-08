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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconButton;
import org.interreg.docexplore.gui.IconToggleButton;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.gui.DocumentPanel;
import org.interreg.docexplore.management.gui.MainWindow;
import org.interreg.docexplore.management.manage.ManageComponent;
import org.interreg.docexplore.management.manage.SelectPagesPanel;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

public class BookViewer extends JPanel
{
	private static final long serialVersionUID = -3584515946932650576L;
	
	MainWindow win;
	Book book;
	JPanel pagePanel;
	
	Point dragPos;
	PageLabel dragSource = null;
	Icon dragIcon = null;
	PageLabel lastSelected = null;
	int lastMovedIndex = -2;
	
	boolean isLocked;
	JLabel titleLabel;
	
	IconButton addPagesButton;
	
	@SuppressWarnings("serial")
	public BookViewer(final MainWindow win)
	{
		super(new BorderLayout());
		
		this.win = win;
		this.book = null;
		WrapLayout layout = new WrapLayout();
		layout.setHgap(1);
		this.pagePanel = new JPanel(layout);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pagePanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		add(pagePanel, BorderLayout.CENTER);
		pagePanel.setBackground(Color.white);
		isLocked = true;
		
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			if (book == null)
				return;
			if (isLocked)
			{
				JOptionPane.showMessageDialog(BookViewer.this, XMLResourceBundle.getBundledString("imageDelUnlockMessage"));
				return;
			}
			
			List<Page> pages = getSelectedPages();
			win.manageComponent.handler.pagesDeleted(pages);
//			if (win.manageComponent.handler.pagesDeleted(pages))
//				try {setDocument(book);}
//				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
	}
	
	public List<Page> getSelectedPages()
	{
		Vector<Page> pages = new Vector<Page>();
		for (Component component : pagePanel.getComponents())
			if (((PageLabel)component).selected)
				pages.add(((PageLabel)component).page);
		return pages;
	}
	
	public void setDocument(AnnotatedObject document) throws DataLinkException
	{
		if (book != document)
		{
			this.book = (Book)document;
			
			WrapLayout topLayout = new WrapLayout();
			topLayout.setHgap(20);
			JPanel topPanel = new JPanel(topLayout);
			titleLabel = new JLabel("", ImageUtils.getIcon("book-48x48.png"), SwingConstants.LEFT);
			topPanel.add(titleLabel);
			
			isLocked = true;
			final Icon unlocked = ImageUtils.getIcon("unlocked-32x32.png");
			final Icon locked = ImageUtils.getIcon("locked-32x32.png");
			final IconToggleButton lockedButton = new IconToggleButton(locked);
			addPagesButton = new IconButton("add-24x24.png", XMLResourceBundle.getBundledString("manageAppendPagesLabel"));
			addPagesButton.setEnabled(false);
			
			lockedButton.setSelected(true);
			lockedButton.setToolTipText(XMLResourceBundle.getBundledString("imageLockTooltip"));
			lockedButton.setFocusable(false);
			lockedButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
			{
				if (lockedButton.isSelected())
				{
					lockedButton.setIcon(locked);
					addPagesButton.setEnabled(false);
				}
				else
				{
					if (JOptionPane.showConfirmDialog(BookViewer.this, XMLResourceBundle.getBundledString("imageUnlockMessage"), 
						XMLResourceBundle.getBundledString("imageLockTooltip"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					{
						lockedButton.setIcon(unlocked);
						addPagesButton.setEnabled(true);
					}
					else
					{
						lockedButton.setSelected(true);
						lockedButton.setIcon(locked);
						addPagesButton.setEnabled(false);
					}
				}
				isLocked = lockedButton.isSelected();
			}});
			topPanel.add(lockedButton);
			
			addPagesButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					ManageComponent manageComp = win.manageComponent;
					List<File> files = SelectPagesPanel.show();
					if (files == null)
						return;
					manageComp.handler.appendPages(book, files);
					
					try {setDocument(book);}
					catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				}
			});
			addPagesButton.setEnabled(false);
			topPanel.add(addPagesButton);
			
			JPanel gotoPanel = new JPanel(new FlowLayout());
			gotoPanel.add(new JLabel(XMLResourceBundle.getBundledString("imageGotoLabel")));
			final JTextField goField = new JTextField(7);
			goField.addKeyListener(new KeyAdapter()
			{
				public void keyReleased(KeyEvent event)
				{
					if (event.getKeyCode() == KeyEvent.VK_ENTER)
						try {goTo(goField.getText());}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
				}
			});
			gotoPanel.add(goField);
			topPanel.add(gotoPanel);
			
			add(topPanel, BorderLayout.NORTH);
		}
		
		titleLabel.setText("<html><big>"+book.getName()+"</big><br/>"+book.getLastPageNumber()+" pages</html>");
		
		pagePanel.removeAll();
		int nPages = book.getLastPageNumber();
		for (int i=1;i<=nPages;i++)
			pagePanel.add(new PageLabel(this, book.getPage(i)));
		
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
	
	public void goTo(String s) throws Exception
	{
		s = s.toLowerCase();
		if (s.length() < 7)
			win.addTab(book.getPage(Integer.parseInt(s)));
		else
		{
			Pair<Page, Point> pair = decode(book, s);
			DocumentPanel panel = win.addTab(pair.first);
			panel.pageViewer.setOperation(new BeaconOperation(pair.second, 5000, 500));
			panel.pageViewer.repaint();
		}
	}
	
	public static String encode(Page page, int x, int y) throws DataLinkException
	{
		String pageNum = Integer.toString(page.getPageNumber(), 36);
		while (pageNum.length() < 3)
			pageNum = '0'+pageNum;
		
		Dimension dim = DocExploreDataLink.getImageDimension(page);
		double xc = x*1./dim.width;
		double yc = y*1./dim.height;
		String xs = Integer.toString((int)(xc*(36*36-1)), 36);
		while (xs.length() < 2)
			xs = '0'+xs; 
		String ys = Integer.toString((int)(yc*(36*36-1)), 36);
		while (ys.length() < 2)
			ys = '0'+ys;
		return (pageNum+xs+ys);
	}
	
	public static Pair<Page, Point> decode(Book book, String s) throws Exception
	{
		int pageNum = Integer.parseInt(s.substring(0, 3), 36);
		Page page = book.getPage(pageNum);
		Dimension dim = DocExploreDataLink.getImageDimension(page);
		int x = (int)(dim.width*Integer.parseInt(s.substring(3, 5), 36)*1./(36*36-1));
		int y = (int)(dim.height*Integer.parseInt(s.substring(5, 7), 36)*1./(36*36-1));
		return new Pair<Page, Point>(page, new Point(x, y));
	}
}
