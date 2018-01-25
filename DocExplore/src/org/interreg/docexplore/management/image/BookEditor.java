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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.IconToggleButton;
import org.interreg.docexplore.gui.WrapLayout;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.management.gui.DocumentEditor;
import org.interreg.docexplore.management.gui.DocumentEditorHost;
import org.interreg.docexplore.management.gui.DocumentPanel;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.Pair;

public class BookEditor extends JPanel implements DocumentEditor
{
	private static final long serialVersionUID = -3584515946932650576L;
	
	static interface ConfigurationEditor
	{
		public Component getComponent();
		public void onDeletePagesRequest();
		public void onAddPagesRequest();
		public void onActionRequest(String action) throws Exception;
		public void refresh();
		public void setReadOnly(boolean b);
	}
	
	DocumentEditorHost host;
	ConfigurationEditor configurationEditor;
	Book book;
	
	boolean isLocked;
	JLabel titleLabel;
	
	@SuppressWarnings("serial")
	public BookEditor(final DocumentEditorHost host, final Book book) throws DataLinkException
	{
		super(new BorderLayout());
		
		this.host = host;
		this.book = book;
		isLocked = true;
		
		if (PosterUtils.isPoster(book))
			this.configurationEditor = new PosterPartsEditor(host, book);
		else this.configurationEditor = new BookPagesEditor(this);
		add(configurationEditor.getComponent(), BorderLayout.CENTER);
		configurationEditor.setReadOnly(isLocked);
		
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction() {public void actionPerformed(ActionEvent e)
		{
			if (isLocked) JOptionPane.showMessageDialog(BookEditor.this, Lang.s("imageDelUnlockMessage"));
			else configurationEditor.onDeletePagesRequest();
		}});
		
		WrapLayout topLayout = new WrapLayout();
		topLayout.setHgap(20);
		JPanel topPanel = new JPanel(topLayout);
		boolean isPoster = PosterUtils.isPoster(book);
		int nPages = book.getLastPageNumber();
		titleLabel = new JLabel("<html><big>"+book.getName()+"</big>"+(isPoster ? "" : "<br/>"+nPages+" page"+(nPages == 1 ? "" : "s"))+"</html>", 
			ImageUtils.getIcon(isPoster ? "scroll-64x64.png" : "book-64x64.png"), SwingConstants.LEFT);
		topPanel.add(titleLabel);
		
		final Icon unlocked = ImageUtils.getIcon("unlocked-32x32.png");
		final Icon locked = ImageUtils.getIcon("locked-32x32.png");
		final IconToggleButton lockedButton = new IconToggleButton(locked);
		lockedButton.setSelected(true);
		lockedButton.setToolTipText(Lang.s("imageLockTooltip"));
		lockedButton.setFocusable(false);
		lockedButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e)
		{
			if (lockedButton.isSelected())
				lockedButton.setIcon(locked);
			else
			{
				if (JOptionPane.showConfirmDialog(BookEditor.this, Lang.s("imageUnlockMessage"), 
					Lang.s("imageLockTooltip"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						lockedButton.setIcon(unlocked);
				else
				{
					lockedButton.setSelected(true);
					lockedButton.setIcon(locked);
				}
			}
			isLocked = lockedButton.isSelected();
			configurationEditor.setReadOnly(isLocked);
		}});
		topPanel.add(lockedButton);
		
		JPanel gotoPanel = new JPanel(new FlowLayout());
		gotoPanel.add(new JLabel(Lang.s("imageGotoLabel")));
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
	
	@Override public void refresh()
	{
		configurationEditor.refresh();
	}
	
	@Override public void goTo(String s) throws Exception
	{
		s = s.toLowerCase();
		if (s.length() < 7)
			host.onDocumentEditorRequest(book.getPage(Integer.parseInt(s)));
		else
		{
			Pair<Page, Point> pair = decode(book, s);
			DocumentPanel panel = host.onDocumentEditorRequest(pair.first);
			panel.goTo(s);
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
	
	@Override public Component getComponent() {return this;}
	@Override public void onActionRequest(String action) throws Exception
	{
		if (action.equals("add-pages"))
			if (isLocked) JOptionPane.showMessageDialog(BookEditor.this, Lang.s("imageDelUnlockMessage"));
			else configurationEditor.onAddPagesRequest();
		else if (action.equals("remove-pages"))
			if (isLocked) JOptionPane.showMessageDialog(BookEditor.this, Lang.s("imageDelUnlockMessage"));
			else configurationEditor.onDeletePagesRequest();
		else configurationEditor.onActionRequest(action);
	}
	@Override public void onActionStateRequest(String action, boolean state) throws Exception {}
	@Override public void onClose()
	{
		book.unloadMetaData();
	}
}
