package org.interreg.docexplore.management.align;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JLabel;

public class AlignmentDetector
{
	public static void detect(TranscriptionPanel tp)
	{
		int nLines = tp.linesModel.getSize();
		int nChars = 0;
		for (int i=0;i<nLines;i++)
			nChars = Math.max(nChars, tp.linesModel.get(i).toString().length());
		BinaryImage bimage = tp.panel.editor.aimage.bimage;
		
		float [] hHist = new float [bimage.data[0].length];
		for (int i=0;i<hHist.length;i++)
			hHist[i] = bimage.lineScore(i)*1f/bimage.data.length;
		hHist = PixelUtils.blur(hHist, hHist.length/(16*nLines));
		//display(fromScores(hHist, false));
		
		int [] vTrims = ScoreUtils.trim(hHist, .0125f);
		tp.panel.lsp.separators[0].y = vTrims[0]*1./hHist.length;
		tp.panel.lsp.separators[nLines].y = vTrims[1]*1./hHist.length;
		double w = tp.panel.lsp.separators[nLines].y-tp.panel.lsp.separators[0].y;
		
		for (int i=1;i<nLines;i++)
		{
			int base = (int)(hHist.length*(tp.panel.lsp.separators[0].y+i*w/nLines));
			base = ScoreUtils.moveToLowestScore(base, hHist, hHist.length/(32*nLines));
			//tp.panel.lsp.separators[i] = tp.panel.lsp.separators[0]+i*w/nLines;
			tp.panel.lsp.separators[i].y = base*1./hHist.length;
		}
		
		for (int i=0;i<nLines;i++)
		{
			float [] vHist = new float [bimage.data.length];
			int y0 = (int)(bimage.data[0].length*tp.panel.lsp.separators[i].y);
			int h = (int)(bimage.data[0].length*(tp.panel.lsp.separators[i+1].y-tp.panel.lsp.separators[i].y));
			for (int j=0;j<bimage.data.length;j++)
				vHist[j] = ScoreUtils.columnScore(bimage, j, y0, h);
			vHist = PixelUtils.blur(vHist, vHist.length/(4*nChars));
			//display(fromScores(vHist, true));
			
			int [] hTrims = ScoreUtils.trim(vHist, .025f);
			tp.panel.lep.extents[i][0] = hTrims[0]*1./vHist.length;
			tp.panel.lep.extents[i][1] = hTrims[1]*1./vHist.length;
			
			springDetect(tp, i, vHist);
		}
	}
	
	public static void springDetect(TranscriptionPanel tp, int line) {springDetect(tp, line, null);}
	static void springDetect(TranscriptionPanel tp, int line, float [] vHist)
	{
		int maxChars = 0;
		for (int i=0;i<tp.linesModel.getSize();i++)
			maxChars = Math.max(maxChars, tp.linesModel.get(i).toString().length());
		
		//int nChars = tp.linesModel.get(line).toString().length();
		if (vHist == null)
		{
			BinaryImage bimage = tp.panel.editor.aimage.bimage;
			vHist = new float [bimage.data.length];
			int y0 = (int)(bimage.data[0].length*tp.panel.lsp.separators[line].y);
			int h = (int)(bimage.data[0].length*(tp.panel.lsp.separators[line+1].y-tp.panel.lsp.separators[line].y));
			for (int j=0;j<bimage.data.length;j++)
				vHist[j] = ScoreUtils.columnScore(bimage, j, y0, h);
			vHist = PixelUtils.blur(vHist, vHist.length/(maxChars));
		}
		
		LineBreakdown lb = tp.breakdowns[line];
		double lineChars = 0;
		for (int i=0;i<lb.words.length;i++)
		{
			String word = lb.words[i];
			String prev = i == 0 ? null : lb.words[i-1];
			String next = i == lb.words.length-1 ? null : lb.words[i+1];
			lineChars += word.length()+(!word.equals(".") && prev != null ? .5 : 0)+(next != null ? .5 : 0);
		}
		for (int i=0;i<lb.delimiters.length;i++)
		{
			String word = lb.words[i];
			String prev = i == 0 ? null : lb.words[i-1];
			String next = i == lb.words.length-1 ? null : lb.words[i+1];
			double len = word.length()+(!word.equals(".") && prev != null ? .5 : 0)+(next != null ? .5 : 0);
			lb.delimiters[i] = (i > 0 ? lb.delimiters[i-1] : 0)+len/lineChars;
		}
		
		int [] initialDelims = new int [lb.delimiters.length];
		double ew = tp.panel.lep.extents[line][1]-tp.panel.lep.extents[line][0];
		for (int j=0;j<initialDelims.length;j++)
			{initialDelims[j] = (int)(vHist.length*(tp.panel.lep.extents[line][0]+ew*lb.delimiters[j]));}
		
		int [] delims = new int [lb.delimiters.length];
		for (int j=0;j<delims.length;j++)
			delims[j] = initialDelims[j];
		
		int [] tempDelims = new int [lb.delimiters.length];
		
		for (int k=0;k<30;k++)
		{
			for (int j=0;j<delims.length;j++)
			{
				int cur = delims[j];
				int leftCons = j == 0 ? initialDelims[j] : delims[j-1]+(initialDelims[j]-initialDelims[j-1]);
				int rightCons = j == delims.length-1 ? initialDelims[j] : delims[j+1]-(initialDelims[j+1]-initialDelims[j]);
				int histCons = ScoreUtils.moveToLowestScore(delims[j], vHist, vHist.length/(maxChars));
				tempDelims[j] = (cur+leftCons+rightCons+histCons)/4;
			}
			
			for (int j=0;j<delims.length;j++)
				delims[j] = tempDelims[j];
		}
		
		for (int j=0;j<delims.length;j++)
			lb.delimiters[j] = (delims[j]-vHist.length*tp.panel.lep.extents[line][0])/(vHist.length*ew);
	}
	
	static BufferedImage fromScores(float [] scores, boolean hor)
	{
		BufferedImage image = new BufferedImage(hor ? scores.length : 50, hor ? 50 : scores.length, 
			BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		for (int i=0;i<scores.length;i++)
		{
			g.setColor(new Color(scores[i], scores[i], scores[i]));
			g.drawLine(hor ? i : 0, hor ? 0 : i, hor ? i : image.getWidth(), hor ? image.getHeight() : i);
		}
		return image;
	}
	
	@SuppressWarnings("serial")
	static void display(final BufferedImage image)
	{
		JDialog dialog = new JDialog();
		JLabel label = new JLabel() {public void paintComponent(Graphics g) {g.drawImage(image, 0, 0, null);}};
		label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		dialog.add(label);
		dialog.pack();
		dialog.setVisible(true);
	}
}
