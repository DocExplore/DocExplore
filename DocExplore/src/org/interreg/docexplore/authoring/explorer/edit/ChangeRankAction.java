/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.authoring.explorer.edit;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.internationalization.Lang;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaData;
import org.interreg.docexplore.util.history.ReversibleAction;

public class ChangeRankAction extends ReversibleAction
{
	MetaDataEditor editor;
	AnnotatedObject document;
	List<MetaData> annotations;
	MetaData annotation;
	int from, to;
	
	public ChangeRankAction(MetaDataEditor editor, MetaData annotation, int from, int to)
	{
		this.editor = editor;
		this.document = editor.document;
		this.annotations = new LinkedList<MetaData>();
		for (InfoElement element : editor.elements)
			annotations.add(element.md);
		this.annotation = annotation;
		this.from = from;
		this.to = to;
	}
	
	public void doAction() throws Exception
	{
		move(from, to);
		
		if (document == editor.document)
			editor.reload();
	}

	public void undoAction() throws Exception
	{
		move(to, from);
		
		if (document == editor.document)
			editor.reload();
	}
	
	void move(int from, int to) throws Exception
	{
		for (MetaData md : annotations)
		{
			int rank = BookImporter.getRank(md);
			if (from < to && rank > from && rank <= to)
				BookImporter.setRank(md, rank-1);
			else if (from > to && rank < from && rank >= to)
				BookImporter.setRank(md, rank+1);
		}
		BookImporter.setRank(annotation, to);
	}
	
	public String description()
	{
		return Lang.s("moveAnnotation");
	}

}
