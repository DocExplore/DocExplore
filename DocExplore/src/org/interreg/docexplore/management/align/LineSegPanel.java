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
