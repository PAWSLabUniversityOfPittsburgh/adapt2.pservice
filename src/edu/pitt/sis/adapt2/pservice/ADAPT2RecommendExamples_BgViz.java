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
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * @author michael_yudelson
 *
 */
public class ADAPT2RecommendExamples_BgViz implements iPServiceResultVisualizer
{
//	private static final String ON_MOUSE_IN = "";
//	private static final String ON_MOUSE_OUT = "";

	/* (non-Javadoc)
	 * @see edu.pitt.sis.adapt2.pservice.iPServiceResultVisualizer#getItemAnnotation(com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Annotation getItemAnnotation(Resource _item, String context_path, PerformanceTraceItem _trace)
	{
//		
//		Statement res_educ_level =  _item.getProperty(DCTerms.educationLevel);// + url_suffix;
//		Statement res_extent =  _item.getProperty(DCTerms.extent);// + url_suffix;
//		double _educ_level = (res_educ_level!=null && res_educ_level.getString().length() != 0)?Double.parseDouble(res_educ_level.getString()):-1;
//		double _extent = (res_extent!=null && res_extent.getString().length() != 0)?Double.parseDouble(res_extent.getString()):-1;
//
//		String soc_icon_prefix = 
//				context_path + "/assets/icons/targetc_";
//		
//		// additional parameters
//		String individ_status = "";
//		
//		// social info
//		String icon = "";
//		// 100 superb coverage, 75 great, 50 good, 25 some , 00 minimal, xxx no
//		/*
//		<strong>Dark green</strong> color of the target means <strong>superb coverage</strong> (actual value 100%) of examples in this folder<br/><br/>
//		<strong>Darker green</strong> color of the target means <strong>great coverage</strong> (actual value 75%) of examples in this folder<br/><br/>
//		<strong>Light green</strong> color of the target means <strong>good coverage</strong> (actual value 50%) of examples in this folder<br/><br/>
//		<strong>Very light green</strong> color of the target means <strong>fair coverage</strong> (actual value 25%) of examples in this folder<br/><br/>
//		<strong>Grey dotted</strong> pattern of the target means <strong>minimal to zero coverage</strong> (actual value 00%) of examples in this folder<br/><br/>
//
//		<strong>3 darts</strong> in the target mean <strong>great progress</strong> (actual value  3%) with quizzes in this folder
//		<strong>2 darts</strong> in the target mean <strong>good progress</strong> (actual value  2%) with quizzes in this folder
//		<strong>1 darts</strong> in the target mean <strong>some progress</strong> (actual value  1%) with quizzes in this folder
//		<strong>no darts</strong> in the target mean <strong>minimal to zero progress</strong> (actual value  0%) with quizzes in this folder
//		 */
//		String icon_descEG = "";
//		String icon_descQZ = "";
//		String icon_html = "";
//		NumberFormat formatter = new DecimalFormat("###");
//
//		// Then example progress
//		if(_res_value>.875)
//		{
//			icon = soc_icon_prefix +  "100";
//			individ_status += "ex~100";
//			icon_descEG = "<strong>Dark green</strong> color of the target means <strong>superb coverage</strong>" +
//					" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
//		}
//		else if(_res_value>.625)
//		{
//			icon = soc_icon_prefix +  "075";
//			individ_status += "ex~075";
//			icon_descEG = "<strong>Darker green</strong> color of the target means <strong>great coverage</strong>" +
//			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
//		}
//		else if(_res_value>.375)
//		{
//			icon = soc_icon_prefix +  "050";
//			individ_status += "ex~050";;
//			icon_descEG = "<strong>Light green</strong> color of the target means <strong>good coverage</strong>" +
//			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
//		}
//		else if(_res_value>.125)
//		{
//			icon = soc_icon_prefix +  "025";
//			individ_status += "ex~025";
//			icon_descEG = "<strong>Very light green</strong> color of the target means <strong>fair coverage</strong>" +
//			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
//		}
//		else
//		{
//			icon = soc_icon_prefix +  "000";
//			individ_status += "ex~000";
//			icon_descEG = "<strong>Grey dotted</strong> pattern of the target means <strong>minimal to zero coverage</strong>" +
//			" (actual value " + formatter.format(_extent * 100) + "%) of examples in this folder<br/><br/>";
//		}
//
//		
//		String trace_identifier = "{pservice::" + _trace.pservice_rdfid + "," + _trace.st + "," + _trace.user_group + "}";
//		icon_html = "<a " + m_in_out + "><img src='" + icon + "' border='0'/></a><span style='display:none' id='textToolTip" + n_rand + "'>" + icon_descEG + icon_descQZ + "</span>";
//		
//		//Update the link
//		String _link_prop = _item.getProperty(RSS.link).getString();
//		_item.removeAll(RSS.link);
//		_item.addProperty(RSS.link, _link_prop+"&amp;svc=" + trace_identifier);
//		
//		return new Annotation(icon_html, "", "user=" + individ_status);

