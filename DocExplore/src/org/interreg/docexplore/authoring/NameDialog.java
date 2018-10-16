/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.PosterUtils;
import org.interreg.docexplore.manuscript.app.ManuscriptAppHost;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.MemoryImageSource;
import org.interreg.docexplore.util.history.ReversibleAction;

@SuppressWarnings("serial")
public class NameDialog extends JDialog
{
	ManuscriptAppHost host;
	JTextField title;
	JTextArea desc;
	JRadioButton isBookBox, isPosterBox;
	JLabel displayLocked;
	boolean okayed = false;
	
	public NameDialog(ManuscriptAppHost host)
	{
		super((Frame)null, Lang.s("generalTitle"), true);
		
		this.host = host;
		
		JPanel top = new JPanel(new LooseGridLayout(0, 1, 5, 5, true, false, SwingConstants.LEFT, SwingConstants.TOP, false, false));
		setContentPane(top);
		top.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		top.add(new JLabel(Lang.s("collectionAddBookMessage")));
		top.add(title = new JTextField());
		title.setPreferredSize(new Dimension(320, title.getPreferredSize().height));
		top.add(new JLabel(" "));
		
		top.add(new JLabel(Lang.s("generalDescription")));
		top.add(desc = new JTextArea());
		desc.setPreferredSize(new Dimension(title.getPreferredSize().width, 64));
		desc.setFont(title.getFont());
		desc.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		top.add(new JLabel(" "));
		
		JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bookPanel.add(new JLabel(ImageUtils.getIcon("book-64x64.png")));
		bookPanel.add(isBookBox = new JRadioButton(Lang.s("collectionBookDisplayLabel")));
		top.add(bookPanel);
		JPanel posterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		posterPanel.add(new JLabel(ImageUtils.getIcon("scroll-64x64.png")));
		posterPanel.add(isPosterBox = new JRadioButton(Lang.s("collectionPosterDisplayLabel")));
		top.add(posterPanel);
		ButtonGroup group = new ButtonGroup();
		group.add(isBookBox);
		group.add(isPosterBox);
		top.add(displayLocked = new JLabel(""));
		top.add(new JLabel(" "));
		top.add(new JLabel(" "));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(Lang.s("generalOkLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			okayed = true;
			setVisible(false);
		}}));
		buttons.add(new JButton(new AbstractAction(Lang.s("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			okayed = false;
			setVisible(false);
		}}));
		top.add(buttons);
		
		pack();
	}
	
	public boolean showDialog()
	{
		try {return showDialog(host.getLink(), host.getLink().getBook(host.getLink().getLink().getAllBookIds().get(0)));}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return false;
	}
	
	public boolean showDialog(final DocExploreDataLink link, final Book book)
	{
		okayed = false;
		title.setText(book.getName());
		try
		{
			MetaDataKey key = book.getLink().getOrCreateKey("Description");
			List<MetaData> mds = book.getMetaDataListForKey(key);
			if (mds.size() > 0)
				desc.setText(mds.get(0).getString());
			else desc.setText("");
			
			boolean isPoster = PosterUtils.isPoster(book);
			boolean isEmpty = !isPoster && book.getLastPageNumber() == 0 || isPoster && book.getMetaDataListForKey(link.partKey).isEmpty();
			isBookBox.setSelected(!isPoster);
			isPosterBox.setSelected(isPoster);
			isBookBox.setEnabled(isEmpty);
			isPosterBox.setEnabled(isEmpty);
			displayLocked.setText(isEmpty ? "" : Lang.s("collectionBookDisplayLockedMsg"));
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
						host.historyManager.submit(new ReversibleAction()
						{
							@Override public void doAction() throws Exception {toPoster(link, book);}
							@Override public void undoAction() throws Exception {toBook(link, book);}
							@Override public String description() {return Lang.s("collectionToPosterDisplayLabel");}
						});
					else 
						host.historyManager.submit(new ReversibleAction()
						{
							@Override public void doAction() throws Exception {toBook(link, book);}
							@Override public void undoAction() throws Exception {toPoster(link, book);}
							@Override public String description() {return Lang.s("collectionToBookDisplayLabel");}
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
		host.setLink(host.getLink().getLink());
		host.notifyActiveDocumentChanged();
	}
	private void toBook(DocExploreDataLink link, Book book) throws Exception
	{
		book.setMetaDataString(link.displayKey, "book");
		book.removePage(1);
		host.setLink(host.getLink().getLink());
		host.notifyActiveDocumentChanged();
	}
}
