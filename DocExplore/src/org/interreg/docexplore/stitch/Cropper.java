package org.interreg.docexplore.stitch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.interreg.docexplore.util.ImageUtils;

public class Cropper
{
	public static void main(String [] args) throws Exception
	{
		File dir = new File("C:\\Users\\Alex\\Documents\\manuscrits\\Ms U 18 bis v1");
		Map<Integer, File> files = new TreeMap<Integer, File>();
		for (File file : dir.listFiles())
			if (file.getName().startsWith("tout") && file.getName().endsWith(".png"))
				files.put(Integer.parseInt(file.getName().substring(4, file.getName().length()-4)), file);
		for (File file : files.values())
		{
			BufferedImage in = ImageUtils.read(file);
			BufferedImage out = new BufferedImage(1922, 4151, BufferedImage.TYPE_3BYTE_BGR);
			for (int i=0;i<1922;i++)
				for (int j=0;j<4151;j++)
					out.setRGB(1921-i, j, in.getRGB(j, 50+i));
			ImageUtils.write(out, "PNG", new File(file.getParent(), "t"+file.getName()));
			System.out.println(file.getName());
		}
	}
}
