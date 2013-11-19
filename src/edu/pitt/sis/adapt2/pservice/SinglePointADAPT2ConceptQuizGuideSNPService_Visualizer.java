/**
 * 
 */
package edu.pitt.sis.adapt2.pservice;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * @author michael_yudelson
 *
 */
public class SinglePointADAPT2ConceptQuizGuideSNPService_Visualizer implements
		iPServiceResultVisualizer
{

	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemAnnotation(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace)
	{
//		System.out.println("@@");
		Statement res_educ_level =  _item.getProperty(DCTerms.educationLevel);// + url_suffix;
		Statement res_group_progr =  _item.getProperty(DCTerms.audience);// + url_suffix;
		double _educ_level = (res_educ_level!=null && res_educ_level.getString().length() != 0)?Double.parseDouble(res_educ_level.getString()):-1;
		double _group_progr = (res_group_progr!=null && res_group_progr.getString().length() != 0)?Double.parseDouble(res_group_progr.getString()):-1;
		String _educ_level_s = Math.round(_educ_level*100) + "%";
		String _group_progr_s = Math.round(_group_progr*100) + "%";
		
		String color = (_educ_level<_group_progr)?"red":"green";
		
		String result = "<div><strong style='font-size:1.2em;color:" + color + ";'>Your overall progress with course tools is " + _educ_level_s + 
			".</strong></div>";// +" Progress with course tools of the rest of the class is " + _group_progr_s + ".</strong>";
//		"%. You have made " + Math.round(sum_indiv_count) + " learning attemts while group an average of " + Math.round(sum_group_count) + " learning attemts</strong>;
		return new Annotation(result, "", "");
	}

	public Annotation getChannelAnnotation(String context_path, PerformanceTraceItem _trace)
	{
		return new Annotation();
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemStyle(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public String getItemStyle(Resource _item)
	{
		return "";
	}

}