		Statement res_value = _item.getProperty(RDF.value);
//System.out.println("value = " + res_value.getString());		
		Statement res_relation = _item.getProperty(DC.relation);
//System.out.println("type = " + res_relation.getString());		
		double _res_value = (res_value!=null && res_value.getString().length() != 0)?res_value.getDouble():-1;
		String _res_relation = (res_relation!=null && res_relation.getString().length() != 0)?res_relation.getString():"";
		
		String icon_prefix = context_path + "/assets/icons/recommended_";
		String icon = "";
		String style = "";
		String status = "";
		String summary = "";
		String icon_desc = "";
		NumberFormat formatter = new DecimalFormat("###");
		
		if(_res_relation.equals("recommended"))
		{
			
			if(_res_value>.875)
			{
				icon = "100.gif";
				status = "100";
				style = "background-color:#4E991F;";
				icon_desc = "<strong>Recommended example. Dark blue</strong> background means <strong>very high relevance</strong>" +
					" (" + formatter.format(_res_value * 100) + "%)<br/>"; // actual value // to the resources in this folder
			}
			else if(_res_value>.625)
			{
				icon = "075.gif";
				status = "075";
				style = "background-color:#6DC738;";
				icon_desc = "<strong>Recommended example. Darker blue</strong> background means <strong>high relevance</strong>" +
					" (" + formatter.format(_res_value * 100) + "%)<br/>";
			}
			else if(_res_value>.375)
			{
				icon = "050.gif";
				status = "050";
				style = "background-color:#AADE8A;";
				icon_desc = "<strong>Recommended example. Light blue</strong> background means <strong>medium relevance</strong>" +
					" (" + formatter.format(_res_value * 100) + "%)<br/>";
			}
			else if(_res_value>.125)
			{
				icon = "025.gif";
				status = "025";
				style = "background-color:#DAF3CB;";
				icon_desc = "<strong>Recommended example. Very blue</strong> background means <strong>fair relevance</strong>" +
					" (" + formatter.format(_res_value * 100) + "%)<br/>";
			}
			else
			{
				icon = "000.gif";
				status = "000";
				style = "background-color:#DDDDDD;";
				icon_desc = "<strong>Recommended example. White</strong> background means <strong>minimal relevance</strong>" +
					" (" + formatter.format(_res_value * 100) + "%)<br/>";
			}

		}
		
		// if it is bookmarked by this user
		Statement res_is_bookmarked = _item.getProperty(DC.rights);
//System.out.println("rights = " + res_is_bookmarked.toString());		
		boolean is_bookmarked = (res_is_bookmarked!=null && res_is_bookmarked.getString().length() != 0)?res_is_bookmarked.getBoolean():false;
		
		// get channel uri
		StmtIterator ch_iter = _item.getModel().listStatements(null, RDF.type, RSS.channel);
		Resource channel = null;
		if (ch_iter.hasNext())
		{
			channel = ch_iter.nextStatement().getSubject();
		}
		
