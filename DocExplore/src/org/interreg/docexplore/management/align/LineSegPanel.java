package org.interreg.docexplore.management.align;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.interreg.docexplore.util.ImageUtils;
import org.interreg.docexplore.util.StringUtils;


@SuppressWarnings("serial")
public class LineSegPanel extends JPanel
{
	JLabel canvas;
	JScrollPane scrollPane;
	JTextArea transArea;
	AnalyzedImage image;
	int [] lines;
	
	public LineSegPanel()
	{
		super(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		this.canvas = new JLabel() {public void paintComponent(Graphics g) {paintCanvas((Graphics2D)g);}};
		this.scrollPane = new JScrollPane(canvas, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		
		this.transArea = new JTextArea(6, 128);
		add(transArea, BorderLayout.SOUTH);
		
		setImage(null, null);
	}
	
	void paintCanvas(Graphics2D g)
	{
		if (image == null)
			return;
		
		g.drawImage(image.original, 0, 0, null);
		g.setColor(Color.red);
		for (int i=0;i<lines.length;i++)
			g.drawLine(0, lines[i], image.original.getWidth(), lines[i]);
	}
	
	public void setImage(BufferedImage image, String transcription)
	{
		this.image = image == null ? null : new AnalyzedImage(image);
		transArea.setText(transcription == null ? "" : transcription);
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
		float [] scores = new float [image.original.getHeight()];
		for (int i=0;i<scores.length;i++)
			scores[i] = image.bimage.lineScore(i)*1f/image.original.getHeight();
		scores = PixelUtils.blur(scores, 10);
		for (int i=0;i<nLines;i++)
			lines[i] = PixelUtils.moveToLowestScore(i*image.original.getHeight()/nLines, scores, 5);
		
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
		
		BufferedImage image = ImageUtils.read(new File("C:\\sci\\align\\roi.PNG"));
		String transcription = StringUtils.readFile(new File("C:\\sci\\align\\trans.txt"), "UTF-8");
		System.out.println(transcription);
		lsp.setImage(image, transcription);
		lsp.detectLines(4);
	}
}
