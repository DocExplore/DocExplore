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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

public class LineBreakdown
{
	String [] words;
	double [] delimiters;
	int highlighted;
	TranscriptionPanel tp;
	
	public LineBreakdown(TranscriptionPanel tp)
	{
		this.tp = tp;
		this.words = new String [0];
		this.delimiters = new double [0];
		this.highlighted = -1;
	}
	
	public void setLine(String line) {setLine(line, 0);}
	public void setLine(String line, int maxTokens)
	{
		StringBuffer sb = new StringBuffer();
		LinkedList<String> tokens = new LinkedList<String>();
		
		for (int i=0;i<line.length();i++)
		{
			char c = line.charAt(i);
			if (!Character.isLetter(c))
			{
				if (sb.length() > 0)
				{
					tokens.add(sb.toString());
					sb.delete(0, sb.length());
				}
				if (!Character.isWhitespace(c))
					tokens.add(""+c);
			}
			else sb.append(c);
		}
		if (sb.length() > 0)
			tokens.add(sb.toString());
		
		if (maxTokens > 0 && tokens.size() > maxTokens)
		{
			sb.delete(0, sb.length());
			sb.append(tokens.get(maxTokens-1)).append(' ');
			for (int i=maxTokens;i<tokens.size();i++)
				sb.append(tokens.get(i)).append(' ');
			while (tokens.size() > maxTokens)
				tokens.removeLast();
			tokens.removeLast();
			tokens.add(sb.toString());
		}
		
		this.words = tokens.toArray(new String [tokens.size()]);
		this.delimiters = new double [words.length-1];
		for (int i=0;i<delimiters.length;i++)
			delimiters[i] = (i+1)*1./(words.length);
		this.highlighted = -1;
	}
	
	public boolean mouseMoved(int line, double x, double y, LabeledImageViewer editor)
	{
		double r = 4*editor.cz;
		int hovered = -1;
		double ly0 = tp.panel.lsp.separators[line].yAt(x), ly1 = tp.panel.lsp.separators[line+1].yAt(x);
		if (!(x < 0 || x > 1 || y < ly0 || y > ly1))
			for (int i=0;i<delimiters.length;i++)
			{
				double lx = editor.toScreenXLength(x-tp.panel.lep.fromLineX(line, delimiters[i]));
				if (lx*lx < r*r)
				{
					hovered = i;
					break;
				}
			}
		
		boolean changed = hovered != highlighted;
		highlighted = hovered;
		return changed;
	}

	public boolean mouseDragged(int line, double x, double y)
	{
		if (highlighted < 0)
			return false;
		
		double nl = tp.panel.lep.toLineX(line, x);
		double lim = .01;
		if (nl < lim) nl = lim;
		if (nl > 1-lim) nl = 1-lim;
		if (highlighted > 0 && nl < delimiters[highlighted-1]+lim)
			nl = delimiters[highlighted-1]+lim;
		if (highlighted < delimiters.length-1 && nl > delimiters[highlighted+1]-lim)
			nl = delimiters[highlighted+1]-lim;
		delimiters[highlighted] = nl;
		
		return true;
	}
	
	static Font font = Font.decode("Arial-bold-12");
	static void setFontSize(float size)
	{
		font = font.deriveFont(size);
	}
	
	Stroke stroke = new BasicStroke(1);
	Color glassBlue = new Color(0, 127, 255, 127);
	Color glassYellow = new Color(255, 255, 0, 127);
	Color glassBlack = new Color(0, 0, 0, 127);
	public void render(Graphics2D g, int line)
	{
		int w = tp.panel.editor.displayBuffer.getWidth();
		int h = tp.panel.editor.displayBuffer.getHeight();
		int lw = (int)(w*(tp.panel.lep.extents[line][1]-tp.panel.lep.extents[line][0]));
		int lx0 = (int)(w*tp.panel.lep.extents[line][0]);
		
		g.setStroke(stroke);
		for (int i=0;i<delimiters.length;i++)
		{
			double l = delimiters[i];
			if (i == highlighted) g.setColor(glassYellow);
			else g.setColor(glassBlue);
			
			double x = tp.panel.lep.extents[line][0]+(tp.panel.lep.extents[line][1]-tp.panel.lep.extents[line][0])*l;
			
			int ly0 = (int)(h*tp.panel.lsp.separators[line].yAt(x));
			int ly1 = (int)(h*tp.panel.lsp.separators[line+1].yAt(x));
			
			g.drawLine((int)(w*x), ly0, (int)(w*x), ly1);
		}
		
		int margin = 3;
		g.setFont(font);
		for (int i=0;i<words.length;i++)
		{
			Rectangle2D wb = g.getFont().getStringBounds(words[i], g.getFontRenderContext());
			int wx0 = lx0+(int)(lw*(i == 0 ? 0 : delimiters[i-1]));
			int ww = lx0+(int)(lw*(i == words.length-1 ? 1 : delimiters[i]))-wx0;
			
			int tx0 = (int)(wx0+(ww-wb.getWidth())/2);
			int ly0 = (int)(h*tp.panel.lsp.separators[line].y);
			int ty0 = ly0+(int)(1.2*g.getFontMetrics().getHeight());
			
			g.setColor(glassBlack);
			g.fillRect(tx0-margin, ty0-margin-(int)(3*wb.getHeight()/4), (int)(wb.getWidth()+2*margin), (int)(wb.getHeight()+2*margin));
			
			g.setColor(Color.white);
			g.drawString(words[i], tx0, ty0);
		}
	}
}
