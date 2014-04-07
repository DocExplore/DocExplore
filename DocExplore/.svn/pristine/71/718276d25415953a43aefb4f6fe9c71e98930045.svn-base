package org.interreg.docexplore.manuscript;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.interreg.docexplore.datalink.DataLinkException;
import org.interreg.docexplore.manuscript.AnnotatedObject.ObjectStub;
import org.interreg.docexplore.util.Pair;

/**
 * The abstract representation of the results of a search operation. It holds references to manuscript objects that match a certain criteria. 
 * Subclasses are responsible for dereferencing.
 * Results are sorted by increasing score (relevance) from indexes 0 to {@link bufferSize()}-1.
 * @author Alexander Burnett
 *
 * @param <Type>
 */
public abstract class ResultBuffer<Type extends AnnotatedObject>
{
	ManuscriptLink link;
	List<Pair<String, Double>> ids;
	Set<Integer> metadataKeyIds = null;
	
	ResultBuffer(ManuscriptLink link, Map<String, Double> ids, List<Pair<MetaDataKey, String>> criteria)
	{
		this.link = link;
		Map<Double, List<String>> sorted = new TreeMap<Double, List<String>>();
		for (Map.Entry<String, Double> entry : ids.entrySet())
		{
			List<String> idsForScore = sorted.get(entry.getValue());
			if (idsForScore == null)
			{
				idsForScore = new LinkedList<String>();
				sorted.put(entry.getValue(), idsForScore);
			}
			idsForScore.add(entry.getKey());
		}
		this.ids = new Vector<Pair<String, Double>>(ids.size());
		for (Map.Entry<Double, List<String>> entry : sorted.entrySet())
			for (String id : entry.getValue())
				this.ids.add(new Pair<String, Double>(id, entry.getKey()));
		
		if (criteria != null)
		{
			this.metadataKeyIds = new TreeSet<Integer>();
			for (Pair<MetaDataKey, String> pair : criteria)
				metadataKeyIds.add(pair.first.id);
		}
	}
	
	public int bufferSize() {return ids.size();}
	
	/**
	 * Returns the matching text values and their associated {@link MetaDataKey} for the ith result.
	 * @param i
	 * @return
	 * @throws DataLinkException
	 */
	public List<Pair<MetaDataKey, String>> getMetaDataText(int i) throws DataLinkException
	{
		List<Pair<MetaDataKey, String>> text = new LinkedList<Pair<MetaDataKey,String>>();
		List<Pair<Integer, String>> rawText = link.getLink().getMetaDataText(ids.get(ids.size()-i-1).first, metadataKeyIds);
		for (Pair<Integer, String> pair : rawText)
			text.add(new Pair<MetaDataKey, String>(link.getKey(pair.first), pair.second));
		return text;
	}
	/**
	 * Returns the score (relevance) of the ith result.
	 * @param i
	 * @return
	 */
	public double getScore(int i) {return ids.get(ids.size()-i-1).second;}
	/**
	 * Return the stub for the ith result. The manuscript object itself can be retrieved from the stub.
	 * @param i
	 * @return
	 * @throws DataLinkException
	 */
	public <StubType extends ObjectStub<Type>> StubType getStub(int i) throws DataLinkException {return getStubFromId(ids.get(ids.size()-i-1).first);}
	protected abstract <StubType extends ObjectStub<Type>> StubType getStubFromId(String id) throws DataLinkException;
}
