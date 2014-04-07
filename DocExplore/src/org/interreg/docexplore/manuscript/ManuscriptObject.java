package org.interreg.docexplore.manuscript;


/**
 * Base class of all manuscript objects. Defines an absolute ordering of instances based on their id in the {@link ManuscriptLink}.
 * @author Burnett
 *
 */
public abstract class ManuscriptObject implements Comparable<ManuscriptObject>
{
	ManuscriptLink link;
	int id;
	
	ManuscriptObject(ManuscriptLink link, int objectId)
	{
		this.link = link;
		this.id = objectId;
	}
	
	ManuscriptObject(ManuscriptLink link)
	{
		this(link, -1);
	}

	public int compareTo(ManuscriptObject mo)
	{
		return id-mo.id;
	}
	
	public int getId() {return id;}
	public ManuscriptLink getLink() {return link;}
}
