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
