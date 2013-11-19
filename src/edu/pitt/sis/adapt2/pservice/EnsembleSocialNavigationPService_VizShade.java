/**
 * 
 */
package edu.pitt.sis.adapt2.pservice;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.StringTokenizer;
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
public class EnsembleSocialNavigationPService_VizShade implements
		iPServiceResultVisualizer
{

	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemAnnotation(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace)
	{
//		System.out.println("@@");
		Statement res_educ_level =  _item.getProperty(DCTerms.educationLevel);// + url_suffix;
		Statement res_audience =  _item.getProperty(DCTerms.audience);// + url_suffix;
		double _educ_level = (res_educ_level!=null && res_educ_level.getString().length() != 0)?Double.parseDouble(res_educ_level.getString()):-1;

		String _audience = (res_audience!=null && res_audience.getString().length() != 0)?res_audience.getString():"";

//		// parse limits of the adaptation
//		StringTokenizer st = new StringTokenizer(_audience, "*");
		double val1 = 0;
		double val2 = 0;
//		while(st.hasMoreTokens())
//		{// for all tokens
//			val2 = val1;
//			String tk = st.nextToken();
//			try
//			{
//				val1 = Double.parseDouble(tk);
//			}
//			catch(NumberFormatException nfe) { nfe.printStackTrace(System.out); };
//
//		}// -- for all tokens
//		if(val1>val2)
//		{
//			double buf = val1;
//			val1 = val2;
//			val2 = buf;
//		}
		
//		System.out.println("~~~ res=" + _item.toString() + " val1=" + val1 + " val2=" + val2);
		
		val1 = 7;
		val2 = 15;

		
		String soc_icon_prefix = 
				context_path + "/assets/icons/";
		String soc_icon_root = "bullet_";
		
		StmtIterator iter = _item.getModel(). listStatements(_item, RDF.type, DCTypes.Collection);
		while (iter.hasNext())
		{
			if(iter.nextStatement().getObject().toString().equals(DCTypes.Collection.toString()))
				soc_icon_root = "folder";
		}
		
		// additional parameters
		String individ_status = "";
		String individ_summary = "";
		
		// social info
		String icon2 = "";
		String icon2_ind_desc = "";
		String icon2_html = "";
		
		NumberFormat formatter = new DecimalFormat("###");
		
		if(_educ_level<0)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "x.gif";
//			icon2_ind_desc = "<strong>Dotted Frame</strong> and <strong>grey background</strong> means <strong>no information</strong> is available for this resource.";
			icon2_ind_desc = "This dotted frame and grey background means no information is available for this resource.";
			individ_status = "x";
			individ_summary = "x";
		}
		else if(_educ_level>val2)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "4.gif";
//			icon2_ind_desc = "<strong>Dark Green </strong> shade means <strong>great progress</strong> " + 
//				"(" + formatter.format(_educ_level) + " interactions with this resource.)";
			icon2_ind_desc = "This dark green shade means a great amount of click traffic " + 
				"(" + formatter.format(_educ_level) + " clicks.)";
			individ_status = "4";
			individ_summary = "4/4";
		}
		else if(_educ_level>( val1 + (val2-val1)/2 ))
		{
			icon2 = soc_icon_prefix + soc_icon_root + "3.gif";
//			icon2_ind_desc = "<strong>Darker Green</strong> shade means <strong>good progress</strong> " + 
//				"(" + formatter.format(_educ_level) + " clicks.)";
			icon2_ind_desc = "This darker green shade indicates a good amount of click traffic " + 
				"(" + formatter.format(_educ_level) + " clicks.)";
			individ_status = "3";
			individ_summary = "3/4";
		}
		else if(_educ_level>val1)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "2.gif";
//			icon2_ind_desc = "<strong>Light Green</strong> shade indicates <strong>medium progress</strong> " + 
//				"(" + formatter.format(_educ_level) + " clicks.)";
			icon2_ind_desc = "This  light green shade indicates a medium amount of click traffic " + 
				"(" + formatter.format(_educ_level) + " clicks.)";
			individ_status = "2";
			individ_summary = "2/4";
		}
		else if(_educ_level>0)
		{
			icon2 = soc_icon_prefix + soc_icon_root + "1.gif";
//			icon2_ind_desc = "<strong>Very Light Green</strong> shade indicates <strong>fair progress</strong> " + 
//				"(" + formatter.format(_educ_level) + " clicks.)";
			icon2_ind_desc = "This very light green shade indicates a fair amount of click traffic " + 
				"(" + formatter.format(_educ_level) + " clicks.)";
			individ_status = "1";
			individ_summary = "1/4";
		}
		else
		{
			icon2 = soc_icon_prefix + soc_icon_root + "0.gif";
//			icon2_ind_desc = "<strong>White</strong> shade indicates <strong>minimal to zero progress</strong> " + 
//				"(" + formatter.format(_educ_level) + " clicks.)";
			icon2_ind_desc = "This white shade indicates a minimal amount of click traffic " + 
				"(" + formatter.format(_educ_level) + " clicks.)";
			individ_status = "0";
			individ_summary = "0/4";
		}
				
//		icon2_html = "<img src='" + icon2 + "' border='0'/>";
		Random rand = new Random();
		int n_rand = rand.nextInt();
		String m_in_out = "";
//		try
//		{
//			m_in_out = "onmouseout='UnTip();finish_ts = new Date();sndTimeUpd(finish_ts.getTime()-start_ts.getTime(),\"" + URLEncoder.encode(_item.toString().replaceAll("&", "&amp;"),"UTF-8") + "\",\"textToolTip" + n_rand + "\");' " + 
//					"onmouseover=\"TagToTip('textToolTip" + n_rand + "',DELAY,100,BGCOLOR,'#FFFF99',BORDERCOLOR,'#666666',FOLLOWMOUSE,false,BORDERSTYLE,'dotted',WIDTH,200,TEXTALIGN,'justify');start_ts = new Date();\" href=\"#\"";
//		}
//		catch(UnsupportedEncodingException uee){ uee.printStackTrace(System.out);}
		
		String trace_identifier = "{pservice::" + _trace.pservice_rdfid + "," + _trace.st + "," + _trace.user_group + "}";
//		icon2_html = "<span style='padding:3px;' ><a " + m_in_out + "><img style='vertical-align:text-bottom;' src='" + icon2 + "' border='0'/></a></span><span style='display:none' id='textToolTip" + n_rand + "'>" + icon2_ind_desc + "</span>";
		icon2_html = "<span style='padding:3px;' ><img style='vertical-align:text-bottom;' src='" + icon2 + "' border='0' title='" + icon2_ind_desc + "'/></span>";
		
		
		//Update the link
		String _link_prop = _item.getProperty(RSS.link).getString();
		_item.removeAll(RSS.link);
		_item.addProperty(RSS.link, _link_prop+"&amp;svc=" + trace_identifier);
		
//		return new Annotation(icon2_html, style, "user=" + individ_summary + "\tgroup=" + group_summary);
		return new Annotation(icon2_html, "", "user=" + individ_summary);
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
