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
public class ADAPT2ConceptBasedPService_NavexStyleViz implements
		iPServiceResultVisualizer
{

	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemAnnotation(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace)
	{
		Statement res_educ_level =  _item.getProperty(DCTerms.educationLevel);// + url_suffix;
		double _educ_level = (res_educ_level!=null && res_educ_level.getString().length() != 0)?Double.parseDouble(res_educ_level.getString()):-1;

		String soc_icon_prefix = 
				context_path + "/assets/icons/bullet_";
		
		String status = "";
		
		// social info
		String icon = "";
//		String icon_title = "";
		String icon_html = "";
		String icon_desc = "";
		NumberFormat formatter = new DecimalFormat("###");
		
		if(_educ_level==-1)
		{
			icon = soc_icon_prefix + "progressXXX";
			status = "progress~xxx,";
			icon_desc = "Your progress with this resource is not being tracked.";
		}
		else if(_educ_level>.875)
		{
			icon = soc_icon_prefix + "progress100";
			status = "progress~100,";
			icon_desc = "Your progress with this resource is <strong>great</strong>(actual value " + formatter.format(_educ_level * 100) + "%).";
		}
		else if(_educ_level>.625)
		{
			icon = soc_icon_prefix + "progress075";
			status = "progress~075,";
			icon_desc = "Your progress with this resource is <strong>good</strong>(actual value " + formatter.format(_educ_level * 100) + "%).";
		}
		else if(_educ_level>.375)
		{
			icon = soc_icon_prefix + "progress050";
			status = "progress~050,";
			icon_desc = "Your progress with this resource is <strong>getting better</strong>(actual value " + formatter.format(_educ_level * 100) + "%).";
		}
		else if(_educ_level>.125)
		{
			icon = soc_icon_prefix + "progress025";
			status = "progress~025,";
			icon_desc = "Your progress with this resource is <strong>fair</strong>(actual value " + formatter.format(_educ_level * 100) + "%).";
		}
		else if(_educ_level>=0)
		{
			icon = soc_icon_prefix  + "progress000";
			status = "progress~000,";
			icon_desc = "Your progress with this resource is <strong>zero to minimal</strong>(actual value " + formatter.format(_educ_level * 100) + "%).";
		}
		else
		{
			icon = soc_icon_prefix  + "bar_discourage";
			status = "progress~BAN,";
			icon_desc = "Your are <strong>not ready</strong> to work with this resource yet.";
		}
		
		Random rand = new Random();
		int n_rand = rand.nextInt();
		String m_in_out = "";
		try
		{
			m_in_out = "onmouseout='UnTip();finish_ts = new Date();sndTimeUpd(finish_ts.getTime()-start_ts.getTime(),\"" + URLEncoder.encode(_item.toString().replaceAll("&", "&amp;"),"UTF-8") + "\",\"textToolTip" + n_rand + "\");' " + 
					"onmouseover=\"TagToTip('textToolTip" + n_rand + "',DELAY,100,BGCOLOR,'#FFFF99',BORDERCOLOR,'#666666',FOLLOWMOUSE,false,BORDERSTYLE,'dotted',WIDTH,200,TEXTALIGN,'justify');start_ts = new Date();\" href=\"#\"";
		}
		catch(UnsupportedEncodingException uee){ uee.printStackTrace(System.out);}

//		icon_html = "<img src='" + icon + ".gif' border='0'/>";
		String trace_identifier = "{pservice::" + _trace.pservice_rdfid + "," + _trace.st + "," + _trace.user_group + "}";
		icon_html = "<a " + m_in_out + "><img src='" + icon + ".gif' border='0'/></a><span style='display:none' id='textToolTip" + n_rand + "'>" + icon_desc + "</span>";

		
		//Update the link
		String _link_prop = _item.getProperty(RSS.link).getString();
		_item.removeAll(RSS.link);
		_item.addProperty(RSS.link, _link_prop+"&amp;svc=" + trace_identifier);
		
		return new Annotation(icon_html, "", "user=" + status);
	
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
