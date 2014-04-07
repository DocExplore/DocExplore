package org.interreg.docexplore.authoring.explorer.edit;

import java.util.LinkedList;
import java.util.List;

import org.interreg.docexplore.authoring.BookImporter;
import org.interreg.docexplore.internationalization.XMLResourceBundle;
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
		return XMLResourceBundle.getBundledString("moveAnnotation");
	}

}
