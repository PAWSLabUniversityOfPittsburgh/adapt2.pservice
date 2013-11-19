package edu.pitt.sis.adapt2.pservice;


/**
 * @author michael_yudelson
 * This class is a container for several ways to express annotation of a personalized resource, e.g. icon, style, summary (description)
 */
public class Annotation
{
	public String icon = "";
	public String style = "";
	public String summary = "";
	/**
	 * Empty annotation constructor
	 */
	public Annotation() 
	{
		icon = "";
		style = "";
		summary = "";
	}
	
	public Annotation(String _icon, String _style, String _summary)
	{
		icon = _icon;
		style = _style;
		summary = _summary;
	}
}
