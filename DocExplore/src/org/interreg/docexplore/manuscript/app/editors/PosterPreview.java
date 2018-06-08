package org.interreg.docexplore.manuscript.app.editors;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.BorderFactory;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.image.ImageView;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.TileConfiguration;
import org.interreg.docexplore.manuscript.app.DocumentEditorHost;

@SuppressWarnings("serial")
public class PosterPreview extends ImageView
{
	final DocumentEditorHost host;
	public final Book book;
	
	public PosterPreview(DocumentEditorHost host, Book book)
	{
		this.book = book;
		this.host = host;
		
		setBorder(BorderFactory.createLineBorder(GuiConstants.borderColor));
		
		addMouseListener(new MouseAdapter() {@Override public void mouseReleased(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
				try {host.onDocumentEditorRequest(book.getPage(1));}
				catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		}});
	}
	
	public void refresh(Container parent)
	{
		int pw = parent != null ? Math.min(3*parent.getWidth()/4, parent.getHeight()/3) : 0;
		if (pw == 0)
			return;
		try
		{
			if (book.getLastPageNumber() == 0)
			{
				setImage(null);
				return;
			}
			List<MetaData> configs = book.getMetaDataListForKey(host.getAppHost().getLink().tileConfigKey);
			TileConfiguration config = configs.size() > 0 ? (TileConfiguration)new ObjectInputStream(configs.get(0).getValue()).readObject() : null;
			if (config != null)
			{
				BufferedImage image = host.getAppHost().getLink().getMetaData(config.getTileId(config.getLastLayer(), 0, 0)).getImage();
				setImage(image);
			}
			else setImage(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR));
			setPreferredSize(new Dimension(pw, pw));
			revalidate();
			repaint();
		}
		catch (Exception e)
		{
			setImage(null); 
			ErrorHandler.defaultHandler.submit(e, false);
		}
	}
}
