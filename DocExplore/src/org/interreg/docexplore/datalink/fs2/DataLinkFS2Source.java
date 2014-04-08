/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.datalink.fs2;

import java.io.File;
import java.io.IOException;

import org.interreg.docexplore.DocExploreTool;
import org.interreg.docexplore.datalink.DataLink;
import org.interreg.docexplore.gui.ErrorHandler;

public class DataLinkFS2Source implements DataLink.DataLinkSource
{
	private static final long serialVersionUID = 7381964118246575300L;
	public String file;
	
	@SuppressWarnings("unused")
	private DataLinkFS2Source() {this.file = null;}
	public DataLinkFS2Source(String file)
	{
		this.file = file;
	}
	
	DataLinkFS2 link = null;
	public DataLink getDataLink()
	{
		if (link == null) try {link = new DataLinkFS2(this);}
		catch (Exception e) {ErrorHandler.defaultHandler.submit(e);}
		return link;
	}
	
	File getFile()
	{
		File file = new File(this.file);
		if (!file.isAbsolute())
			file = new File(DocExploreTool.getHomeDir(), file.getPath());
		return file;
	}

	public String getDescription() {return "File system '"+file+"'";}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {out.writeUTF(file);}
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {this.file = in.readUTF();}
	
	public boolean equals(Object o)
	{
		if (o instanceof DataLinkFS2Source)
			return file.equals(((DataLinkFS2Source)o).file);
		return false;
	}
}