package org.interreg.docexplore.management.process.align;

public class TextMetrics
{
	public static class LineMetrics
	{
		String line;
		int [] spaces;
		
		LineMetrics(String line)
		{
			this.line = line;
			
			int nSpaces = 0;
			for (int i=0;i<line.length();i++)
				if (line.charAt(i) == ' ')
					nSpaces++;
			
			this.spaces = new int [nSpaces];
			nSpaces = 0;
			for (int i=0;i<line.length();i++)
				if (line.charAt(i) == ' ')
					spaces[nSpaces++] = i;
		}
		
	}
	
	LineMetrics [] lines;
	
	public TextMetrics(String text)
	{
		String [] textLine = text.split("\n");
		lines = new LineMetrics [textLine.length];
		for (int i=0;i<textLine.length;i++)
			lines[i] = new LineMetrics(textLine[i]);
	}
}
