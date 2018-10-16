/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
