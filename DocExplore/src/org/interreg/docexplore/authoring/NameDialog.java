package org.interreg.docexplore.authoring;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;

@SuppressWarnings("serial")
public class NameDialog extends JDialog
{
	JTextField title;
	JTextArea desc;
	boolean okayed = false;
	
	public NameDialog()
	{
		super((Frame)null, XMLResourceBundle.getBundledString("generalTitle"), true);
		
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
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			okayed = false;
			setVisible(false);
		}}));
		buttons.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel")) {@Override public void actionPerformed(ActionEvent e)
		{
			okayed = true;
			setVisible(false);
		}}));
		top.add(buttons);
		
		pack();
	}
	
	public boolean show(Book book)
	{
		okayed = false;
		title.setText(book.getName());
		try
		{
			MetaDataKey key = book.getLink().getOrCreateKey("Description");
			List<MetaData> mds = book.getMetaDataListForKey(key);
			if (mds.size() > 0)
				desc.setText(mds.get(0).getString());
		}
		catch (Exception e) {e.printStackTrace();}
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
			}
			catch (Exception e) {e.printStackTrace();}
		}
		
		return okayed;
	}
}
