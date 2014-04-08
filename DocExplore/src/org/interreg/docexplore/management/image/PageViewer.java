/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Page;
import org.interreg.docexplore.manuscript.Region;
import org.interreg.docexplore.util.GuiUtils;
import org.interreg.docexplore.util.ImageUtils;

public class PageViewer extends JPanel
{
	public static interface ImageOperation
	{
		public void pointClicked(PageViewer ic, Point point, int modifiers, int clickCount);
		public void pointHovered(PageViewer ic, Point point, int modifiers);
		public void pointGrabbed(PageViewer ic, Point point, int modifiers);
		public void pointDragged(PageViewer ic, Point point, int modifiers);
		public void pointDropped(PageViewer ic, Point point, int modifiers);
		
		public void render(PageViewer ic, Graphics2D g);
		public boolean completed();
		public String getMessage();
	}
	
	public static interface Listener
	{
		public void regionAdded(Page page, Point [] outline);
		public void regionRemoved(Region region);
		public void regionAnnotationRequested(Region region);
		public void objectSelected(AnnotatedObject object);
		public void analysisRequested(BufferedImage image);
		public void operationSet(ImageOperation operation);
		public void pageCropped(Page page, int tlx, int tly, int brx, int bry);
	}
	
	private static final long serialVersionUID = -8837147382083972319L;
	
	public final static SelectionOperation defaultOperation = new SelectionOperation();
	
	public AnnotatedObject document;
	public BufferedImage image;
	public RegionOverlay overlay;
	ImageOperation operation;
	public AffineTransform transform;
	boolean bufferIsDirty = true;
	
