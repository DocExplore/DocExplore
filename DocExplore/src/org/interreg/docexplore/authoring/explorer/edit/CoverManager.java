package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.imgscalr.Scalr;
import org.interreg.docexplore.authoring.explorer.BookView;
import org.interreg.docexplore.authoring.explorer.ExplorerView;
import org.interreg.docexplore.authoring.explorer.ViewItem.Data;
import org.interreg.docexplore.authoring.explorer.ViewMouseListener;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.util.FileImageSource;
import org.interreg.docexplore.util.ImageSource;
import org.interreg.docexplore.util.ImageUtils;

@SuppressWarnings("serial")
public class CoverManager extends JPanel
{
	Book book = null;
	
	public CoverManager()
	{
		super(new LooseGridLayout(2, 2, 5, 5, true, true, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		
		add(new ImagePanel(XMLResourceBundle.getBundledString("coverFront"), "frontCover"));
		add(new ImagePanel(XMLResourceBundle.getBundledString("coverBack"), "backCover"));
		add(new ImagePanel(XMLResourceBundle.getBundledString("coverFrontInner"), "frontInnerCover"));
		add(new ImagePanel(XMLResourceBundle.getBundledString("coverBackInner"), "backInnerCover"));
		
		setPreferredSize(new Dimension(800, 600));
		
		notifyBookChanged();
	}
	
	public void setBook(Book book)
	{
		this.book = book;
		notifyBookChanged();
	}
	
	static interface Listener
	{
		public void bookChanged();
	}
	List<Listener> listeners = new LinkedList<CoverManager.Listener>();
	void notifyBookChanged()
	{
		for (Listener listener : listeners)
			listener.bookChanged();
	}
	
	class ImagePanel extends JPanel implements ViewMouseListener.DropTarget
	{
		String keyName;
		boolean canDrop = false;
		JPanel canvas;
		
		ImagePanel(String name, final String keyName)
		{
			super(new BorderLayout());
			setBorder(BorderFactory.createTitledBorder(name));
			ViewMouseListener.makeFileSystemDropTarget(this);
			
			this.keyName = keyName;
			canvas = new JPanel() {public void paintComponent(Graphics g)
			{
				g.clearRect(0, 0, getWidth(), getHeight());
				if (book != null) try
				{
					MetaDataKey key = book.getLink().getOrCreateKey(keyName, "");
					List<MetaData> mds = book.getMetaDataListForKey(key);
					if (mds != null && mds.size() > 0)
					{
						MetaData md = mds.get(0);
						BufferedImage image = md.getImage();
						float ratio = image.getWidth() > getWidth() ? getWidth()*1f/image.getWidth() : 1;
						ratio = ratio*image.getHeight() > getHeight() ? getHeight()*1f/image.getHeight() : ratio;
						int w = (int)(ratio*image.getWidth());
						int h = (int)(ratio*image.getHeight());
						g.drawImage(image, (getWidth()-w)/2, (getHeight()-h)/2, w, h, null);
					}
				}
				catch (Exception e) {e.printStackTrace();}
				
				if (canDrop)
				{
					g.setColor(Color.red);
					int w = 3;
					g.fillRect(0, 0, getWidth(), w);
					g.fillRect(0, 0, w, getHeight());
					g.fillRect(getWidth()-w, 0, w, getHeight());
					g.fillRect(0, getHeight()-w, getWidth(), w);
				}
			}};
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			final JButton discard = new JButton(new AbstractAction("Discard") {public void actionPerformed(ActionEvent e)
			{
				try
				{
					MetaDataKey key = book.getLink().getOrCreateKey(keyName, "");
					List<MetaData> mds = book.getMetaDataListForKey(key);
					if (mds != null)
						for (MetaData md : mds)
							book.removeMetaData(md);
				}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
				
				canvas.repaint();
			}});
//			buttonPanel.add(browse);
			buttonPanel.add(discard);
			add(buttonPanel, BorderLayout.NORTH);
			
			add(canvas, BorderLayout.CENTER);
			
			listeners.add(new Listener() {public void bookChanged()
			{
				discard.setEnabled(book != null);
				canvas.repaint();
			}});
		}
		
		private void setImage(ImageSource source)
		{
			try
			{
				MetaDataKey key = book.getLink().getOrCreateKey(keyName, "");
				List<MetaData> mds = book.getMetaDataListForKey(key);
				if (mds != null)
					for (MetaData md : mds)
						book.removeMetaData(md);
				MetaData md = new MetaData(book.getLink(), key, MetaData.imageType, source.getFile());
				book.addMetaData(md);
			}
			catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}

		public void dropped(ExplorerView source, List<Data> items, Point where)
		{
			canDrop = false;
			for (Data data : items)
				if (data.object instanceof Page)
				{
					try {setImage(((Page)data.object).getImage());}
					catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
					break;
				}
				else if (data.object instanceof File && ImageUtils.isSupported(((File)data.object).getName()))
				{
					setImage(new FileImageSource((File)data.object));
					break;
				}
			canvas.repaint();
		}
		public void dragged(ExplorerView source, List<Data> items, Point where)
		{
			boolean canDrop = source == null || source instanceof BookView;
			if (canDrop != this.canDrop)
			{
				this.canDrop = canDrop;
				canvas.repaint();
			}
		}
		public void exited()
		{
			if (canDrop)
			{
				canDrop = false;
				canvas.repaint();
			}
		}
	}
	
	static BufferedImage getImage(Book book, String keyName) throws Exception
	{
		MetaDataKey key = book.getLink().getOrCreateKey(keyName, "");
		List<MetaData> mds = book.getMetaDataListForKey(key);
		if (mds == null || mds.isEmpty())
			return null;
		MetaData md = mds.get(0);
		return md.getImage();
	}
	public static BufferedImage buildPreviewCoverImage(Book book)
	{
		try
		{
			BufferedImage front = getImage(book, "frontCover");
			BufferedImage back = front == null ? getImage(book, "backCover") : null;
			if (front == null && back == null)
				return null;
			if (front == null)
			{
				BufferedImage rev = new BufferedImage(
					(front != null ? front.getWidth() : back.getWidth()), 
					(front != null ? front.getHeight() : back.getHeight()), 
					BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = rev.createGraphics();
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				g.setBackground(new Color(0));
				g.clearRect(0, 0, rev.getWidth(), rev.getHeight());
				g.drawImage(back, 0, 0, back.getWidth(), back.getHeight(), back.getWidth(), 0, 0, back.getHeight(), null);
				front = rev;
			}
			
			return Scalr.resize(front, 512);
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	public static BufferedImage buildCoverImage(Book book)
	{
		try
		{
			BufferedImage front = getImage(book, "frontCover");
			BufferedImage back = getImage(book, "backCover");
			if (front == null && back == null)
				return null;
			BufferedImage res = new BufferedImage(
				(front != null ? front.getWidth() : back.getWidth())+(back != null ? back.getWidth() : front.getWidth()), 
				Math.max((front != null ? front.getHeight() : back.getHeight()), (back != null ? back.getHeight() : front.getHeight())), 
				BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = res.createGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g.setBackground(new Color(0));
			g.clearRect(0, 0, res.getWidth(), res.getHeight());
			if (front != null)
				g.drawImage(front, res.getWidth()-front.getWidth(), 0, front.getWidth(), front.getHeight(), null);
			else g.drawImage(back, res.getWidth()-back.getWidth(), 0, res.getWidth(), res.getHeight(), back.getWidth(), 0, 0, back.getHeight(), null);
			if (back != null)
				g.drawImage(back, 0, 0, back.getWidth(), back.getHeight(), null);
			else g.drawImage(front, 0, 0, front.getWidth(), front.getHeight(), front.getWidth(), 0, 0, front.getHeight(), null);
			
			return res;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	public static BufferedImage buildInnerCoverImage(Book book)
	{
		try
		{
			BufferedImage front = getImage(book, "frontInnerCover");
			BufferedImage back = getImage(book, "backInnerCover");
			if (front == null && back == null)
				return null;
			BufferedImage res = new BufferedImage(
				(front != null ? front.getWidth() : back.getWidth())+(back != null ? back.getWidth() : front.getWidth()), 
				Math.max((front != null ? front.getHeight() : back.getHeight()), (back != null ? back.getHeight() : front.getHeight())), 
				BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = res.createGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g.setBackground(new Color(0));
			g.clearRect(0, 0, res.getWidth(), res.getHeight());
			if (back != null)
				g.drawImage(back, res.getWidth()-back.getWidth(), 0, back.getWidth(), back.getHeight(), null);
			else g.drawImage(front, res.getWidth()-front.getWidth(), 0, res.getWidth(), res.getHeight(), front.getWidth(), 0, 0, front.getHeight(), null);
			if (front != null)
				g.drawImage(front, 0, 0, front.getWidth(), front.getHeight(), null);
			else g.drawImage(back, 0, 0, back.getWidth(), back.getHeight(), back.getWidth(), 0, 0, back.getHeight(), null);
			
			return res;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
}
