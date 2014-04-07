package org.interreg.docexplore.stitch;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.util.ImageUtils;

public class Subdivider
{
	public static void main(String [] args) throws Exception
	{
		File dir = new File("C:\\sci\\workspace\\DocExplore\\server-resources\\book32");
		Map<Integer, File> files = new TreeMap<Integer, File>();
		for (File file : dir.listFiles())
			if (file.getName().startsWith("image") && file.getName().endsWith(".png"))
				files.put(Integer.parseInt(file.getName().substring(5, file.getName().length()-4)), file);
		
		File out = new File(dir, "out");
		out.mkdir();
		
		int cnt = 0, cnt2 = 0;
		for (File file : files.values())
		{
			BufferedImage big = ImageUtils.read(file);
			BufferedImage small1 = new BufferedImage(big.getWidth(), big.getHeight()/2, BufferedImage.TYPE_3BYTE_BGR);
			BufferedImage small2 = new BufferedImage(big.getWidth(), big.getHeight()-small1.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			
			Graphics2D g = small1.createGraphics();
			g.drawImage(big, 0, 0, small1.getWidth(), small1.getHeight(), 0, 0, small1.getWidth(), small1.getHeight(), null);
			g = small2.createGraphics();
			g.drawImage(big, 0, 0, small2.getWidth(), small2.getHeight(), 0, small1.getHeight(), small2.getWidth(), big.getHeight(), null);
			
			ImageUtils.write(small1, "PNG", new File(out, "image"+cnt+".png")); cnt++;
			ImageUtils.write(small2, "PNG", new File(out, "image"+cnt+".png")); cnt++;
			System.out.println((++cnt2)+"/"+files.size());
		}
	}
}
