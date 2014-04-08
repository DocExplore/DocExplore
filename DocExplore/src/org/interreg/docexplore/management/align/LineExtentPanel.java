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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;

public class LineExtentPanel implements LabeledImageViewer.EditorListener
{
	LabeledImageViewer editor;
	LineSeparatorPanel lsp;
	double [][] extents;
	int focusedLine;
	int highlighted;
	
	public LineExtentPanel(LabeledImageViewer editor, LineSeparatorPanel lsp)
	{
		this.editor = editor;
		this.lsp = lsp;
		this.extents = new double [0][2];
		this.focusedLine = -1;
		this.highlighted = -1;
		
		editor.addEditorListener(this);
	}
	
	public double toLineX(int line, double gx) {return (gx-extents[line][0])/(extents[line][1]-extents[line][0]);}
	public double fromLineX(int line, double lx) {return extents[line][0]+lx*(extents[line][1]-extents[line][0]);}
	
	public void mouseMoved(LabeledImageViewer editor, double x, double y)
	{
		int hoveredLine = -1;
		if (y > lsp.separators[0].y)
			for (int i=0;i<lsp.separators.length-1;i++)
				if (y < lsp.separators[i+1].y)
					{hoveredLine = i; break;}
		
		int hovered = hoveredLine != -1 ? extentAt(hoveredLine, x, y, editor) : -1;
		
		if (hovered == highlighted && hoveredLine == focusedLine)
			return;
		
		highlighted = hovered;
		focusedLine = hoveredLine;
		LineExtentPanel.this.editor.repaint();
	}
	public void mouseDragged(LabeledImageViewer editor, int button, double x, double y)
	{
		if ((button & InputEvent.BUTTON1_MASK) == 0)
			return;
		if (focusedLine < 0 || highlighted < 0)
			return;
		
		putExtentAt(highlighted, x);
	}
	public void mouseClicked(LabeledImageViewer editor, int button, int modifiers, double x, double y) {}
	
	private void putExtentAt(int i, double x)
	{
		if (x > 1) x = 1;
		if (x < 0) x = 0;
		
		double lim = .01;
		if (i == 0 && x > extents[focusedLine][1]-lim)
			x = extents[focusedLine][1]-lim;
		if (i == 1 && x < extents[focusedLine][0]+lim)
			x = extents[focusedLine][0]+lim;
		
		extents[focusedLine][i] = x;
		editor.repaint();
	}
	
	private int extentAt(int line, double x, double y, LabeledImageViewer editor)
	{
		double r = 4*editor.cz;
		for (int i=0;i<2;i++)
		{
			double lx = editor.toScreenXLength(x-extents[line][i]);
			if (lx*lx < r*r)
				return i;
		}
		
		return -1;
	}
	
	public void setNLines(int n)
	{
		this.extents = new double [n][2];
		for (int i=0;i<n;i++)
		{
			extents[i][0] = .01;
			extents[i][1] = .99;
		}
		this.focusedLine = -1;
	}
	
	public void rendered(LabeledImageViewer editor, Graphics2D g)
	{
		if (lsp.separators.length < 2)
			return;
		
		int h = editor.displayBuffer.getHeight();
		int w = editor.displayBuffer.getWidth();
		
		g.setStroke(lsp.stroke);
		for (int i=0;i<lsp.separators.length-1;i++)
		{
			int lx0 = (int)(w*extents[i][0]);
			int lx1 = (int)(w*extents[i][1]);
			int ly0 = (int)(h*lsp.separators[i].y);
			int ly1 = (int)(h*lsp.separators[i+1].y);
			int ly0x0 = (int)(h*lsp.separators[i].yAt(extents[i][0]));
			int ly1x0 = (int)(h*lsp.separators[i+1].yAt(extents[i][0]));
			int ly0x1 = (int)(h*lsp.separators[i].yAt(extents[i][1]));
			int ly1x1 = (int)(h*lsp.separators[i+1].yAt(extents[i][1]));
			
			g.setColor(new Color(255, 255, 255, 127));
			g.fillRect(0, ly0, lx0, ly1-ly0);
			g.fillRect(lx1, ly0, w-lx1, ly1-ly0);
			
			if (focusedLine == i && highlighted == 0) g.setColor(lsp.glassYellow);
			else g.setColor(lsp.glassRed);
			g.drawLine(lx0, ly0x0, lx0, ly1x0);
			if (focusedLine == i && highlighted == 1) g.setColor(lsp.glassYellow);
			else g.setColor(lsp.glassRed);
			g.drawLine(lx1, ly0x1, lx1, ly1x1);
		}
	}
}
