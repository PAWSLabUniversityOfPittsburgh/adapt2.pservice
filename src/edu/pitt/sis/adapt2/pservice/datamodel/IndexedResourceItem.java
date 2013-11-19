package edu.pitt.sis.adapt2.pservice.datamodel;

import java.util.Iterator;

import edu.pitt.sis.paws.core.ItemVector;
import edu.pitt.sis.paws.core.iHierarchicalItem2;

public class IndexedResourceItem extends ResourceItem implements iHierarchicalItem2
{
	static final long serialVersionUID = 5L;
	
	public final static int CONCEPT_NOT_LEARNED = 0;
	public final static int CONCEPT_LEARNED = 1;
	public final static int CONCEPT_PREREQUISITE = -1;
	public final static int CONCEPT_OUTCOME = 1;

	private ItemVector<ConceptItem> concepts; 
	private ItemVector<ConceptItem> pre_concepts; 
	private ItemVector<ConceptItem> out_concepts; 
	
	public IndexedResourceItem()
	{
		super();
		concepts = new ItemVector<ConceptItem>();
		pre_concepts = new ItemVector<ConceptItem>();
		out_concepts = new ItemVector<ConceptItem>();
	}

	public IndexedResourceItem(int _id, String _title, String _uri)
	{
		super(_id, _title, _uri);
		concepts = new ItemVector<ConceptItem>();
		pre_concepts = new ItemVector<ConceptItem>();
		out_concepts = new ItemVector<ConceptItem>();
	}

	public IndexedResourceItem(int _id, String _title, String _uri, double _progress)
	{
		super(_id, _title, _uri, _progress);
		concepts = new ItemVector<ConceptItem>();
		pre_concepts = new ItemVector<ConceptItem>();
		out_concepts = new ItemVector<ConceptItem>();
	}
	
	public IndexedResourceItem(String _uri)
	{
		super(_uri);
		concepts = new ItemVector<ConceptItem>();
		pre_concepts = new ItemVector<ConceptItem>();
		out_concepts = new ItemVector<ConceptItem>();
	}


	public String toString()
	{
		return "[ResourceItem title: " + this.getTitle() + " id:" + this.getId() + " uri:'" + this.getURI() + 
			"' progress: " + this.getProgress() + "]";
	}

	public ItemVector<ConceptItem> getConcepts() { return concepts; }
	public ItemVector<ConceptItem> getPrerequisiteConcepts() { return pre_concepts; }
	public ItemVector<ConceptItem> getOutcomeConcepts() { return out_concepts; }
	
	/**
	 * Based on concept tagging (Id filed) split concepts into prerequisites and outcomes
	 */
	public void splitConcepts()
	{
		for(Iterator<ConceptItem> iter = concepts.iterator(); iter.hasNext();)
		{
			ConceptItem concept = iter.next();
			if(concept.getId()==CONCEPT_LEARNED)
				pre_concepts.add(concept);
			else if(concept.getId()==CONCEPT_NOT_LEARNED)
				out_concepts.add(concept);
		}
	}
	
}
