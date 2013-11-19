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
public class ADAPT2TopicQGPService_TargetDartViz implements
		iPServiceResultVisualizer
{

	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemAnnotation(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace)
	{
		Statement res_educ_level =  _item.getProperty(DCTerms.educationLevel);// + url_suffix;
//		Statement res_group_progr =  _item.getProperty(DCTerms.audience);// + url_suffix;
		double _educ_level = (res_educ_level!=null && res_educ_level.getString().length() != 0)?Double.parseDouble(res_educ_level.getString()):-1;
//		double _group_progr = (res_group_progr!=null && res_group_progr.getString().length() != 0)?Double.parseDouble(res_group_progr.getString()):-1;

		String soc_icon_prefix = 
				context_path + "/assets/icons/";
		String soc_icon_root = "targeta_";
		
//		StmtIterator iter = _item.getModel(). listStatements(_item, RDF.type, DCTypes.Collection);
//		while (iter.hasNext())
//		{
//			if(iter.nextStatement().getObject().toString().equals(DCTypes.Collection.toString()))
//				soc_icon_root = "folder";
//		}
		
		// additional parameters
		String individ_status = "";
		String group_status = "";
		
		// social info
		String icon2 = "";
		String icon2_desc = "";
		String icon2_html = "";
		NumberFormat formatter = new DecimalFormat("###");
		
		if(_educ_level>0.84)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "3.gif";
			individ_status = "3/3";
			icon2_desc = "<strong>3 darts</strong> in the target mean <strong>great progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		else if(_educ_level>.50)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "2.gif";
			individ_status = "2/3";
			icon2_desc = "<strong>2 darts</strong> in the target mean <strong>good progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		else if(_educ_level>.17)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "1.gif";
			individ_status = "1/3";
			icon2_desc = "<strong>1 dart</strong> in the target mean <strong>some progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		else
		{
			icon2 = soc_icon_prefix + soc_icon_root + "0.gif";
			individ_status = "0/3";
			icon2_desc = "<strong>no darts</strong> in the target mean <strong>minimal to zero progress</strong>" +
					" (actual value " + formatter.format(_educ_level * 100) + "%) with quizzes in this folder";
		}
		
//		String icon2_style = "";
//		if(_group_progr<0.0)
//		{
//			icon2_style = "#ffffff";//"#cccccc";
////			icon2_title = "There is no information about group progress";
//			group_status = "X";
//		}
//		else if(_group_progr>.85)
//		{
//			icon2_style = "#4e991f";
////			icon2_title = "Group progress is high";
//			group_status = "4";
//		}
//		else if(_group_progr>.5)
//		{
//			icon2_style = "#6dc738";
////			icon2_title = "Group progress is medium";
//			group_status = "3";
//		}
//		else if(_group_progr>0.2)
//		{
//			icon2_style = "#aade8a";
////			icon2_title = "Group progress is fair";
//			group_status = "2";
//		}
//		else if(_group_progr>0.0)
//		{
//			icon2_style = "#daf3cb";
////			icon2_title = "Group progress is fair";
//			group_status = "1";
//		}
//		else 
//		{
//			icon2_style = "#ffffff";
////			icon2_title = "Group progress is zero";
//			group_status = "0";
//		}
		
//		icon2_html = "<td bgcolor='" + icon2_style + "'><img src='" + icon2 + "' border='0'/></td>";
//		icon2_html = "<td><img src='" + icon2 + "' border='0'/></td>";
		
		
		
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
		icon2_html = "<a " + m_in_out + "><img src='" + icon2 + "' border='0'/></a><span style='display:none' id='textToolTip" + n_rand + "'>" + icon2_desc + "</span>";

		
//		// icon
//		icon2_html = "<img src='" + icon2 + "' border='0'/>";
//		
		//Update the link
		String _link_prop = _item.getProperty(RSS.link).getString();
		_item.removeAll(RSS.link);
		_item.addProperty(RSS.link, _link_prop+"&amp;svc=" + trace_identifier);
		
		return new Annotation(icon2_html, "", "user=" + individ_status);
	
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