		Random rand = new Random();
		int n_rand = rand.nextInt();
		String m_in_out = "";
		try
		{
			m_in_out = "onmouseout='UnTip();finish_ts = new Date();sndTimeUpd(finish_ts.getTime()-start_ts.getTime(),\"" + URLEncoder.encode(_item.toString().replaceAll("&", "&amp;"),"UTF-8") + "\",\"textToolTip" + n_rand + "\");'" + 
					" onmouseover=\"TagToTip('textToolTip" + n_rand + "',DELAY,100,BGCOLOR,'#FFFF99',BORDERCOLOR,'#666666',FOLLOWMOUSE,false,BORDERSTYLE,'dotted',WIDTH,200,TEXTALIGN,'justify');start_ts = new Date();\" " +
					" onclick='flipBookMark(this," + n_rand + ",\"" + URLEncoder.encode(channel.toString().replaceAll("&", "&amp;"),"UTF-8") + "\",\"" + URLEncoder.encode(_item.toString().replaceAll("&", "&amp;"),"UTF-8") + "\");'" +
					" href=\"javascript:void(0);\"";
		}
		catch(UnsupportedEncodingException uee){ uee.printStackTrace(System.out);}
		String trace_identifier = "{pservice::" + _trace.pservice_rdfid + "," + _trace.st + "," + _trace.user_group + "}";
		String icon_hmlt = "<a name='" + ((is_bookmarked)?1:0) + "' " + m_in_out + ">" + 
			"<img id='recommend" + n_rand + "-bookm-me' style='display:" + ((is_bookmarked)?"inline":"none") + "' src='" + icon_prefix + icon.replaceAll("\\.gif", "_bookm_me\\.gif") + "' border='0'/>" +
			"<img id='recommend" + n_rand + "-bookm-ow' style='display:" + ((is_bookmarked)?"none":"inline") + "' src='" + icon_prefix + icon + "' border='0'/>" +
			"</a>" +
			"<span style='display:none' id='textToolTip" + n_rand + ((is_bookmarked)?"-ow":"") + "'>" + icon_desc + "<br/><strong>Click to bookmark this link</strong></span>" +
			"<span style='display:none' id='textToolTip" + n_rand + ((is_bookmarked)?"":"-ow") + "'>" + icon_desc + "<br/><strong>Yellow star</strong> means the link is <strong>bookmarked</strong>. Click to un-bookmark it</span>";
		
		String style_html = "background-color:#D7E8FE;"; //((is_bookmarked)?"font-weight:bold;":"");
		
		//Update the link
		String _link_prop = _item.getProperty(RSS.link).getString();
		_item.removeAll(RSS.link);
		_item.addProperty(RSS.link, _link_prop+"&amp;svc=" + trace_identifier);
		
		summary = "val:" + _res_value + ",ic:" + status + ",bkm:" + ((is_bookmarked)?"y":"n");
		
		return new Annotation(icon_hmlt, style_html, summary);
	
	}

	public Annotation getChannelAnnotation(String context_path, PerformanceTraceItem _trace)
	{
		StringTokenizer st = new StringTokenizer(_trace.user_group, ":");
		String user = st.nextToken();
		String group = st.nextToken();
			
		String script_html = 
			"<script src=\"" + context_path + "/assets/wz_tooltip.js\"></script>\n" +
			"<script src=\"" + context_path + "/assets/prototype.js\"></script>\n" +
			"<script language='javascript' type=\"text/javascript\">\n" +
			"function flipBookMark(el, id, puri, uri)\n" +
			"{\n" +
			"	if(el.name=='0')\n" +
			"	{\n" +
			"		$('recommend'+id+'-bookm-me').style.display = 'inline';\n" +
			"		$('recommend'+id+'-bookm-ow').style.display = 'none';\n" +
			"		// show 'you bookmarked too'\n" +
			"		oldTT = $('textToolTip'+id);\n" +
			"		newTT = $('textToolTip'+id+'-ow');\n" +
			"		oldTT.id += '-ow';\n" +
			"		newTT.id = newTT.id.substring(0,newTT.id.length-3);\n" +
			"		el.name = '1';\n" +
			"		reportBookmark(puri, uri, '1');\n" +
			"	}\n" +
			"	else if(el.name=='1')\n" +
			"	{\n" +
			"		$('recommend'+id+'-bookm-me').style.display = 'none';\n" +
			"		$('recommend'+id+'-bookm-ow').style.display = 'inline';\n" +
			"		// hide 'you bookmarked too'\n" +
			"		oldTT = $('textToolTip'+id);\n" +
			"		newTT = $('textToolTip'+id+'-ow');\n" +
			"		oldTT.id += '-ow';\n" +
			"		newTT.id = newTT.id.substring(0,newTT.id.length-3);\n" +		
			"		el.name = '0';\n" +
			"		reportBookmark(puri, uri,'0');\n" +
			"	}\n" +
			"}\n" +
			"function reportBookmark(a_puri, a_uri,a_flag)\n" +
			"{\n" +
			"	var a_ts = new Date();\n" +
			"	var params = $H({token:'" + _trace.st + "', usr:'" + user + "', grp:'" + group + "', puri:a_puri, uri:a_uri, flg:a_flag, tm:a_ts.getTime()});\n" +
			"	new Ajax.Request(\n" +
			"			'http://adapt2.sis.pitt.edu/netex/GetBookmarks',\n" + //http://adapt2.sis.pitt.edu/pservice/TestServlet // 
			"			{ \n" +
			"				method: \"get\",\n" + 
			"				parameters: params\n" +
			"	 		}\n" +
			"		);\n" +
			"}\n" +
			"</script>\n" +
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
