/**
Copyright LITIS/EDA 2014
contact@docexplore.eu

This software is a computer program whose purpose is to manage and display interactive digital books.

This software is governed by the CeCILL license under French law and abiding by the rules of distribution of free software.  You can  use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy, modify and redistribute granted by the license, users are provided only with a limited warranty  and the software's author,  the holder of the economic rights,  and the successive licensors  have only  limited liability.

In this respect, the user's attention is drawn to the risks associated with loading,  using,  modifying and/or developing or reproducing the software by the user in light of its specific status of free software, that may mean  that it is complicated to manipulate,  and  that  also therefore means  that it is reserved for developers  and  experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards their requirements in conditions enabling the security of their systems and/or data to be ensured and,  more generally, to use and operate it in the same conditions as regards security.

The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you accept its terms.
 */
package org.interreg.docexplore.management.search;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.management.search.SearchHandler.SearchSummary;
import org.interreg.docexplore.manuscript.AnnotatedObject;
import org.interreg.docexplore.manuscript.MetaDataKey;
import org.interreg.docexplore.manuscript.AnnotatedObject.ObjectStub;
import org.interreg.docexplore.manuscript.ResultBuffer;
import org.interreg.docexplore.util.Pair;

@SuppressWarnings("rawtypes")
public class ResultMerger
{
	static class Result
	{
		
		ResultBuffer buffer;
		Class clazz;
		int index;
		
		Result(ResultBuffer buffer, Class clazz, int index)
		{
			this.buffer = buffer;
			this.clazz = clazz;
			this.index = index;
		}
		
		double getScore() {return buffer.getScore(index);}
		@SuppressWarnings("unchecked")
		List<Pair<MetaDataKey, String>> getMetaDataText() throws DataLinkException {return buffer.getMetaDataText(index);}
		ObjectStub<?> getStub() throws DataLinkException {return buffer.getStub(index);}
	}
	
	List<Pair<MetaDataKey, String>> criteria;
	double relevance;
	List<Result> results;
	
	public ResultMerger(SearchSummary summary)
	{
		this.criteria = summary.criteria;
		this.relevance = summary.relevance;
		
		@SuppressWarnings("unchecked")
		Map.Entry<Class<? extends AnnotatedObject>, ResultBuffer<? extends AnnotatedObject>> [] buffers = 
			new Map.Entry [summary.results.size()];
		int size = 0, cnt = 0;
		for (Map.Entry<Class<? extends AnnotatedObject>, ResultBuffer<? extends AnnotatedObject>> buffer : summary.results.entrySet())
		{
			size += buffer.getValue().bufferSize();
			buffers[cnt++] = buffer;
		}
		this.results = new Vector<ResultMerger.Result>(size);
		
		int [] cursors = new int [buffers.length];
		for (int i=0;i<cursors.length;i++)
			cursors[i] = 0;
		
		while (results.size() < size)
		{
			double max = 0;
			int index = 0;
			for (int i=0;i<buffers.length;i++)
				if (cursors[i] < buffers[i].getValue().bufferSize() && buffers[i].getValue().getScore(cursors[i]) > max)
				{
					index = i;
					max = buffers[i].getValue().getScore(cursors[i]);
				}
			
			results.add(new Result(buffers[index].getValue(), buffers[index].getKey(), cursors[index]));
			cursors[index]++;
		}
	}
	
	public int size() {return results.size();}
	public double getScore(int i) {return results.get(i).getScore();}
	public List<Pair<MetaDataKey, String>> getMetaDataText(int i) throws DataLinkException {return results.get(i).getMetaDataText();}
	public Class<?> getClass(int i) {return results.get(i).clazz;}
	public ObjectStub<?> getStub(int i) throws DataLinkException {return results.get(i).getStub();}
}
