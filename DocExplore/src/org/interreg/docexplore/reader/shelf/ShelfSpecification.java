/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.reader.shelf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import org.interreg.docexplore.reader.book.roi.SwingRenderer;
import org.interreg.docexplore.reader.gui.GuiLayer;
import org.interreg.docexplore.util.ImageUtils;

public class ShelfSpecification
{
	public static class Entry
	{
		public String title;
		public String desc;
		public String src;
		public BufferedImage cover;
		
		public Entry(String title, String desc, String src, BufferedImage cover)
		{
			this.title = title;
			this.desc = desc;
			this.src = src;
			this.cover = cover;
		}
		
		static Color background = new Color(0, 0, 0, 0);
		static Color lineCol = new Color(.2f, .3f, 1, 1f);
		static Color fillCol = new Color(.2f, .3f, 1, .25f);
		static SwingRenderer renderer = null;
		static BufferedImage defaultCover = null;
		public int renderTo(BufferedImage image)
		{
			try
			{
				if (defaultCover == null)
					defaultCover = ImageUtils.read(Thread.currentThread().getContextClassLoader().getResource(ShelfSpecification.class.getPackage().getName().replace('.', '/')+"/frontCover2.png"));
				BufferedImage cover = this.cover == null ? Entry.defaultCover : this.cover;
				String html = "<xhtml>" +
					"<font style=\"" +
					"font-family:Arial;" +
					"font-weight:bold;" +
					"color:rgb(50, 75, 255);" +
					//"margin:"+(int)(image.getWidth()/16)+"px;" +
					//"text-align:middle
					"\"><center><br/>" +
					"<div style=\"font-size:"+(int)(image.getWidth()/12)+"px\">"+title+"</div><br/>" +
					"<div style=\"font-size:"+(int)(image.getWidth()/18)+"px\">"+desc+"</div>" +
					"</center></font></xhtml>";
//				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
//					new ByteArrayInputStream((html).getBytes("UTF-8")));
				
				Graphics2D g = image.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				float maxw = image.getWidth(), maxh = image.getWidth();
				float aspect = 1;
				if (cover.getWidth() > maxw)
					aspect = maxw/cover.getWidth();
				if (aspect*cover.getHeight() > maxh)
					aspect = maxh/cover.getHeight();
				int bw = (int)(aspect*cover.getWidth());
				int bh = (int)(aspect*cover.getHeight());
				
				g.setBackground(background);
				g.clearRect(0, 0, image.getWidth(), bh);
				g.drawImage(cover, (image.getWidth()-bw)/2, 0, (image.getWidth()+bw)/2, bh, 0, 0, cover.getWidth(), cover.getHeight(), null);
				
				if (renderer == null)
					renderer = new SwingRenderer();
				int h = renderer.render(html, image, background, bh);
				
				g.setStroke(GuiLayer.defaultStroke);
				g.setColor(fillCol);
				g.fillRoundRect(2, bh+2, image.getWidth()-4, h-4, 20, 20);
				g.setColor(lineCol);
				g.drawRoundRect(2, bh+2, image.getWidth()-4, h-4, 20, 20);
				return bh+h;
			}
			catch (Exception e) {e.printStackTrace();}
			return -1;
		}
	}
	
	public List<Entry> entries;
	
	public ShelfSpecification()
	{
		this.entries = new Vector<ShelfSpecification.Entry>();
	}
	
	public void addEntry(String title, String desc, String src, BufferedImage cover)
	{
		entries.add(new Entry(title, desc, src, cover));
	}
}
