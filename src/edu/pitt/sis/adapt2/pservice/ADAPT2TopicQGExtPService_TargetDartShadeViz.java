/**
 * 
 */
package edu.pitt.sis.adapt2.pservice;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * @author michael_yudelson
 *
 */
public class ADAPT2TopicQGExtPService_TargetDartShadeViz implements iPServiceResultVisualizer
{
//	private static final String ON_MOUSE_IN = "";
//	private static final String ON_MOUSE_OUT = "";

	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemAnnotation(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace)
	{
		Statement res_educ_level =  _item.getProperty(DCTerms.educationLevel);// + url_suffix;
		Statement res_extent =  _item.getProperty(DCTerms.extent);// + url_suffix;
		double _educ_level = (res_educ_level!=null && res_educ_level.getString().length() != 0)?Double.parseDouble(res_educ_level.getString()):-1;
		double _extent = (res_extent!=null && res_extent.getString().length() != 0)?Double.parseDouble(res_extent.getString()):-1;

		String soc_icon_prefix = 
				context_path + "/assets/icons/targetc_";
		
		// additional parameters
		String individ_status = "";
		
		// social info
		String icon = "";
		// 100 superb coverage, 75 great, 50 good, 25 some , 00 minimal, xxx no
		/*
		<strong>Dark green</strong> color of the target means <strong>superb coverage</strong> (actual value 100%) of examples in this folder<br/><br/>
		<strong>Darker green</strong> color of the target means <strong>great coverage</strong> (actual value 75%) of examples in this folder<br/><br/>
		<strong>Light green</strong> color of the target means <strong>good coverage</strong> (actual value 50%) of examples in this folder<br/><br/>
		<strong>Very light green</strong> color of the target means <strong>fair coverage</strong> (actual value 25%) of examples in this folder<br/><br/>
		<strong>Grey dotted</strong> pattern of the target means <strong>minimal to zero coverage</strong> (actual value 00%) of examples in this folder<br/><br/>

		<strong>3 darts</strong> in the target mean <strong>great progress</strong> (actual value  3%) with quizzes in this folder
		<strong>2 darts</strong> in the target mean <strong>good progress</strong> (actual value  2%) with quizzes in this folder
		<strong>1 darts</strong> in the target mean <strong>some progress</strong> (actual value  1%) with quizzes in this folder
		<strong>no darts</strong> in the target mean <strong>minimal to zero progress</strong> (actual value  0%) with quizzes in this folder
		 */
		String icon_descEG = "";
		String icon_descQZ = "";
		String icon_html = "";
		NumberFormat formatter = new DecimalFormat("###");

		// Then example progress
		if(_extent>.875)
		{
			icon = soc_icon_prefix +  "100";
			individ_status += "ex~100";
			icon_descEG = "<strong>Dark green</strong> color of the target means <strong>superb coverage</strong>" +
					" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
		}
		else if(_extent>.625)
		{
			icon = soc_icon_prefix +  "075";
			individ_status += "ex~075";
			icon_descEG = "<strong>Darker green</strong> color of the target means <strong>great coverage</strong>" +
			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
		}
		else if(_extent>.375)
		{
			icon = soc_icon_prefix +  "050";
			individ_status += "ex~050";;
			icon_descEG = "<strong>Light green</strong> color of the target means <strong>good coverage</strong>" +
			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
		}
		else if(_extent>.125)
		{
			icon = soc_icon_prefix +  "025";
			individ_status += "ex~025";
			icon_descEG = "<strong>Very light green</strong> color of the target means <strong>fair coverage</strong>" +
			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
		}
		else
		{
			icon = soc_icon_prefix +  "000";
			individ_status += "ex~000";
			icon_descEG = "<strong>Grey dotted</strong> pattern of the target means <strong>minimal to zero coverage</strong>" +
			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
		}

		// First quiz progress
		if(_educ_level>0.84)
		{
			icon += "_3.gif";
			individ_status += ",quiz~3/3";
			icon_descQZ = "<strong>3 darts</strong> in the target mean <strong>great progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		else if(_educ_level>.50)
		{
			icon+= "_2.gif";
			individ_status += ",quiz~2/3";
			icon_descQZ = "<strong>2 darts</strong> in the target mean <strong>good progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		else if(_educ_level>.17)
		{
			icon += "_1.gif";
			individ_status += ",quiz~1/3";
			icon_descQZ = "<strong>1 dart</strong> in the target mean <strong>some progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		else
		{
			icon += "_0.gif";
			individ_status += ",quiz~0/3";
			icon_descQZ = "<strong>no darts</strong> in the target mean <strong>minimal to zero progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}


//		icon_html = "<img src='" + icon + "' border='0'/>";
		Random rand = new Random();
		int n_rand = rand.nextInt();
		String m_in_out = "";
		try
		{
			m_in_out = "onmouseout='UnTip();finish_ts = new Date();sndTimeUpd(finish_ts.getTime()-start_ts.getTime(),\"" + URLEncoder.encode(_item.toString().replaceAll("&", "&amp;"),"UTF-8") + "\",\"textToolTip" + n_rand + "\");' " + 
					"onmouseover=\"TagToTip('textToolTip" + n_rand + "',DELAY,100,BGCOLOR,'#FFFF99',BORDERCOLOR,'#666666',FOLLOWMOUSE,false,BORDERSTYLE,'dotted',WIDTH,200,TEXTALIGN,'justify');start_ts = new Date();\" href=\"#\"";
		}
		catch(UnsupportedEncodingException uee){ uee.printStackTrace(System.out);}
		
		String trace_identifier = "{pservice::" + _trace.pservice_rdfid + "," + _trace.st + "," + _trace.user_group + "}";
		icon_html = "<a " + m_in_out + "><img src='" + icon + "' border='0'/></a><span style='display:none' id='textToolTip" + n_rand + "'>" + icon_descEG + icon_descQZ + "</span>";
		
		//Update the link
		String _link_prop = _item.getProperty(RSS.link).getString();
		_item.removeAll(RSS.link);
		_item.addProperty(RSS.link, _link_prop+"&amp;svc=" + trace_identifier);
		
		return new Annotation(icon_html, "", "user=" + individ_status);
	
	}

	public Annotation getChannelAnnotation(String context_path, PerformanceTraceItem _trace)
	{
		String script_html = 
			"<script src=\"" + context_path + "/assets/wz_tooltip.js\"></script>\n" +
			"<script src=\"" + context_path + "/assets/prototype.js\"></script>\n" +
			"<script language=\"JavaScript\" type=\"text/javascript\">\n" +
			"var start_ts;\n" +
			"var finish_ts;\n" +
			"function sndTimeUpd(time, uri, annot_id)\n" +
			"{ \n" +
			"	var params = $H({t:time, r:'" + _trace.st + "',s:'"+ _trace.pservice_rdfid + "',u:'" +  _trace.user_group + "',id:uri,a:$(annot_id).innerHTML});\n" +
			"	new Ajax.Request(\n" +
			"		'" + context_path + "/annotexplore', \n" +
			"		{ \n" +
			"			method: \"post\", \n" +
			"			parameters: params\n" +
			" 		} \n" +
			"	); \n" +
			"}\n" +
			"</script>\n";
		return new Annotation(script_html,"","");
	}
	
	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemStyle(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public String getItemStyle(Resource _item)
	{
		return "";
	}

}
