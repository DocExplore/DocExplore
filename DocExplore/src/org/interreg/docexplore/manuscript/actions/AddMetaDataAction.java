/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.manuscript.actions;

import java.util.Collections;
import java.util.List;

import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.Book;
import org.interreg.docexplore.manuscript.MetaData;

public class AddMetaDataAction extends UnreversibleAction
{
	public AnnotatedObject document;
	public List<MetaData> annotations;
	
	public AddMetaDataAction(AnnotatedObject document, MetaData annotation)
	{
		this.document = document;
		this.annotations = Collections.singletonList(annotation);
	}
	public AddMetaDataAction(AnnotatedObject document, List<MetaData> annotations)
	{
		this.document = document;
		this.annotations = annotations;
	}
	
	public void doAction() throws Exception
	{
		for (MetaData annotation : annotations)
		{
			if (document instanceof Book)
			{
				List<MetaData> links = annotation.getMetaDataListForKey(document.getLink().getOrCreateKey("book", ""));
				for (MetaData link : links)
					if (link.getType().equals(MetaData.textType))
						link.setString(""+document.getId());
			}
			document.addMetaData(annotation);
		}
	}

	public String description() {return Lang.s("addMetaData");}
	
	public void dispose()
	{
		document = null;
		annotations = null;
	}
}
