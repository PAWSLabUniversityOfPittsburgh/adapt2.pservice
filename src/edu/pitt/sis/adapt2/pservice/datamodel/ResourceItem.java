package edu.pitt.sis.adapt2.pservice.datamodel;

import edu.pitt.sis.paws.core.HierarchicalItem2;
import edu.pitt.sis.paws.core.iHierarchicalItem2;

public class ResourceItem extends HierarchicalItem2 implements iHierarchicalItem2
{
	static final long serialVersionUID = 5L;

	final static int IS_PREREQUISIT = 1;
	final static int IS_OUTCOME = -1;
	final static int IS_UNDEFINED = 0;
	
	private double progress;
	
	public ResourceItem()
	{
		super();
		progress = -1.0;
	}

	public ResourceItem(int _id, String _title, String _uri)
	{
		super(_id, _title, _uri);
		progress = -1.0;
	}

	public ResourceItem(int _id, String _title, String _uri, double _progress)
	{
		super(_id, _title, _uri);
		progress = _progress;
	}
	
	public ResourceItem(String _uri)
	{
		super(0, "", _uri);
		progress = -1.0;
	}


	public String toString()
	{
		return "[ResourceItem title: " + this.getTitle() + " id:" + this.getId() + " uri:'" + this.getURI() + 
			"' progress: " + progress + "]";
	}

	public double getProgress() { return progress; }
	public void setProgress(double _progress) { progress = _progress; }
	
	public double getMeanResourceProgress()
	{
		if(progress == -1.0 && this.getSubs().size() == 0)
			return 0.0;
		if(progress != -1.0 && this.getSubs().size() == 0)
			return progress;
		else
		{
			double result = 0;
			for(int i=0; i<this.getSubs().size(); i++)
				result += ((ResourceItem)this.getSubs().get(i)).getProgress();
			return result/this.getSubs().size();
		}
			
		
	}
	
}
