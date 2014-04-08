/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public abstract class BufferedImageSource implements ImageSource
{
	private static final long serialVersionUID = -7172674019910216219L;
	
	protected BufferedImage buffer;
	
	public BufferedImageSource()
	{
		this.buffer = null;
	}
	
	public BufferedImage getImage() throws Exception
	{
		if (buffer == null)
		{
			InputStream in = getFile();
			try
			{
				buffer = ImageUtils.read(in);
				if (buffer == null)
					throw new NullPointerException("Unable to read image!");
				return buffer;}
			catch (Exception e)
			{
				if (in != null)
					try {in.close();}
					catch (Exception ex) {}
				throw e;
			}
		}
		else return buffer;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {this.buffer = null;}
}
