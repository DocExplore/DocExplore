/**
Copyright LITIS/EDA 2018
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.DocExploreDataLink;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.MetaDataKey;

public class CoverManager
{
	Book book = null;
	
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
	
	
	
	static BufferedImage getImage(Book book, MetaDataKey key) throws Exception {return getImage(book, key, null);}
	static BufferedImage getImage(Book book, MetaDataKey key, MetaDataKey backup) throws Exception
	{
		List<MetaData> mds = book.getMetaDataListForKey(key);
		if (mds == null || mds.isEmpty())
			return backup != null ? getImage(book, backup) : null;
		MetaData md = mds.get(0);
		return md.getImage();
	}
	public static BufferedImage buildPreviewCoverImage(DocExploreDataLink link, Book book)
	{
		try
		{
			BufferedImage front = getImage(book, link.frontCoverTrans, link.frontCover);
			BufferedImage back = front == null ? getImage(book, link.backCoverTrans, link.backCover) : null;
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
	public static BufferedImage buildCoverImage(DocExploreDataLink link, Book book, boolean trans, boolean inner)
	{
		try
		{
			BufferedImage front = inner ? 
				(trans ? getImage(book, link.frontInnerCoverTrans, link.frontInnerCover) : getImage(book, link.frontInnerCover)) : 
				(trans ? getImage(book, link.frontCoverTrans, link.frontCover) : getImage(book, link.frontCover));
			BufferedImage back = inner ? 
				(trans ? getImage(book, link.backInnerCoverTrans, link.backInnerCover) : getImage(book, link.backInnerCover)) : 
				(trans ? getImage(book, link.backCoverTrans, link.backCover) : getImage(book, link.backCover));
			if (front == null && back == null)
			{
				BufferedImage image = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/interreg/docexplore/reader/book/default"+(inner ? "Inner" : "")+"Cover"+(trans ? "" : "NoTrans")+".png"));
				if (!trans)
				{
					BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
					tmp.createGraphics().drawImage(image, null, 0, 0);
					image = tmp;
				}
				return image;
			}
			BufferedImage res = new BufferedImage(
				(front != null ? front.getWidth() : back.getWidth())+(back != null ? back.getWidth() : front.getWidth()), 
				Math.max((front != null ? front.getHeight() : back.getHeight()), (back != null ? back.getHeight() : front.getHeight())), 
				trans ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = res.createGraphics();
			if (trans)
			{
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				g.setBackground(new Color(0));
				g.clearRect(0, 0, res.getWidth(), res.getHeight());
			}
			if (inner)
			{
				if (back != null) g.drawImage(back, res.getWidth()-back.getWidth(), 0, back.getWidth(), back.getHeight(), null);
				else g.drawImage(front, res.getWidth()-front.getWidth(), 0, res.getWidth(), res.getHeight(), front.getWidth(), 0, 0, front.getHeight(), null);
				if (front != null) g.drawImage(front, 0, 0, front.getWidth(), front.getHeight(), null);
				else g.drawImage(back, 0, 0, back.getWidth(), back.getHeight(), back.getWidth(), 0, 0, back.getHeight(), null);
			}
			else
			{
				if (front != null) g.drawImage(front, res.getWidth()-front.getWidth(), 0, front.getWidth(), front.getHeight(), null);
				else g.drawImage(back, res.getWidth()-back.getWidth(), 0, res.getWidth(), res.getHeight(), back.getWidth(), 0, 0, back.getHeight(), null);
				if (back != null) g.drawImage(back, 0, 0, back.getWidth(), back.getHeight(), null);
				else g.drawImage(front, 0, 0, front.getWidth(), front.getHeight(), front.getWidth(), 0, 0, front.getHeight(), null);
			}
			return res;
		}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return null;
	}
	public static BufferedImage buildCoverImage(DocExploreDataLink link, Book book, boolean trans) {return buildCoverImage(link, book, trans, false);}
	public static BufferedImage buildInnerCoverImage(DocExploreDataLink link, Book book, boolean trans) {return buildCoverImage(link, book, trans, true);}
}