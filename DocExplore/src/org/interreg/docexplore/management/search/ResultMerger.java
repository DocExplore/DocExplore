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
