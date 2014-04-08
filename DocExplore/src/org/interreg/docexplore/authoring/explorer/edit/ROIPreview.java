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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.interreg.docexplore.gui.ErrorHandler;
import org.interreg.docexplore.gui.LooseGridLayout;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
import org.interreg.docexplore.util.Pair;

@SuppressWarnings("serial")
public class ROIPreview extends JDialog
{
	static Color back = new Color(0, 0, 0, 255);
	JScrollPane scrollPane;
	JPanel ratioPanel;
	int width, height;
	List<Pair<JButton, int []>> ratioButtons = new Vector<Pair<JButton,int []>>();
	
	BufferedImage image = null;
	int x0, y0, r;
	JLabel canvas;
	
	public ROIPreview(int width)
	{
		//super(new LooseGridLayout(0, 1, 0, 5, true, false, SwingConstants.CENTER, SwingConstants.TOP, true, false));
		super(JOptionPane.getRootFrame(), XMLResourceBundle.getBundledString("previewTitle"), false);
		
		setAlwaysOnTop(true);
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().setBackground(back);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setResizable(false);
		
		this.width = width;
		
		scrollPane = new JScrollPane(new JPanel(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollPane, BorderLayout.EAST);
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		
		JPanel topPanel = new JPanel(new LooseGridLayout(1, 0, 0, 0, true, false, SwingConstants.CENTER, SwingConstants.CENTER, true, true));
		topPanel.setOpaque(true);
		topPanel.setBackground(Color.gray);
		ratioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ratioPanel.setOpaque(false);
		ratioPanel.add(new JLabel("<html><font color=\"#ffffff\">"+XMLResourceBundle.getBundledString("previewRatio")+":</font></html>"));
		ratioPanel.add(buildRatioButton(16, 9).first);
		ratioPanel.add(buildRatioButton(16, 10).first);
		ratioPanel.add(buildRatioButton(4, 3).first);
		ratioPanel.add(buildRatioButton(1, 1).first);
		topPanel.add(ratioPanel, BorderLayout.WEST);
		JPanel warningPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		warningPanel.setOpaque(false);
		warningPanel.add(new JLabel("<html><font color=\"#ff8080\"><b>"+XMLResourceBundle.getBundledString("previewWarning")+"</b></font></html>"));
		topPanel.add(warningPanel, BorderLayout.EAST);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
		canvas = new JLabel() {
			Rectangle2D.Double rect = new Rectangle2D.Double();
			public void paintComponent(Graphics g)
			{
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (image == null)
					return;
				
				rect.setRect(x0-r, y0-r*getHeight()/getWidth(), 2*r, 2*r*getHeight()/getWidth());
				int sx = rect.x < 0 ? 0 : (int)rect.x, sy = rect.y < 0 ? 0 : (int)rect.y;
				int sw = rect.x+rect.width >= image.getWidth() ? image.getWidth()-1 : (int)rect.width,
					sh = rect.y+rect.height >= image.getHeight() ? image.getHeight()-1 : (int)rect.height;
				int dx = rect.x < 0 ? (int)(-rect.x/rect.width*getWidth()) : 0, 
					dy = rect.y < 0 ? (int)(-rect.y/rect.height*getHeight()) : 0;
				int dw = (int)(sw*getWidth()/rect.width), dh = (int)(sh*getHeight()/rect.height);
				g.drawImage(image, dx, dy, dx+dw, dy+dh, sx, sy, sx+sw, sy+sh, null);
			}};
		getContentPane().add(canvas, BorderLayout.WEST);
		
		setRatio(16, 9);
		
		pack();
	}
	
	private void updateRatioButtons(int num, int den)
	{
		for (Pair<JButton, int []> button : ratioButtons)
			if (button.second[0] == num && button.second[1] == den)
				button.first.setText("<html><font color=\"#ffffff\"><b>"+num+"/"+den+"</b></font></html>");
			else button.first.setText("<html><font color=\"#ffffff\">"+button.second[0]+"/"+button.second[1]+"</font></html>");
	}
	private Pair<JButton, int []> buildRatioButton(final int num, final int den)
	{
		JButton button = new JButton(new AbstractAction("<html><font color=\"#ffffff\">"+num+"/"+den+"</font></html>")
			{public void actionPerformed(ActionEvent arg0) {setRatio(num, den);}
		}) {{setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setBackground(Color.black);}};
		Pair<JButton, int []> res = new Pair<JButton, int []>(button, new int [] {num, den});
		ratioButtons.add(res);
		return res;
	}
	private void setRatio(int num, int den)
	{
		this.height = den*2*width/num;
		updateRatioButtons(num, den);
		refresh();
	}
	private void refresh()
	{
		scrollPane.getViewport().setPreferredSize(new Dimension(width, height));
		canvas.setPreferredSize(new Dimension(width, height));
		setPreferredSize(new Dimension(2*width+getInsets().left+getInsets().right, 
			height+getInsets().top+getInsets().bottom+ratioPanel.getHeight()));
		pack();
	}
	
	public void set(BufferedImage page, Point [] region, List<InfoElement> elements, float [] progress)
	{
		progress[0] = 0;
		
		Polygon shape = new Polygon();
		for (Point p : region)
			shape.addPoint(p.x, p.y);
		
		Rectangle bounds = shape.getBounds();
		this.x0 = bounds.x+bounds.width/2;
		this.y0 = bounds.y+bounds.height/2;
		this.r = Math.max(bounds.width, bounds.height)/2;
		this.image = new BufferedImage(page.getWidth(), page.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		Area mask = new Area(new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight()));
		mask.subtract(new Area(shape));
		Graphics2D g = image.createGraphics();
		g.drawImage(page, 0, 0, null);
		g.setColor(new Color(0, 0, 0, 127));
		g.fill(mask);
		
		JPanel content = new JPanel(new LooseGridLayout(0, 1, 0, 5, true, false, SwingConstants.CENTER, SwingConstants.TOP, true, false));
		content.setBackground(back);
		int cnt = 0;
		for (InfoElement element : elements) try
		{
			BufferedImage image = element.getPreview(width, back);
			JLabel label = new JLabel(new ImageIcon(image));
			label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
			label.getInsets().set(0, 0, 0, 0);
			label.setBorder(null);
			content.add(label);
			cnt++;
			progress[0] = cnt*1f/elements.size();
		}
		/*else
		{
			JLabel label = new JLabel("<html><center><b>Plugin: "+element.md.getType().toUpperCase()+"</b><br/><i>No preview available</i></center></html>");
			label.setForeground(Color.white);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setFont(Font.decode("Arial-24"));
			label.setBorder(BorderFactory.createLineBorder(Color.white, 1));
			label.setPreferredSize(new Dimension(width, width/2));
			content.add(label);
		}*/
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e, true); e.printStackTrace();}
		scrollPane.setViewportView(content);
		
		refresh();
	}
}
