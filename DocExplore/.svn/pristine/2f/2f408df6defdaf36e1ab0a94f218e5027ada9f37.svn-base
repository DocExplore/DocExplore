package org.interreg.docexplore.management.process.align;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


@SuppressWarnings("serial")
public class LineSegPanel extends JPanel
{
	JLabel canvas;
	JScrollPane scrollPane;
	BufferedImage image;
	int [] lines;
	
	public LineSegPanel()
	{
		super(new BorderLayout());
		
		this.canvas = new JLabel() {public void paintComponent(Graphics g) {paintCanvas((Graphics2D)g);}};
		this.scrollPane = new JScrollPane(canvas, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		setImage(null);
		
		add(scrollPane, BorderLayout.CENTER);
	}
	
	void paintCanvas(Graphics2D g)
	{
		if (image == null)
			return;
		
		g.drawImage(image, 0, 0, null);
		g.setColor(Color.red);
		for (int i=0;i<lines.length;i++)
			g.drawLine(0, lines[i], image.getWidth(), lines[i]);
	}
	
	public void setImage(BufferedImage image)
	{
		this.image = image;
		if (image != null)
			canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		else canvas.setPreferredSize(new Dimension(0, 0));
		lines = new int [0];
		
		canvas.invalidate();
		scrollPane.validate();
		canvas.repaint();
	}
	
	public void detectLines(int nLines)
	{
		if (image == null)
			return;
		
		lines = new int [nLines];
		int [][] grad = ImageUtils.buildGrad(image);
		float [] scores = new float [grad[0].length];
		for (int i=0;i<scores.length;i++)
			scores[i] = ImageUtils.lineScore(grad, i);
		scores = ImageUtils.blur(scores, 10);
		for (int i=0;i<nLines;i++)
			lines[i] = ImageUtils.moveToLowestScore(i*image.getHeight()/nLines, scores, 5);
		
		canvas.repaint();
	}
	
	public static void main(String [] args) throws Exception
	{
		JFrame win = new JFrame("AlignIt");
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		LineSegPanel lsp = new LineSegPanel();
		win.add(lsp);
		win.pack();
		win.setExtendedState(JFrame.MAXIMIZED_BOTH);
		win.setVisible(true);
		
		BufferedImage image = ImageIO.read(new File("C:\\sci\\align\\roi.PNG"));
		lsp.setImage(image);
		lsp.detectLines(4);
	}
}
