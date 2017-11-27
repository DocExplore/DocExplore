package org.interreg.docexplore.authoring;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.management.image.PosterUtils;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.MemoryImageSource;
import org.interreg.docexplore.util.history.ReversibleAction;

@SuppressWarnings("serial")
public class NameDialog extends JDialog
{
	AuthoringToolFrame win;
	JTextField title;
	JTextArea desc;
	JRadioButton isBookBox, isPosterBox;
	JLabel displayLocked;
	boolean okayed = false;
	
	public NameDialog(AuthoringToolFrame win)
	{
		super((Frame)null, XMLResourceBundle.getBundledString("generalTitle"), true);
		
		this.win = win;
		
		JPanel top = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		setContentPane(top);
		top.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		top.add(new JLabel(XMLResourceBundle.getBundledString("collectionAddBookMessage")));
		top.add(title = new JTextField());
		title.setPreferredSize(new Dimension(320, title.getPreferredSize().height));
		top.add(new JLabel(" "));
		
		top.add(new JLabel(XMLResourceBundle.getBundledString("generalDescription")));
		top.add(desc = new JTextArea());
		desc.setPreferredSize(new Dimension(title.getPreferredSize().width, 64));
		desc.setFont(title.getFont());
		desc.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		top.add(new JLabel(" "));
		
		JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bookPanel.add(isBookBox = new JRadioButton(XMLResourceBundle.getBundledString("collectionBookDisplayLabel")));
		bookPanel.add(new JLabel(ImageUtils.getIcon("book-64x64.png")));
		top.add(bookPanel);
		JPanel posterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		posterPanel.add(isPosterBox = new JRadioButton(XMLResourceBundle.getBundledString("collectionPosterDisplayLabel")));
		posterPanel.add(new JLabel(ImageUtils.getIcon("scroll-64x64.png")));
		top.add(posterPanel);
		ButtonGroup group = new ButtonGroup();
		group.add(isBookBox);
		group.add(isPosterBox);
		top.add(displayLocked = new JLabel(""));
		top.add(new JLabel(" "));
		top.add(new JLabel(" "));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			okayed = true;
			setVisible(false);
		}}));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			okayed = false;
			setVisible(false);
		}}));
		top.add(buttons);
		
		pack();
	}
	
	public boolean show(final DocExploreDataLink link, final Book book)
	{
		okayed = false;
		title.setText(book.getName());
		try
		{
			MetaDataKey key = book.getLink().getOrCreateKey("Description");
			List<MetaData> mds = book.getMetaDataListForKey(key);
			if (mds.size() > 0)
				desc.setText(mds.get(0).getString());
			
			boolean isPoster = PosterUtils.isPoster(book);
			boolean isEmpty = !isPoster && book.getLastPageNumber() == 0 || isPoster && book.getMetaDataListForKey(link.partKey).isEmpty();
			isBookBox.setSelected(!isPoster);
			isPosterBox.setSelected(isPoster);
			isBookBox.setEnabled(isEmpty);
			isPosterBox.setEnabled(isEmpty);
			displayLocked.setText(isEmpty ? "" : XMLResourceBundle.getBundledString("collectionBookDisplayLockedMsg"));
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e); return false;}
		setVisible(true);
		
		if (okayed)
		{
			try
			{
				book.setName(title.getText());
				MetaDataKey key = book.getLink().getOrCreateKey("Description");
				List<MetaData> mds = book.getMetaDataListForKey(key);
				MetaData descMd = null;
				if (mds.size() > 0)
					mds.get(0).setString(desc.getText());
				else
				{
					descMd = new MetaData(book.getLink(), key, desc.getText());
					book.addMetaData(descMd);
				}
				
				boolean isPoster = PosterUtils.isPoster(book);
				boolean toPoster = isPosterBox.isSelected();
				if (isPoster != toPoster) try
				{
					if (toPoster)
						win.historyManager.submit(new ReversibleAction()
						{
							@Override public void doAction() throws Exception {toPoster(link, book);}
							@Override public void undoAction() throws Exception {toBook(link, book);}
							@Override public String description() {return XMLResourceBundle.getBundledString("collectionToPosterDisplayLabel");}
						});
					else 
						win.historyManager.submit(new ReversibleAction()
						{
							@Override public void doAction() throws Exception {toBook(link, book);}
							@Override public void undoAction() throws Exception {toPoster(link, book);}
							@Override public String description() {return XMLResourceBundle.getBundledString("collectionToBookDisplayLabel");}
						});
				}
				catch (Throwable e) {ErrorHandler.defaultHandler.submit(e);}
				//book.setMetaDataString(link.displayKey, isBookBox.isSelected() ? "book" : "poster");
			}
			catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		}
		
		return okayed;
	}
	
	private void toPoster(DocExploreDataLink link, Book book) throws Exception
	{
		book.setMetaDataString(link.displayKey, "poster");
		book.appendPage(new MemoryImageSource(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR)));
		win.editor.explore(win.editor.curPath);
	}
	private void toBook(DocExploreDataLink link, Book book) throws Exception
	{
		book.setMetaDataString(link.displayKey, "book");
		book.removePage(1);
		win.editor.explore(win.editor.curPath);
	}
}
