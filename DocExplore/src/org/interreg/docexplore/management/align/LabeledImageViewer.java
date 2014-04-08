/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.align;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.interreg.docexplore.management.align.LabeledImage.Label;
import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

@SuppressWarnings("serial")
public class LabeledImageViewer extends JPanel
{
	public static interface EditorListener
	{
		public void rendered(LabeledImageViewer editor, Graphics2D g);
		public void mouseMoved(LabeledImageViewer editor, double x, double y);
		public void mouseDragged(LabeledImageViewer editor, int button, double x, double y);
		public void mouseClicked(LabeledImageViewer editor, int button, int modifiers, double x, double y);
	}
	
	AnalyzedImage aimage;
	BufferedImage displayBuffer;
	JLabel canvas;
	LabeledImage.Label highlighted;
	List<EditorListener> editorListeners;
	boolean showLabels;
	
	public LabeledImageViewer()
	{
		super(new BorderLayout());
		
		this.aimage = null;
		this.displayBuffer = null;
		this.canvas = new JLabel() {public void paintComponent(Graphics g) {render((Graphics2D)g);}};
		this.showLabels = false;
		
		canvas.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (displayBuffer == null)
					return;
				double x = fromScreenX(e.getX());
				double y = fromScreenY(e.getY());
				for (EditorListener listener : editorListeners)
					listener.mouseClicked(LabeledImageViewer.this, e.getButton(), e.getModifiers(), x, y);
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				if (displayBuffer == null)
					return;
				
				double x = fromScreenX(e.getX());
				double y = fromScreenY(e.getY());
				for (EditorListener listener : editorListeners)
					listener.mouseMoved(LabeledImageViewer.this, x, y);
				
				if (showLabels && aimage.limage != null)
				{
					LabeledImage.Label hovered = labelAt(e.getX(), e.getY());
					if (highlighted == hovered)
						return;
					
					if (highlighted != null)
						resetLabelColor(highlighted);
					
					highlighted = hovered;
					if (highlighted != null)
						setLabelColor(highlighted, Color.yellow);
					
					repaint();
				}
				
				dragging = false;
			}
			