	@SuppressWarnings("serial")
	public PageViewer()
	{
		super(new BorderLayout());
		
		this.document = null;
		this.overlay = new RegionOverlay();
		this.image = null;
		this.operation = defaultOperation;
		this.transform = new AffineTransform();
		
		final Point dragStart = new Point();
		final boolean [] dragging = {false};
		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(final MouseEvent e)
			{
				requestFocus();
				
				if (e.getButton() == MouseEvent.BUTTON3 && document != null)
				{
					JPopupMenu popup = new JPopupMenu();
					popup.add(new JMenuItem(new AbstractAction(XMLResourceBundle.getBundledString("imageLocateLabel")) {
						public void actionPerformed(ActionEvent ev)
						{
							Point point = toImage(e.getPoint());
							try
							{
								final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), true);
								String code = BookViewer.encode(getPage(), point.x, point.y);
								JTextField field = new JTextField(code);
								field.setFont(Font.decode(null).deriveFont(24f).deriveFont(Font.BOLD));
								field.setEditable(false);
								field.setBackground(dialog.getContentPane().getBackground());
								field.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
								dialog.add(field);
								dialog.pack();
								GuiUtils.centerOnScreen(dialog);
								dialog.setVisible(true);
							}
							catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
						}
					}));
					popup.show(PageViewer.this, e.getX(), e.getY());
				}
				else if (e.getButton() == MouseEvent.BUTTON1)
				{
					Page page = getPage();
					if (page == null)
						return;
					
					operation.pointClicked(PageViewer.this, toImage(e.getPoint()), e.getModifiersEx(), e.getClickCount());
					if (operation.completed())
						operation = defaultOperation;
				
					repaint();
				}
			}
			public void mousePressed(MouseEvent e)
			{
				dragStart.setLocation(toImage(e.getPoint()));
			}
			public void mouseReleased(MouseEvent e)
			{
				if (dragging[0])
				{
					dragging[0] = false;
					if (operation != null)
						operation.pointDropped(PageViewer.this, toImage(e.getPoint()), e.getModifiersEx());
				}
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				operation.pointHovered(PageViewer.this, toImage(e.getPoint()), e.getModifiersEx());
				repaint();
			}
			public void mouseDragged(MouseEvent e)
			{
				if (!dragging[0])
				{
					dragging[0] = true;
					operation.pointGrabbed(PageViewer.this, dragStart, e.getModifiersEx());
				}
				operation.pointDragged(PageViewer.this, toImage(e.getPoint()), e.getModifiersEx());
				repaint();
			}
		});
		
		addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent arg0)
			{
				bufferIsDirty = true;
				repaint();
			}
		});
		
		setFocusable(true);
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
		getActionMap().put("ESC", new AbstractAction() {public void actionPerformed(ActionEvent e) {setOperation(defaultOperation);}});
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");
		getActionMap().put("DEL", new AbstractAction()
			{public void actionPerformed(ActionEvent e) {if (document instanceof Region) deleteRegion((Region)document);}});
	}
	
	List<Listener> listeners = new LinkedList<PageViewer.Listener>();
	public void addListener(Listener listener) {listeners.add(listener);}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	public void notifyRegionAdded(Page page, Point [] outline) {for (Listener listener : listeners) listener.regionAdded(page, outline);}
	public void notifyRegionRemoved(Region region) {for (Listener listener : listeners) listener.regionRemoved(region);}
	public void notifyObjectSelected(AnnotatedObject object) {for (Listener listener : listeners) listener.objectSelected(object);}
	public void notifyAnalysisRequested(BufferedImage image) {for (Listener listener : listeners) listener.analysisRequested(image);}
	public void notifyRegionAnnotationRequested(Region region) {for (Listener listener : listeners) listener.regionAnnotationRequested(region);}
	public void notifyOperationSet(ImageOperation operation) {for (Listener listener : listeners) listener.operationSet(operation);}
	public void notifyPageCropped(Page page, int tlx, int tly, int brx, int bry) {for (Listener listener : listeners) listener.pageCropped(page, tlx, tly, brx, bry);}
	
	public Point toImage(Point displayPoint)
	{
		Point point = new Point(displayPoint);
		try {transform.inverseTransform(displayPoint, point);}
		catch (Exception ex) {ErrorHandler.defaultHandler.submit(ex);}
		point.x = point.x < 0 ? 0 : point.x >= image.getWidth() ? image.getWidth()-1 : point.x;
		point.y = point.y < 0 ? 0 : point.y >= image.getHeight() ? image.getHeight()-1 : point.y;
		return point;
	}
	
	public Point toDisplayCoordinates(Point imagePoint)
	{
		return toDisplayCoordinates(imagePoint, new Point());
	}
	public Point toDisplayCoordinates(Point imagePoint, Point dest)
	{
		return (Point)transform.transform(imagePoint, dest);
	}
	
	public int displayDistance2(Point a, Point b)
	{
		Point at = toDisplayCoordinates(a);
		Point bt = toDisplayCoordinates(b);
		
		int dx = at.x-bt.x, dy = at.y-bt.y;
		return dx*dx+dy*dy;
	}
	
	public void setImageAtDisplay(Point imagePoint, Point displayPoint)
	{
		Point imageDisplayPoint = toDisplayCoordinates(imagePoint);
		JViewport viewport = (JViewport)getParent();
		Point ori = viewport.getViewPosition();
		Point newOri = new Point(ori.x-displayPoint.x+imageDisplayPoint.x, ori.y-displayPoint.y+imageDisplayPoint.y);
		Dimension extent = viewport.getExtentSize();
		newOri.x = newOri.x > getWidth()-extent.width ? getWidth()-extent.width : newOri.x;
		newOri.y = newOri.y > getHeight()-extent.height ? getHeight()-extent.height : newOri.y;
		newOri.y = newOri.y < 0 ? 0 : newOri.y;
		newOri.x = newOri.x < 0 ? 0 : newOri.x;
		newOri.y = newOri.y < 0 ? 0 : newOri.y;
		viewport.setViewPosition(newOri);
	}
	
	public void addRegion(Point [] outline)
	{
		try {notifyRegionAdded(getPage(), outline);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	public void deleteRegion(Region region)
	{
		try {notifyRegionRemoved(region); setDocument(getPage());}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}	
	}
	public void cropPage(int tlx, int tly, int brx, int bry)
	{
		try {notifyPageCropped(getPage(), tlx, tly, brx, bry);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
	}
	
	public Page getPage()
	{
		if (document instanceof Page)
			return (Page)document;
		if (document instanceof Region)
			return ((Region)document).getPage();
		return null;
	}
	
	public void reload() throws DataLinkException
	{
		try
		{
			Page page = document instanceof Page ? (Page)document :
				document instanceof Region ? ((Region)document).getPage() :
				null;
			image = page == null ? ImageUtils.getImageFromIcon(ImageUtils.getIcon("book-big.png")) : page.getImage().getImage();
			overlay.setDocument(page, document instanceof Region ? (Region)document : null);
			setOperation(null);
			bufferIsDirty = true;
		}
		catch (Exception e) {throw new DataLinkException(document.getLink().getLink(), e);}
	}
	public void setDocument(AnnotatedObject document) throws DataLinkException
	{
		try
		{
			if (this.document == document)
				return;
			this.document = document;
			
			Page page = document instanceof Page ? (Page)document :
				document instanceof Region ? ((Region)document).getPage() :
				null;
			
			image = page == null ? ImageUtils.getImageFromIcon(ImageUtils.getIcon("book-big.png")) : page.getImage().getImage();
			overlay.setDocument(page, document instanceof Region ? (Region)document : null);
			
			fit();
			//repaint();
			
			setOperation(null);
		}
		catch (Exception e) {throw new DataLinkException(document.getLink().getLink(), e);}
	}
	
	public void copyView(PageViewer viewer)
	{
		transform = (AffineTransform)viewer.transform.clone();
		int sw = (int)(image.getWidth()*transform.getScaleX()), sh = (int)(image.getHeight()*transform.getScaleY());
		this.setPreferredSize(new Dimension(sw, sh));
		this.setBounds(0, 0, sw, sh);
		bufferIsDirty = true;
	}
	
	public void fit()
	{
		int pw = getParent().getWidth()-10, ph = getParent().getHeight()-10;
		int iw = image.getWidth(), ih = image.getHeight();
		double pr = pw*1./ph, ir = iw*1./ih;
		double r = pr > ir ? ph*1./ih : pw*1./iw;
		setZoom(r);
	}
	
	public void setOperation(ImageOperation operation)
	{
		setCursor(Cursor.getDefaultCursor());
		if (document != null && operation != null)
			this.operation = operation;
		else this.operation = defaultOperation;
		notifyOperationSet(this.operation);	
		repaint();
	}
	public ImageOperation getOperation() {return operation;}
	
	/**
	 * Set the zoomFactor of the viewed picture
	 * @param zoomFactor
	 */
	public void applyZoomShift(double zoomFactor)
	{
		transform.concatenate(AffineTransform.getScaleInstance(zoomFactor, zoomFactor));
		int sw = (int)(image.getWidth()*transform.getScaleX()), sh = (int)(image.getHeight()*transform.getScaleY());
		this.setPreferredSize(new Dimension(sw, sh));
		this.setBounds(0, 0, sw, sh);
		bufferIsDirty = true;
		this.repaint();
	}
	public void setZoom(double zoomFactor)
	{
		transform = AffineTransform.getScaleInstance(zoomFactor, zoomFactor);
		int sw = (int)(image.getWidth()*transform.getScaleX()), sh = (int)(image.getHeight()*transform.getScaleY());
		this.setPreferredSize(new Dimension(sw, sh));
		this.setBounds(0, 0, sw, sh);
		bufferIsDirty = true;
		this.repaint();
	}
	
	
	/**
	 * Return the id of the picture which is displayed by the component
	 * @return keyPage 
	 */
	public AnnotatedObject getDocument() {return this.document;}
	
	public final static Color selectedRegionOutlineColor = new Color(255, 255, 64, 255);
	public final static Color exteriorMaskColor = new Color(192, 192, 192, 127);
	
	BufferedImage buffer = null;
	protected void paintComponent(Graphics _g)
	{
		//Graphics2D g = (Graphics2D)_g;
		boolean tooBig = false;
		if (buffer == null || buffer.getWidth() < getWidth() || buffer.getHeight() < getHeight())
		{
			int w = buffer == null || buffer.getWidth() < getWidth() ? getWidth() : buffer.getWidth();
			int h = buffer == null || buffer.getHeight() < getHeight() ? getHeight() : buffer.getHeight();
			tooBig = w > 4096 || h > 4096;
			if (!tooBig)
				buffer = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			//System.out.println(w+","+h+" "+tooBig);
		}
		
		if(image != null && !tooBig && bufferIsDirty)
		{
			Graphics2D g = buffer.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			super.paintComponent(g);
			g.transform(transform);
			g.drawImage(image, 0, 0, null);
			bufferIsDirty = false;
		}
		_g.drawImage(buffer, 0, 0, null);
		
		Graphics2D g = (Graphics2D)_g;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		AffineTransform old = g.getTransform();
		g.transform(transform);
		if(image != null)
		{
			if (tooBig)
				g.drawImage(image, 0, 0, null);
			overlay.render(g);
			operation.render(this, g);
		}
		g.setTransform(old);
	}
}