package edu.pitt.sis.adapt2.pservice.datamodel;

import java.io.Serializable;

import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.ItemVector;
import edu.pitt.sis.paws.core.iItem;

public class PServiceItem extends Item implements iItem, Serializable
{
	static final long serialVersionUID = 30L;
	
	private ItemVector<ConfItem> confs;
	private ItemVector<VizItem> vizs;
	
	private String name;
	private String description;
	private String class_name;
	
	public PServiceItem()
	{
		super();
		name = "";
		description = "";
		class_name = "";
		confs = new ItemVector<ConfItem>();
		vizs = new ItemVector<VizItem>();
	}
	
	public PServiceItem(int _id, String _title, String _name, String _desc, String _class_name)
	{
		super(_id,_title);
		name = _name;
		description = _desc;
		class_name = _class_name;
		confs = new ItemVector<ConfItem>();
		vizs = new ItemVector<VizItem>();
	}

	public ItemVector<ConfItem> getConfs() { return confs; }
	public ItemVector<VizItem> getVizs() { return vizs; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getClassName() { return class_name; }

	public String getVisualizerHTMLSelector()
	{
		String result = "";
		String options = "";
		
		options += "	<option value='' selected>-No Visualizer-</option>\n";
		for(int i=0; i<vizs.size(); i++)
			options += "	<option value='" + vizs.get(i).getTitle() + 
					"'>" + vizs.get(i).getName() + "</option>\n";
		result += "<select name='" + DataRobot.REST_VISUALIZER_ID + "' size='1'>\n" +
			options + "</select>\n";
		return result;
	}

}
