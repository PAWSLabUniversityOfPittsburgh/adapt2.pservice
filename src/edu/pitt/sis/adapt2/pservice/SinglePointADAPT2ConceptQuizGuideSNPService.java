package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorReport;

public class SinglePointADAPT2ConceptQuizGuideSNPService extends PService
{
	// Constants
//	public String ADAPT2_CUMULATE_REPORTMANAGER = //"http://localhost:8080/cbum/ReportManager?typ=act&dir=out&frm=dat&app=23";
////		"http://adapt2.sis.pitt.edu/cbum/ReportManager?typ=act&dir=out&frm=dat&app=23";
//		"http://adapt2.sis.pitt.edu/cbum/ReportManager?typ=con&dir=out&frm=dat&app=23&dom=sql_topics&lev=application";
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace) throws IOException, URISyntaxException
	{
//		String _rdf = _params.get(PSERVICE_PARAM_RDF);
		String _uri = _params.get(PSERVICE_PARAM_URI);

		Model result = ModelFactory.createDefaultModel(); 
		
		InputStream in;
		URI checking_uri = new URI(_uri);
		
		long caller_st = System.nanoTime();
		URLConnection conn = checking_uri.toURL().openConnection();
		in = conn.getInputStream();
		result.read(in, "");
		in.close();
		in = null;
		long caller_fi = System.nanoTime();
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "psinit.get2pm", caller_st, caller_fi, caller_fi - caller_st, (int)result.size(), "rdf3");
		_trace.details.add(ptdi);

		return result;
	}
	
	public String doPersonalize(Model _ini_model, Configuration _conf, iPServiceResultVisualizer _vis, 
			Map<String, String> _params, PerformanceTraceItem _trace)
	{
		Model _result = ModelFactory.createDefaultModel();
		_result.add(_ini_model);

		// Read parameters
		String _user = _params.get(iPService.PSERVICE_PARAM_USER_ID);
		String _group = _params.get(iPService.PSERVICE_PARAM_GROUP_ID);
		String _session = _params.get(iPService.PSERVICE_PARAM_SESSION_ID);
		String _date = _params.get(iPService.PSERVICE_PARAM_DATE);
//		String _rdf = _params.get(PSERVICE_PARAM_RDF);
//		String _uri = _params.get(PSERVICE_PARAM_URI);
		String _context_path = _params.get(DataRobot.REST_CONTEXT_PATH);
		
		_trace.user_group = _user + ":" + _group;
//		System.out.println("~~~ [PService] SinglePointADAPT2ConceptQuizGuideSNPService user: " + _user + "  group: " + _group);

		String vis_id = _params.get(DataRobot.REST_VISUALIZER_ID);
		
//		_model = ModelFactory.createDefaultModel();
//		
//		// Check URI
//		try
//		{
//			if (_uri==null || _uri.length()==0)
//				throw new URISyntaxException("{emptystring}","Is not a valid URI");
//			URI checking_uri = new URI(_uri);
//		}
//		catch(URISyntaxException use)
//		{
//			return _model;
//		}
//		
//		// read RSS from URI
//		try
//		{
//			InputStream in = FileManager.get().open( _uri );
//			_model.read(in, "");
//			in.close();
//			in = null;
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace(System.out);
//		}
//
//		// If RDF specified add it to the model
//		if(_rdf != null && _rdf.length() >0)
//		{
//			// TODO
//			//_model.add(_rdf);
//		}
		
		ArrayList<ProgressEstimatorReport> resArray = null;

		// UM timing
		long um_start = System.nanoTime(); //currentTimeMillis();
		// end of - UM timing

		// send request and receive response
		try
		{
			URI adapt2_cumulate_reportmanager = _conf.uris.get("context");
//			System.out.println("~~~ PService " + adapt2_cumulate_reportmanager);
			URL url = new URL(adapt2_cumulate_reportmanager + "&usr=" + _user + "&grp=" + _group);
			URLConnection con = url.openConnection();
			
			ObjectInputStream ii = new ObjectInputStream(con.getInputStream());
			try{ resArray = (ArrayList)ii.readObject(); }
			catch(Exception e) { e.printStackTrace(System.err); }
			finally{ii.close();}

		}
		catch(Exception e) { e.printStackTrace(System.out); }
		
		// UM timing
		long um_finish = System.nanoTime(); //currentTimeMillis();
		
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.um", um_start, um_finish, um_finish - um_start, (int)resArray.size(),"arri");
		_trace.details.add(ptdi);

//		_trace.external_cost = um_finish - um_start;
//		_trace.external_ts_start = um_start;
//		_trace.external_ts_end = um_finish;
		// end of -- UM timing		
		
		Iterator it = resArray.iterator();
		double sum_group_progr = 0.0;
		double sum_indiv_progr = 0.0;
		double sum_group_count = 0.0;
		double sum_indiv_count = 0.0;
		int effective_count = 0;
		for(;it.hasNext();)
		{
			ProgressEstimatorReport pe = (ProgressEstimatorReport)it.next();
//			System.out.println("SPointADAPT2QGSNPS " + pe);
			
//			if(pe.id.equalsIgnoreCase("query") || pe.id.equalsIgnoreCase("open") ||
//					pe.id.equalsIgnoreCase("close") )
//				continue;
//			if(pe.progress_g > 0.0 || pe.count_g > 0.0 || pe.progress>0.0 || pe.count > 0.0)
			{
				sum_group_progr += pe.getGroupProgress(1/*fudge of single item*/);
				sum_indiv_progr += pe.getProgress(1/*fudge of single item*/);
				sum_group_count += pe.getGroupCount(1/*fudge of single item*/);
				sum_indiv_count += pe.getCount(1/*fudge of single item*/);
				effective_count ++;
			}
		}
		sum_group_progr /= effective_count;
		sum_indiv_progr /= effective_count;

		// upload responce data
		Property p_el = _result.createProperty(DCTerms.educationLevel.toString());
		Property p_gp = _result.createProperty(DCTerms.audience.toString());
		Property annotation = _result.createProperty(DC.description.toString());

		Literal lit_el = _result.createLiteral(Double.toString(sum_indiv_progr));
		Literal lit_gp = _result.createLiteral(Double.toString(sum_group_progr));
//			Literal lit_extent = _model.createLiteral("<strong>Your overall progress is " + Math.round(sum_indiv_progr*100) + 
//					"% vs. group progress " + Math.round(sum_group_progr*100) + 
//					"%. You have made " + Math.round(sum_indiv_count) + " learning attemts while group an average of " + Math.round(sum_group_count) + " learning attemts</strong>");
	
//		StmtIterator iter = model.listStatements();
		Resource _o = _result.getResource(RSS.channel.toString());
		StmtIterator iter = _result.listStatements(null, RDF.type, _o);
		if (iter.hasNext())
		{
			Statement stmt = iter.nextStatement();
			Resource lo = stmt.getSubject();
			_result.add(lo, p_el, lit_el);
			_result.add(lo, p_gp, lit_gp);
			if(_vis != null)
			{
				Annotation annot = _vis.getItemAnnotation(lo, _context_path, _trace);
				String s_annotation = annot.icon;
				if(s_annotation != null && s_annotation.length() > 0)
				{
					Literal val_annotation = _result.createLiteral(s_annotation, true);
					_result.add(lo, annotation, val_annotation);
				}
			}
		}
		
		return Model2String(_result);
	}
	
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
//System.out.println(this.getClass().getName() + "::retrieveParams starting...");		
//		Map<String, String> params = new HashMap<String, String>();
		
		_params.put(PSERVICE_PARAM_USER_ID, _request.getParameter(PSERVICE_PARAM_USER_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_USER_ID + "=" + _request.getParameter(PSERVICE_PARAM_USER_ID));
		_params.put(PSERVICE_PARAM_GROUP_ID, _request.getParameter(PSERVICE_PARAM_GROUP_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_GROUP_ID + "=" + _request.getParameter(PSERVICE_PARAM_GROUP_ID));
		_params.put(PSERVICE_PARAM_SESSION_ID, _request.getParameter(PSERVICE_PARAM_SESSION_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_SESSION_ID + "=" + _request.getParameter(PSERVICE_PARAM_SESSION_ID));
		_params.put(PSERVICE_PARAM_DATE, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_DATE),"UTF-8"));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_DATE + "=" + _request.getParameter(PSERVICE_PARAM_DATE));
