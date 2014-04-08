/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System and  Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.interreg.docexplore.reader.book.roi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.context.AWTFontResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.Java2DFontContext;
import org.xhtmlrenderer.swing.Java2DOutputDevice;
import org.xhtmlrenderer.swing.Java2DTextRenderer;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.Configuration;

/**
 * <p>Renders an XML files, formatted with CSS, as an image. Input is a document in the form of file or URL,
 * and output is a BufferedImage. A Java2DRenderer is not intended to be re-used for multiple document
 * sources; just create new Java2DRenderers for each one you need. Java2DRenderer is not thread-safe.
 * Standard usage pattern is</p>
 *
 * <pre>
 * File xhtml = ...
 * Java2DRenderer rend = new Java2DRenderer(xhtml);
 * BufferedImage image = rend.getImage();
 * </pre>
 *
 * <p>The document is not loaded, and layout and render don't take place, until {@link #getImage(int)}  is called.
 * Subsequent calls to {@link #getImage()} don't result in a reload; create a new Java2DRenderer instance to do so.</p>
 *
 * <p>As with {@link org.xhtmlrenderer.swing.RootPanel}, you can access the
 * {@link org.xhtmlrenderer.layout.SharedContext} instance that will be used by this renderer and change settings
 * to control the rendering process; use {@link #getSharedContext()}.</p>
 *
 * <p>By default, this renderer will render to an RGB image which does not support transparency. To use another type
 * of BufferedImage, either set the image type using {@link #setBufferedImageType(int)} before calling
 * {@link #getImage()}, or else override the {@link #createBufferedImage(int, int)} to have full control over
 * the image we render to.</p>
 *
 * <p>Not thread-safe.</p>
 *
 * @see ITextRenderer
 */
public class Java2DRenderer {
	private static final int DEFAULT_HEIGHT = 1000;
	private static final int DEFAULT_DOTS_PER_POINT = 1;
	private static final int DEFAULT_DOTS_PER_PIXEL = 1;
	
	private SharedContext sharedContext;
	private Box root;
	private float dotsPerPoint;
	private String sourceDocumentBase;
	
	 /**
     * Creates a new instance pointing to the given Document. Does not render until {@link #getImage(int)} is called for
     * the first time.
     *
     * @param doc The document to be rendered.
     * @param width Target width, in pixels, for the image; required to provide horizontal bounds for the layout.
     * @param height Target height, in pixels, for the image.
     */
    public Java2DRenderer() {
        init(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_PIXEL);
    }

	/**
	 * Returns the SharedContext to be used by renderer. Is instantiated along with the class, so can be accessed
	 * before {@link #getImage()} is called to tune the rendering process.
	 *
	 * @return the SharedContext instance that will be used by this renderer
	 */
	public SharedContext getSharedContext() {
		return sharedContext;
	}

	/**
	 * Renders the XML document if necessary and returns the resulting image. If already rendered, same image
	 * reference will be returned.
	 *
	 * {@link #getImage(int)} with the target width.
	 * 
	 * @return The XML rendered as a BufferedImage.
	 */
	BufferedImage contextImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	public BufferedImage getImage(String text, int width, Color background)
	{
		try
		{
			text = text.replace("<html>", "").replace("</html>", "").replaceAll("\n", "<br/> ");
			text = "<html>"+text+"<br/> </html>";
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
			setDocument(doc, sourceDocumentBase, new XhtmlNamespaceHandler());
		    layout(doc, width, contextImage.createGraphics());
			
		    BufferedImage res = new BufferedImage(width, root.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = res.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setBackground(background);
			g.clearRect(0, 0, res.getWidth(), res.getHeight());
		    paint(g);
	
			return res;
		}
		catch (Exception e) {e.printStackTrace();}
		return null;
	}
	public int render(Document doc, BufferedImage res, Color background)
		{return render(doc, res, res.getWidth(), background);}
	public int render(Document doc, BufferedImage res, int width, Color background)
	{
	    setDocument(doc, sourceDocumentBase, new XhtmlNamespaceHandler());
	    layout(doc, width, contextImage.createGraphics());
		
	    Graphics2D g = res.createGraphics();
		g.setBackground(background);
		g.clearRect(0, 0, Math.min(width, res.getWidth()), Math.min(root.getHeight(), res.getHeight()));
	    return paint(g);
	}
	private int paint(Graphics2D g)
	{
		Java2DOutputDevice outputDevice = new Java2DOutputDevice(g);
		RenderingContext rc = sharedContext.newRenderingContextInstance();
		rc.setFontContext(new Java2DFontContext(g));
		rc.setOutputDevice(outputDevice);
		sharedContext.getTextRenderer().setup(rc.getFontContext());
		root.getLayer().paint(rc);
		return root.getHeight();
	}

	private void setDocument(Document doc, String url, NamespaceHandler nsh) {
		sharedContext.reset();
		if (Configuration.isTrue("xr.cache.stylesheets", true)) {
			sharedContext.getCss().flushStyleSheets();
		} else {
			sharedContext.getCss().flushAllStyleSheets();
		}
		sharedContext.setBaseURL(url);
		sharedContext.setNamespaceHandler(nsh);
		sharedContext.getCss().setDocumentContext(
				sharedContext,
				sharedContext.getNamespaceHandler(),
				doc,
				new NullUserInterface()
		);
	}

	private void layout(Document doc, int width, Graphics2D g) {
		Rectangle rect = new Rectangle(0, 0, width, DEFAULT_HEIGHT);
		sharedContext.set_TempCanvas(rect);
		LayoutContext c = newLayoutContext(g);
		BlockBox root = BoxBuilder.createRootBox(c, doc);
		root.setContainingBlock(new ViewportBox(rect));
		root.layout(c);
		this.root = root;
	}

	private LayoutContext newLayoutContext(Graphics2D g) {
		LayoutContext result = sharedContext.newLayoutContextInstance();
		result.setFontContext(new Java2DFontContext(g));

		sharedContext.getTextRenderer().setup(result.getFontContext());

		return result;
	}

	private void init(float dotsPerPoint, int dotsPerPixel) {
		this.dotsPerPoint = dotsPerPoint;

		UserAgentCallback userAgent = new NaiveUserAgent();
		sharedContext = new SharedContext(userAgent);

		AWTFontResolver fontResolver = new AWTFontResolver();
		sharedContext.setFontResolver(fontResolver);

		SwingReplacedElementFactory replacedElementFactory = new SwingReplacedElementFactory();
		sharedContext.setReplacedElementFactory(replacedElementFactory);

		sharedContext.setTextRenderer(new Java2DTextRenderer());
		sharedContext.setDPI(72 * this.dotsPerPoint);
		sharedContext.setDotsPerPixel(dotsPerPixel);
		sharedContext.setPrint(false);
		sharedContext.setInteractive(false);
	}

	private static final class NullUserInterface implements UserInterface {

		public boolean isHover(Element e) {
			return false;
		}

		public boolean isActive(Element e) {
			return false;
		}

		public boolean isFocus(Element e) {
			return false;
		}
	}
}