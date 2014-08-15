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
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * @author michael_yudelson
 *
 */
public class SimADAPT2SocialNavigationPService_PortalVisualizer2_GroupOnly implements
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

		String soc_icon_prefix = 
				context_path + "/assets/icons/";
		String soc_icon_root = "person";
		
		StmtIterator iter = _item.getModel(). listStatements(_item, RDF.type, DCTypes.Collection);
		while (iter.hasNext())
		{
			if(iter.nextStatement().getObject().toString().equals(DCTypes.Collection.toString()))
				soc_icon_root = "folder";
		}
		
		// additional parameters
		String individ_status = "";
		String group_status = "";
		String individ_summary = "";
		String group_summary = "";
		
		// social info
		String icon2 = "";
		String icon2_ind_desc = "";
		String icon2_grp_desc = "";
		String icon2_html = "";
		
		NumberFormat formatter = new DecimalFormat("###");
		
		if(_educ_level<0.0)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "XXX.gif";
			icon2_ind_desc = "<strong>Dotted frame</strong> means there is <strong>no information</strong> about your/group progress with this resource.";
			individ_status = "X";
			individ_summary = "xxx";
		}
		else if(_educ_level>.85)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "100.gif";
			icon2_ind_desc = "<strong>Dark green </strong> color of the person means <strong>great progress</strong> " + 
					"(actual value " + formatter.format(_educ_level * 100) + "%)  with this resource.<br/><br/>";
			individ_status = "4";
			individ_summary = "4/4";
		}
		else if(_educ_level>.5)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "075.gif";
			icon2_ind_desc = "<strong>Darker green</strong> color of the person means <strong>good progress</strong>" + 
					"(actual value " + formatter.format(_educ_level * 100) + "%)  with this resource.<br/><br/>";
			individ_status = "3";
			individ_summary = "3/4";
		}
		else if(_educ_level>.2)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "050.gif";
			icon2_ind_desc = "<strong>Light Green</strong> color of the person means <strong>medium progress</strong>" + 
					"(actual value " + formatter.format(_educ_level * 100) + "%)  with this resource.<br/><br/>";
			individ_status = "2";
			individ_summary = "2/4";
		}
		else if(_educ_level>0.0)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "025.gif";
			icon2_ind_desc = "<strong>Very light Green</strong> color of the person means <strong>fair progress</strong>" + 
					"(actual value " + formatter.format(_educ_level * 100) + "%)  with this resource.<br/><br/>";
			individ_status = "1";
			individ_summary = "1/4";
		}
		else
		{
			icon2 = soc_icon_prefix + soc_icon_root + "000.gif";
			icon2_ind_desc = "<strong>White</strong> color of the person means <strong>minimal to zero progress</strong>" + 
					"(actual value " + formatter.format(_educ_level * 100) + "%)  with this resource.<br/><br/>";
			individ_status = "0";
			individ_summary = "0/4";
		}
		
		String icon2_style = "";
		if(_group_progr<0.0)
		{
			icon2_style = "#ffffff";//"#cccccc";
			icon2_grp_desc = "";
			group_status = "X";
			group_summary = "xxx";
		}
		else if(_group_progr>.85)
		{
			icon2_style = "#4e991f";
			icon2_grp_desc = "<strong>Dark green</strong> color of the background means <strong>good progress of your classmates</strong>" + 
					"(actual value " + formatter.format(_group_progr * 100) + "%)  with this resource.";
			group_status = "4";
			group_summary = "1/4";
		}
		else if(_group_progr>.5)
		{
			icon2_style = "#6dc738";
			icon2_grp_desc = "<strong>Darker Green</strong> color of the background means <strong>medium progress of your classmates</strong>" + 
					"(actual value " + formatter.format(_group_progr * 100) + "%)  with this resource.";
			group_status = "3";
			group_summary = "3/4";
		}
		else if(_group_progr>0.2)
		{
			icon2_style = "#aade8a";
			icon2_grp_desc = "<strong>Light Green</strong> color of the background means <strong>fair progress of your classmates</strong>" + 
					"(actual value " + formatter.format(_group_progr * 100) + "%)  with this resource.";
			group_status = "2";
			group_summary = "2/4";
		}
		else if(_group_progr>0.0)
		{
			icon2_style = "#daf3cb";
			icon2_grp_desc = "<strong>Very light Green</strong> color of the background means <strong>fair progress of your classmates</strong>" + 
					"(actual value " + formatter.format(_group_progr * 100) + "%)  with this resource.";
			group_status = "1";
			group_summary = "1/4";
		}
		else 
		{
			icon2_style = "#ffffff";
			icon2_grp_desc = "<strong>White</strong> color of the background means <strong>minimal to zero progress of your classmates</strong>" + 
					"(actual value " + formatter.format(_group_progr * 100) + "%)  with this resource.";
			group_status = "0";
			group_summary = "0/4";
		}
		
//		icon2_html = "<img src='" + icon2 + "' border='0'/>";
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
        
//        // old way
//		icon2_html = "<span style='background-color:" + icon2_style + ";padding:3px;' ><a " + m_in_out + "><img style='vertical-align:text-bottom;' src='" + icon2 + "' border='0'/></a></span><span style='display:none' id='textToolTip" + n_rand + "'>" + icon2_ind_desc + icon2_grp_desc + "</span>";
        // just group
		icon2_html = "<span style='background-color:" + icon2_style + ";padding:3px;' ><a " + m_in_out + "></a></span><span style='display:none' id='textToolTip" + n_rand + "'>"  + icon2_grp_desc + "</span>";
		
		
//		String style = " bgcolor='" + icon2_style + "'";
		
		//Update the link
		String _link_prop = _item.getProperty(RSS.link).getString();
		_item = _item.removeAll(RSS.link);
		_item = _item.addProperty(RSS.link, _link_prop + "&amp;svc=" + trace_identifier);
		
//		return new Annotation(icon2_html, style, "user=" + individ_summary + "\tgroup=" + group_summary);
		return new Annotation(icon2_html, "", "user=" + individ_summary + "\tgroup=" + group_summary);
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
