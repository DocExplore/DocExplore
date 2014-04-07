package org.interreg.docexplore.reader.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;

public class PageResizer
{
	static class Resize
	{
		int w, h;
		int x1, y1, x2, y2;
		
		BufferedImage resize(BufferedImage image)
		{
			BufferedImage res = new BufferedImage(x2-x1, y2-y1, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = res.createGraphics();
			g.drawImage(image, 0, 0, res.getWidth(), res.getHeight(), x1, y1, x2, y2, null);
			return res;
		}
		
		double [] convert(double x, double y)
		{
			x *= w;
			y *= h;
			x -= x1;
			y -= y1;
			x /= (x2-x1);
			y /= (y2-y1);
			x = x > 1 ? 1 : x < 0 ? 0 : x;
			y = y > 1 ? 1 : y < 0 ? 0 : y;
			return new double [] {x, y};
		}
	}
	
	public PageResizer(File bookFile) throws Exception
	{
		File bookDir = new File(bookFile.getParentFile(), bookFile.getName().substring(0, bookFile.getName().length()-4));
		System.out.println(bookDir.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new StringReader(StringUtils.readFile(bookFile, "UTF-8")));
		StringBuffer sb = new StringBuffer();
		Resize curResize = null;
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			if (line.trim().startsWith("<Page"))
			{
				int start = line.indexOf("src=\"")+5;
				int end = line.indexOf("\"", start);
				File imageFile = new File(bookDir, line.substring(start, end));
				curResize = useImage(imageFile);
			}
			else if (line.trim().startsWith("</Page"))
				curResize = null;
			else if (line.trim().startsWith("<RegionOfInterest") && curResize != null)
			{
				int start = line.indexOf("region=\"")+8;
				int end = line.indexOf("\"", start);
				String [] coords = line.substring(start, end).split(",");
				double [] newVals = new double [coords.length];
				for (int i=0;i<coords.length;i+=2)
				{
					double [] newCoords = curResize.convert(Double.parseDouble(coords[i].trim()), Double.parseDouble(coords[i+1].trim()));
					newVals[i] = newCoords[0];
					newVals[i+1] = newCoords[1];
				}
				String newLine = line.substring(0, start);
				for (int i=0;i<newVals.length;i++)
					newLine += (i > 0 ? ", " : "")+newVals[i];
				newLine += line.substring(end);
				line = newLine;
			}
			
			System.out.println(line);
			sb.append(line).append("\n");
		}
		reader.close();
		
		StringUtils.writeFile(bookFile, sb.toString(), "UTF-8");
	}
	
	@SuppressWarnings("serial")
	Resize useImage(final File imageFile) throws Exception
	{
		final BufferedImage image = ImageUtils.read(imageFile);
		
		final JDialog win = new JDialog((Frame)null, "Use Image?", true);
		win.setUndecorated(true);
		win.setLayout(new BorderLayout());
		
		final Resize [] res = {new Resize()};
		res[0].w = image.getWidth();
		res[0].h = image.getHeight();
		
		final JLabel label = new JLabel(new ImageIcon(image)) {public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.setColor(Color.red);
			g.drawRect(res[0].x1, res[0].y1, res[0].x2-res[0].x1, res[0].y2-res[0].y1);
		}};
		label.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e) {res[0].x2 = e.getX(); res[0].y2 = e.getY(); label.repaint();}
			public void mousePressed(MouseEvent e) {res[0].x1 = e.getX(); res[0].y1 = e.getY(); label.repaint();}
		});
		label.addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent e) {res[0].x2 = e.getX(); res[0].y2 = e.getY(); label.repaint();}
		});
		
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.add(new JButton(new AbstractAction("Yes") {public void actionPerformed(ActionEvent e)
		{
			BufferedImage resized = res[0].resize(image);
			try {ImageUtils.write(resized, "PNG", imageFile);}
			catch (Exception ex) {ex.printStackTrace();}
			win.setVisible(false);
		}}));
		buttonPanel.add(new JButton(new AbstractAction("No") {public void actionPerformed(ActionEvent e)
		{
			res[0] = null;
			win.setVisible(false);
		}}));
		
		win.add(label, BorderLayout.CENTER);
		win.add(buttonPanel, BorderLayout.WEST);
		win.pack();
		win.setVisible(true);
		
		return res[0];
	}
	
	public static void main(String [] args) throws Exception
	{
		File bookFile = new File("C:\\Users\\Bigpav\\Desktop\\Demo ARL\\server-resources\\book1.xml");
		new PageResizer(bookFile);
	}
}
