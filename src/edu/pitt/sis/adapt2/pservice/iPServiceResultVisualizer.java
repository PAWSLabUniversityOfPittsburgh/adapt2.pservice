package edu.pitt.sis.adapt2.pservice;

//import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Resource;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * @author michael_yudelson
 * The purpose of this interface is to define functionality for visualizing the results of PService work
 */
public interface iPServiceResultVisualizer
{
	/** This method returns the style for the item, e.g. background of a div element
	 * @param _item personaized resource item
	 * @return conten of the style property of the HTML tag
	 */
	public String getItemStyle(Resource _item);

	/** This method returns the annotation for the item, e.g. and icon
	 * @param _item personaized resource item
	 * @param context_path path to the application root
	 * @param _trace performance trace object
	 * @return HTML annotation
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace);
	
	/** This method returns the annotation for the whole channel, e.g. text, or some script to be used in item annotations
	 * @param context_path path to the application root
	 * @param _trace performance trace object
	 * @return HTML annotation
	 */
	public Annotation getChannelAnnotation(String context_path, PerformanceTraceItem _trace);
}
