package edu.pitt.sis.adapt2.pservice.datamodel;

import java.io.Serializable;

import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.iItem;

public class ConceptItem extends Item implements iItem, Serializable
{
	static final long serialVersionUID = 30L;
	
	private double progress;
	
	public ConceptItem()
	{
		super();
		progress = -1;
	}
	
	public ConceptItem(int _id, String _title)
	{
		super(_id,_title);
		progress = -1;
	}

	public ConceptItem(int _id, String _title, double _progress)
	{
		super(_id,_title);
		progress = _progress;
	}

	public double getProgress() { return progress; }
	public void setProgress(double _progress) { progress=_progress; }
}
