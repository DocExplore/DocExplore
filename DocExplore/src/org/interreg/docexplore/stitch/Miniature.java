package org.interreg.docexplore.stitch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.interreg.docexplore.util.ImageUtils;

public class Miniature
{
	public static void main(String [] args) throws Exception
	{
		File dir = new File("C:\\sci\\workspace\\DocExplore\\server-resources\\book32");
		Map<Integer, File> files = new TreeMap<Integer, File>();
		for (File file : dir.listFiles())
			if (file.getName().startsWith("image") && file.getName().endsWith(".png"))
				files.put(Integer.parseInt(file.getName().substring(5, file.getName().length()-4)), file);
		BufferedImage [] minis = new BufferedImage [files.size()];
		int h = 0, cnt = 0;
		for (File file : files.values())
		{
			BufferedImage big = ImageUtils.read(file);
			BufferedImage small = Scalr.resize(big, Method.QUALITY, big.getHeight()/41);
			h += small.getHeight();
			minis[cnt++] = small;
			System.out.println(cnt+"/"+files.size());
		}
		BufferedImage res = new BufferedImage(minis[0].getWidth(), h, BufferedImage.TYPE_3BYTE_BGR);
		h = 0;
		for (BufferedImage small : minis)
		{
			res.createGraphics().drawImage(small, 0, h, null);
			h += small.getHeight();
		}
		ImageUtils.write(res, "PNG", new File(dir, "mini.png"));
	}
}
