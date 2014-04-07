package org.interreg.docexplore.management.align;

public class ScoreUtils
{
	public static int [] trim(float [] scores, float threshold)
	{
		int [] trims = {0, scores.length-1};
		while (scores[trims[0]] < threshold && trims[0] < scores.length/2)
			trims[0]++;
		while (scores[trims[1]] < threshold && trims[1] > scores.length/2)
			trims[1]--;
		
		return trims;
	}
	
	public static int moveToLowestScore(int start, float [] scores, int ray)
	{
		boolean moved = true;
		while (moved)
		{
			moved = false;
			int lower = start;
			for (int i=start-ray;i<=start+ray;i++)
				if (i>=0 && i<scores.length && scores[i] < scores[lower])
					{moved = true; lower = i;}
			start = lower;
		}
		return start;
	}
	
	public static float columnScore(BinaryImage bimage, int x, int y0, int h)
	{
		int sum = 0;
		for (int i=0;i<h;i++)
			if (bimage.data[x][y0+i])
				sum++;
		return sum*1f/h;
	}
}
