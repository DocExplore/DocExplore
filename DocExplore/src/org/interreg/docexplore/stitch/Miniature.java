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
