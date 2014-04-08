/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
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
