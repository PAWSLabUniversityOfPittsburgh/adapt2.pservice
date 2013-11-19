package edu.pitt.sis.adapt2.pservice;

public class Visualizer
{
	public String rdfID;
	public String name;
	public String class_name;
	public String description;
	
	public Visualizer()
	{
		rdfID = "";
		name = "";
		class_name = "";
		description = "";
	}
	
	public Visualizer(String _rdfID, String _name, String _class_name, String _description)
	{
		rdfID = _rdfID;
		name = _name;
		class_name = _class_name;
		description = _description;
	}

}
