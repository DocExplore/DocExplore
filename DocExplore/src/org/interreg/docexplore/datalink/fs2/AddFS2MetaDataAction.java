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

import java.util.List;

import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.manuscript.actions.AddMetaDataAction;

public class AddFS2MetaDataAction extends AddMetaDataAction
{
	DataLinkFS2 link;
	
	public AddFS2MetaDataAction(DataLinkFS2 link, AnnotatedObject document, MetaData annotation)
	{
		super(document, annotation);
		this.link = link;
	}
	public AddFS2MetaDataAction(DataLinkFS2 link, AnnotatedObject document, List<MetaData> annotations)
	{
		super(document, annotations);
		this.link = link;
	}

	DeleteFS2MetaDataAction reverse = null;
	public void doAction() throws Exception
	{
		if (reverse == null)
			super.doAction();
		else reverse.undoAction();
	}
	
	public void undoAction() throws Exception
	{
		if (reverse == null)
		{
			reverse = new DeleteFS2MetaDataAction(link, document, annotations);
			reverse.cacheDir = cacheDir;
		}
		reverse.doAction();
	}
}
