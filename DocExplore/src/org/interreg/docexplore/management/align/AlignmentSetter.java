package org.interreg.docexplore.management.align;

import java.awt.event.MouseEvent;

public class AlignmentSetter
{
	TranscriptionPanel tp;
	int curLine;
	int nTokens ;
	
	public AlignmentSetter(TranscriptionPanel tp)
	{
		this.tp = tp;
		this.curLine = -1;
		this.nTokens = 0;
	}
	
	public void setLine(int line)
	{
		curLine = line;
		nTokens = tp.breakdowns[line].words.length;
		if (nTokens < 2)
			curLine = -1;
		else
		{
			tp.breakdowns[line].setLine(tp.linesModel.get(line).toString(), 2);
			tp.breakdowns[line].delimiters[0] = .5;
		}
		tp.panel.editor.repaint();
	}
	
	double lim = .01;
	public boolean mouseMoved(double x, double y)
	{
		if (curLine == -1)
			return false;
		
		double lx0 = tp.panel.lep.extents[curLine][0];
		double lw = (tp.panel.lep.extents[curLine][1]-tp.panel.lep.extents[curLine][0]);
		
		double cx = (x-lx0)*1./lw;
		if (cx < 0 || cx > 1)
			return false;
		
		cx = cx < lim ? lim : cx > 1-lim ? 1-lim : cx;
		
		LineBreakdown lb = tp.breakdowns[curLine];
		if (lb.delimiters.length > 1 && cx < lb.delimiters[lb.delimiters.length-2]+lim)
			cx = lb.delimiters[lb.delimiters.length-2]+lim;
		lb.delimiters[lb.delimiters.length-1] = cx;
		return true;
	}
	
	public boolean mouseClicked(int button, double x, double y)
	{
		if (curLine == -1)
			return false;
		
		LineBreakdown lb = tp.breakdowns[curLine];
		double lx0 = tp.panel.lep.extents[curLine][0];
		double ly0 = tp.panel.lsp.separators[curLine].y;
		double lw = (tp.panel.lep.extents[curLine][1]-tp.panel.lep.extents[curLine][0]);
		double lh = (tp.panel.lsp.separators[curLine+1].y-tp.panel.lsp.separators[curLine].y);
		
		if (button == MouseEvent.BUTTON1)
		{
			double cx = (x-lx0)*1./lw, cy = (y-ly0)*1./lh;
			if (cx < 0 || cx > 1 || cy < 0 || cy > 1)
				return false;
			
			if (lb.delimiters.length > 1 && cx < lb.delimiters[lb.delimiters.length-2]+lim)
				cx = lb.delimiters[lb.delimiters.length-2]+lim;
			lb.delimiters[lb.delimiters.length-1] = cx;
			if (lb.delimiters.length == nTokens-1)
			{
				if (tp.lines.getSelectedIndex() == -1 && tp.linesModel.getSize() > curLine+1)
					setLine(curLine+1);
				else curLine = -1;
			}
			else
			{
				double [] delims = lb.delimiters;
				lb.setLine(tp.linesModel.get(curLine).toString(), delims.length+2);
				for (int i=0;i<delims.length;i++)
					lb.delimiters[i] = delims[i];
				lb.delimiters[delims.length] = delims[delims.length-1];
			}
			return true;
		}
		else if (button == MouseEvent.BUTTON3 && lb.delimiters.length > 1)
		{
			double [] delims = lb.delimiters;
			lb.setLine(tp.linesModel.get(curLine).toString(), delims.length);
			for (int i=0;i<lb.delimiters.length;i++)
				lb.delimiters[i] = delims[i];
			return true;
		}
		
		return false;
	}
	
	public void cancel()
	{
		if (curLine == -1)
			return;
		
		LineBreakdown lb = tp.breakdowns[curLine];
		double [] delims = lb.delimiters;
		lb.setLine(tp.linesModel.get(curLine).toString());
		for (int i=0;i<delims.length-1;i++)
			lb.delimiters[i] = delims[i];
		int ind0 = delims.length-2 >= 0 ? delims.length-2 : 0;
		for (int i=delims.length-1;i<lb.delimiters.length;i++)
			lb.delimiters[i] = (i-ind0+1)*1./(lb.delimiters.length-ind0+1);
	}
}