			boolean dragging = false;
			int dx, dy;
			public void mouseDragged(MouseEvent e)
			{
				if (displayBuffer == null)
					return;
				
				double x = fromScreenX(e.getX());
				double y = fromScreenY(e.getY());
				for (EditorListener listener : editorListeners)
					listener.mouseDragged(LabeledImageViewer.this, e.getModifiers(), x, y);
				
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) > 0)
				{
					if (dragging)
					{
						if ((e.getModifiers() & InputEvent.SHIFT_MASK) > 0)
							cz *= Math.exp(.01*(dy-e.getY()));
						else
						{
							cx -= (e.getX()-dx)/cz;
							cy -= (e.getY()-dy)/cz;
						}
						setupView();
					}
					dragging = true;
					dx = e.getX();
					dy = e.getY();
					repaint();
				}
			}
		});
		canvas.addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				cz *= Math.exp(.06*(-e.getWheelRotation()));
				setupView();
				repaint();
			}
		});
		
		add(canvas, BorderLayout.CENTER);
		
		this.editorListeners = new LinkedList<LabeledImageViewer.EditorListener>();
	}
	
	public void addEditorListener(EditorListener listener) {editorListeners.add(listener);}
	public void removeRenderListener(EditorListener listener) {editorListeners.remove(listener);}
	
	/**
	 * In component coordinates (different from image)
	 * @param x
	 * @param y
	 * @return
	 */
	public LabeledImage.Label labelAt(int x, int y)
	{
		if (displayBuffer == null)
			return null;
		
		double mx = x*1./canvas.getWidth()*displayBuffer.getWidth();
		double my = y*1./canvas.getHeight()*displayBuffer.getHeight();
		if (mx < 0 || mx >= displayBuffer.getWidth() || my < 0 || my >= displayBuffer.getHeight())
			return null;
		
		LabeledImage.Label hovered = aimage.limage.data[(int)mx][(int)my];
		if (hovered.val == 0)
			return null;
		
		return hovered;
	}
	
	public void resetLabelColor(LabeledImage.Label label) {setLabelColor(label,  aimage.limage.labelColor(label));}
	public void setLabelColor(LabeledImage.Label label, Color col)
	{
		if (displayBuffer == null)
			return;
		
		final int rgb = col.getRGB();
		aimage.limage.traverseLabel(label, new LabeledImage.LabelFunction()
		{
			public void doPixel(LabeledImage image, Label label, int comp, int x, int y) {displayBuffer.setRGB(x, y, rgb);}
			public void beforeStart(LabeledImage image, Label label) {}
			public void afterEnd(LabeledImage image, Label label) {}
		});
	}
	
	public void setImage(AnalyzedImage aimage)
	{
		this.aimage = aimage;
		this.cx = aimage.original.getWidth()/2;
		this.cy = aimage.original.getHeight()/2;
		this.cz = 1;
		
		displayBuffer = aimage.limage != null ? aimage.limage.toImage() : 
			aimage.bimage != null ? aimage.bimage.toImage() : aimage.original;
		highlighted = null;
		setupView();
		repaint();
	}
	
	double cx, cy, cz, sx, sy, il, it, ir, ib, sl, st, sr, sb;
	void setupView()
	{
		sx = canvas.getWidth()/2.; sy = canvas.getHeight()/2.;
		il = 0; it = 0; ir = displayBuffer.getWidth()-1; ib = displayBuffer.getHeight()-1;
		sl = sx-cz*cx; st = sy-cz*cy; 
		sr = sx+cz*(displayBuffer.getWidth()-cx-1); sb = sy+cz*(displayBuffer.getHeight()-cy-1);
		
		if (sl < 0) {il += -sl/cz; sl = 0;}
		if (st < 0) {it += -st/cz; st = 0;}
		if (sr > canvas.getWidth()-1) {ir -= (sr-canvas.getWidth()+1)/cz; sr = canvas.getWidth()-1;}
		if (sb > canvas.getHeight()-1) {ib -= (sb-canvas.getHeight()+1)/cz; sb = canvas.getHeight()-1;}
	}
	
	public double fromScreenX(int x) {return ((x-sl)/cz+il)/displayBuffer.getWidth();}
	public double fromScreenY(int y) {return ((y-st)/cz+it)/displayBuffer.getHeight();}
	public double fromScreenLength(int l) {return l/cz/displayBuffer.getWidth();}
	public double toScreenXLength(double x) {return cz*displayBuffer.getWidth()*x;}
	public double toScreenYLength(double y) {return cz*displayBuffer.getHeight()*y;}
	public double toScreenLength(double x, double y)
	{
		double lx = toScreenXLength(x), ly = toScreenYLength(y);
		return lx*lx+ly*ly;
	}
	
	private void render(final Graphics2D g)
	{
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		if (displayBuffer == null)
			return;
		
		g.drawImage(showLabels ? displayBuffer : aimage.original, 
			(int)sl, (int)st, (int)sr, (int)sb, 
			(int)il, (int)it, (int)ir, (int)ib, null);
		
		AffineTransform old = g.getTransform(), tmp = g.getTransform();
		tmp.scale(cz, cz);
		tmp.translate(sl/cz-il, st/cz-it);
		g.setTransform(tmp);
		for (EditorListener listener : editorListeners)
			listener.rendered(this, g);
		g.setTransform(old);
	}
	
	public static void main(String [] args) throws Exception
	{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			if ("Nimbus".equals(info.getName()))
				{UIManager.setLookAndFeel(info.getClassName()); break;}
		
		JFrame win = new JFrame("Alignement");
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		LabeledImageViewer lie = new LabeledImageViewer();
		EditorPanel panel = new EditorPanel(lie);
		panel.setImage(new AnalyzedImage(ImageUtils.read(new File("D:\\sci\\align\\roi.PNG")), false, false));
		TranscriptionPanel tp = new TranscriptionPanel(panel);
		tp.setTranscription(StringUtils.readFile(new File("D:\\sci\\align\\trans.txt"), "UTF-8"));
		
		win.setLayout(new BorderLayout());
		win.add(panel, BorderLayout.CENTER);
		win.add(tp, BorderLayout.NORTH);
		
		win.pack();
		win.setExtendedState(JFrame.MAXIMIZED_BOTH);
		win.setVisible(true);
	}
}
