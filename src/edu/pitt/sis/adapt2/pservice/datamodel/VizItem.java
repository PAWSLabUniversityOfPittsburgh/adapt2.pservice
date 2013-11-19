package edu.pitt.sis.adapt2.pservice.datamodel;

import java.io.Serializable;

import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.iItem;

public class VizItem extends Item implements iItem, Serializable
{
	static final long serialVersionUID = 30L;
	
	private String name;
	private String description;
	private String class_name;
	
	public VizItem()
	{
		super();
		name = "";
		description = "";
		class_name = "";
	}
	
	public VizItem(int _id, String _title, String _name, String _desc, String _class_name)
	{
		super(_id,_title);
		name = _name;
		description = _desc;
		class_name = _class_name;
	}
	
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getClassName() { return class_name; }

}
