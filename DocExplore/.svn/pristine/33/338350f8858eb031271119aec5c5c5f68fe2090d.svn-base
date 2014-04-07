package org.interreg.docexplore.management.align;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.management.DocExploreDataLink;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.StringUtils;

public class RegionCopy
{
	static class BookEntry
	{
		int bookId;
		String name;
		
		BookEntry(int bookId, String name) {this.bookId = bookId; this.name = name;}
		public String toString() {return name;}
	}
	
	@SuppressWarnings("serial")
	public static void regionCopy(final DocExploreDataLink link) throws Exception
	{
		final JDialog dialog = new JDialog((Frame)null, "Copy regions", true);
		dialog.setLayout(new BorderLayout());
		
		List<Integer> bookIds = link.getLink().getAllBookIds();
		Vector<BookEntry> entries = new Vector<BookEntry>();
		for (Integer bookId : bookIds)
			entries.add(new BookEntry(bookId, StringUtils.escapeSpecialChars(link.getLink().getBookTitle(bookId))));
			
		final JComboBox fromBox = new JComboBox(entries);
		final JComboBox toBox = new JComboBox(entries);
		//final JCheckBox scaleBox = new JCheckBox("Scale to new size", true);
		
		JPanel bookPanel = new JPanel(new LooseGridLayout(0, 2, 10, 10, SwingConstants.LEFT, SwingConstants.CENTER));
		bookPanel.add(new JLabel("From"));
		bookPanel.add(fromBox);
		bookPanel.add(new JLabel("To"));
		bookPanel.add(toBox);
		//bookPanel.add(scaleBox);
		dialog.add(bookPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalOkLabel"))
		{
			public void actionPerformed(ActionEvent arg0)
			{
				final JDialog pdialog = new JDialog((Frame)null, true);
				pdialog.setUndecorated(true);
				pdialog.add(new JLabel("Copying..."), BorderLayout.NORTH);
				final JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
				pdialog.add(progress, BorderLayout.SOUTH);
				pdialog.pack();
				GuiUtils.centerOnScreen(pdialog);
				
				new Thread()
				{
					public void run()
					{
						try
						{
							int fromBookId = ((BookEntry)fromBox.getSelectedItem()).bookId;
							int toBookId = ((BookEntry)toBox.getSelectedItem()).bookId;
							Book fromBook = link.getBook(fromBookId);
							Book toBook = link.getBook(toBookId);
							
							int lastPage = Math.min(fromBook.getLastPageNumber(), toBook.getLastPageNumber());
							for (int pageNum=1;pageNum<=lastPage;pageNum++)
							{
								progress.setValue(pageNum*100/lastPage);
								
								Page fromPage = fromBook.getPage(pageNum);
								Page toPage = toBook.getPage(pageNum);
								
								BufferedImage image = fromPage.getImage().getImage();
								int fw = image.getWidth(), fh = image.getHeight();
								fromPage.unloadImage();
								
								image = toPage.getImage().getImage();
								int tw = image.getWidth(), th = image.getHeight();
								toPage.unloadImage();
								
								for (Region fromRegion : fromPage.getRegions())
								{
									Point [] fromOutline = fromRegion.getOutline();
									Region toRegion = toPage.addRegion();
									Point [] toOutline = new Point [fromOutline.length];
									for (int i=0;i<fromOutline.length;i++)
										toOutline[i] = new Point((fromOutline[i].x*tw)/fw, (fromOutline[i].y*th)/fh);
									toRegion.setOutline(toOutline);
									
									for (Map.Entry<MetaDataKey, List<MetaData>> metaDataEntry : fromRegion.getMetaData().entrySet())
										for (MetaData metaData : metaDataEntry.getValue())
											toRegion.addMetaData(metaData);
								}
							}
							
							dialog.setVisible(false);
						}
						catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
						while (!pdialog.isVisible())
							try {Thread.sleep(100);}
							catch (Exception e) {}
						pdialog.setVisible(false);
					}
				}.start();
				
				pdialog.setVisible(true);
			}
		}));
		buttonPanel.add(new JButton(new AbstractAction(XMLResourceBundle.getBundledString("generalCancelLabel"))
		{
			public void actionPerformed(ActionEvent arg0) {dialog.setVisible(false);}
		}));
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		GuiUtils.centerOnScreen(dialog);
		
		dialog.setVisible(true);
	}
}