//		params.put(PSERVICE_PARAM_RDF, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_RDF),"UTF-8"));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_RDF + "=" + _request.getParameter(PSERVICE_PARAM_RDF));
		_params.put(PSERVICE_PARAM_URI, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_URI),"UTF-8"));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_URI + "=" + _request.getParameter(PSERVICE_PARAM_URI));
		return _params;
	}

	public String getInvokerUI(Configuration _conf)
	{
		String user_id = "";
		String group_id = "";
		String session_id = "";
		String date = "";
		String rdf = "";
		String uri = "";
		
		String result = 
			"	<tr>\n"+
			"		<td valign='top'>User ID (ADAPT&sup2;)</td>\n"+
			"		<td width='100%'><input name='" + PSERVICE_PARAM_USER_ID + "' type='text' maxlength='40' size='20' value='" + user_id + "'/></td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Group ID (ADAPT&sup2;)</td>\n"+
			"		<td><input name='" + PSERVICE_PARAM_GROUP_ID + "' type='text' maxlength='40' size='20' value='" + group_id + "'/></td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Session ID</td>\n"+
			"		<td><input name='" + PSERVICE_PARAM_SESSION_ID + "' type='text' maxlength='5' size='20' value='" + session_id + "'/></td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Date</td>\n"+
			"		<td>\n" +
			"			<input name='" + PSERVICE_PARAM_DATE + "' type='text' maxlength='50' size='25' value='" + date + "'/><br/>\n" +
			"			<span style='font-size:0.7em;color:grey;'>e.g. 1999-09-08T10:00:00-05:00 - 8th of September 1999, 10 a.m. EST</span>\n" +
			"		</td>\n"+
			"	</tr>\n"+
			"	<tr>\n"+
			"		<td valign='top'>Add-l&nbsp;URI</td>\n"+
			"		<td><input name='" + PSERVICE_PARAM_URI + "' type='text' maxlength='255' size='50' value='" + uri + "'/></td>\n"+
			"	</tr>\n" +
			"	<tr>\n"+
			"		<td valign='top'>RDF</td>\n"+
			"		<td><textarea name='" + PSERVICE_PARAM_RDF + "' cols='46' rows='5'>" + rdf + "</textarea></td>\n"+
			"	</tr>\n";
		
		return result;
	}

}
