package edu.pitt.sis.adapt2.pservice;

/*
 * TODO
 * Weighted quiz calculation
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.ResourceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorReport;
import edu.pitt.sis.paws.core.Item2Vector;

public class ADAPT2TopicQGExtPService extends PService
{
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace)
	{
		return ModelFactory.createDefaultModel();
	}
	
	public String doPersonalize(Model _ini_model, Configuration _conf, iPServiceResultVisualizer _vis, 
			Map<String, String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException
	{
		Model _result = ModelFactory.createDefaultModel();
		_result.add(_ini_model);

		// Read parameters
		String _user = _params.get(iPService.PSERVICE_PARAM_USER_ID);
		String _group = _params.get(iPService.PSERVICE_PARAM_GROUP_ID);
		String _session = _params.get(iPService.PSERVICE_PARAM_SESSION_ID);
		String _date = _params.get(iPService.PSERVICE_PARAM_DATE);
		String _rdf = _params.get(PSERVICE_PARAM_RDF);
		String _uri = _params.get(PSERVICE_PARAM_URI);
		String _context_path = _params.get(DataRobot.REST_CONTEXT_PATH);
		String _inv_token = _params.get(DataRobot.REST_SERVICE_INVOKE_TOKEN);
		
		_session = (_session==null)?"":_session;
		_date = (_date==null)?"":_date;
		_rdf = (_rdf==null)?"":_rdf;
		_inv_token = (_inv_token==null)?"":_inv_token;
		
		
		_trace.user_group = _user + ":" + _group;

//		String vis_id = _params.get(DataRobot.REST_VISUALIZER_ID);
		
		long channel_st = System.nanoTime(); //currentTimeMillis();
		// read RDF from external service
		try
		{
	        // Construct data
	        String params = URLEncoder.encode(PSERVICE_PARAM_USER_ID, "UTF-8") + "=" + URLEncoder.encode(_user, "UTF-8") +
	    		"&" + URLEncoder.encode(PSERVICE_PARAM_GROUP_ID, "UTF-8") + "=" + URLEncoder.encode(_group, "UTF-8") +
	    		"&" + URLEncoder.encode(PSERVICE_PARAM_SESSION_ID, "UTF-8") + "=" + URLEncoder.encode(_session, "UTF-8") +
	    		"&" + URLEncoder.encode(PSERVICE_PARAM_DATE, "UTF-8") + "=" + URLEncoder.encode(_date, "UTF-8") +
	    		"&" + URLEncoder.encode(PSERVICE_PARAM_RDF, "UTF-8") + "=" + URLEncoder.encode(_rdf, "UTF-8") +
	    		"&" + URLEncoder.encode(PSERVICE_PARAM_URI, "UTF-8") + "=" + URLEncoder.encode(_uri, "UTF-8") +
	    		"&" + URLEncoder.encode(DataRobot.REST_SERVICE_INVOKE_TOKEN, "UTF-8") + "=" + URLEncoder.encode(_inv_token, "UTF-8") +
	    		"&" + URLEncoder.encode(PSERVICE_PARAM_URI, "UTF-8");
	        // Send data
//	        URL url = new URL("http://adapt2.sis.pitt.edu/pservice/service/sim-adapt2-social/invoke/sim-adapt2-social/vis/sim-adapt-sn-portal-v2");
	        
//	        URI service_uri = _conf.service_list.get(0);
//System.out.println("(_conf==null):" + (_conf==null));	        
//System.out.println("(_conf.uris==null):" + (_conf.uris==null));	        
	        URI service_uri = _conf.uris.get("pservice");//
	        URLConnection conn = service_uri.toURL().openConnection();
	        conn.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	        wr.write(params);
	        wr.flush();
	    
	        // Get the response
			InputStream in  = conn.getInputStream();
			
			_result.read(in, "");
			in.close();
	        wr.close();
	        
		}
		catch(UnsupportedEncodingException uee) { uee.printStackTrace(System.out); }
		catch(IOException ioe) { ioe.printStackTrace(System.out); };
		// end of -- REQUEST PService OUTPUT
		long channel_fi = System.nanoTime(); //currentTimeMillis();
		
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.channel", channel_st, channel_fi, channel_fi - channel_st, (int)_result.size(), "rdf3");
		_trace.details.add(ptdi);
		_trace.token_suffix = _trace.pservice_rdfid + ":" + _trace.conf_rdfid + ".channel";

//		_trace.chanel_model_ts_start = channel_st;
//		_trace.chanel_model_ts_end = channel_fi;
//		_trace.chanel_model_cost = channel_fi - channel_st;
//		_trace.chanel_model_size = _result.size();
		
		
		Item2Vector<ResourceItem> folder_item_list = new Item2Vector<ResourceItem>();
//		Map<String, Double> resource_progress_map = new HashMap<String, Double>();
		Item2Vector<ResourceItem> resource_list = new Item2Vector<ResourceItem>();

		ArrayList<ProgressEstimatorReport> reqArray = new ArrayList<ProgressEstimatorReport>();
		ArrayList<ProgressEstimatorReport> resArray = null;
		
//		Resource _o = _model.getResource(RSS.item.toString());
		StmtIterator top_iter = _result.listStatements(null, RDF.type, DCTypes.Collection);
		//Dependant folder model URIs
		while (top_iter.hasNext())
		{// for all folders
			String folder_uri = top_iter.nextStatement().getSubject().toString();
			ResourceItem fi = new ResourceItem(folder_uri);
			folder_item_list.add(fi);
			
			// get quizzes into a map
			Model sub_model = ModelFactory.createDefaultModel();
			URI folder_checking_uri = new URI(folder_uri);

//System.out.println("folder_checking_uri="+folder_checking_uri);
			
			InputStream in = FileManager.get().open( folder_checking_uri.toString() );
			sub_model.read(in, "");
			in.close();
			in = null;
			
			StmtIterator sub_iter = sub_model.listStatements(null, RDF.type, RSS.item);
			while (sub_iter.hasNext())
			{// for all sub-items
				String quiz_uri = sub_iter.nextStatement().getSubject().toString();
				
				boolean filtered = false;
				for(Iterator<Entry<String,URI>> iter = _conf.uris.entrySet().iterator(); iter.hasNext();)
				{
					Entry<String,URI> entry = iter.next();
					if(  (entry.getKey().indexOf("uri_filter")>-1) && (quiz_uri.indexOf(entry.getValue().toString()) > -1))
					{ // ("uri_filter".equals(entry.getKey())
						filtered = true;
						break;
					}
				}
//				System.out.println("filtering " + quiz_uri + " " + quiz_uri.indexOf("http://adapt2.sis.pitt.edu/webex/"));
				
				if(filtered) // if it is in the filter(s)
				{
//System.out.println("ADAPT2TopicQGExtPService.doPersonalize FILTERED quiz_uri=" + quiz_uri);					
					ResourceItem res = resource_list.findByURI(quiz_uri);
					if(res==null)
					{
						res = new ResourceItem(0,"",quiz_uri);
						resource_list.add(res);
						ProgressEstimatorReport pea = new ProgressEstimatorReport(quiz_uri);
						reqArray.add(pea);
					}
					fi.getSubs().add(res);
				}
			}// end of --  for all sub-items
			
			// recycle sub_model
			sub_model.close();
			sub_model = null;

		}// end of -- for all folders
		
		// Request progress from user model
		// UM timing
		long um_start = System.nanoTime(); //currentTimeMillis();

		URI adapt2_cumulate_reportmanager = _conf.uris.get("context");
		URL url = new URL(adapt2_cumulate_reportmanager + "&usr=" + _user + "&grp=" + _group + "&app=24" + "&token=" + _inv_token);
		URLConnection con = url.openConnection();
		con.setUseCaches(false);
		con.setDefaultUseCaches(false);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type","java-internal/" + reqArray.getClass().getName());
		ObjectOutputStream oo = new ObjectOutputStream(con.getOutputStream());
		oo.writeObject(reqArray);
		oo.flush();
		oo.close();

		ObjectInputStream ii = new ObjectInputStream(con.getInputStream());
		resArray = (ArrayList)ii.readObject();
		ii.close();

		// UM timing
		long um_finish = System.nanoTime(); //currentTimeMillis();
		
		ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.um", um_start, um_finish, um_finish - um_start, resArray.size(), "arri");
		_trace.details.add(ptdi);

//		_trace.external_cost = um_finish - um_start;
//		_trace.external_ts_start = um_start;
//		_trace.external_ts_end = um_finish;
		// end of -- UM timing		
		
		// upload quiz progress data
		for(int i=0; i<resArray.size(); i++)
		{
			ProgressEstimatorReport pea = resArray.get(i);
			ResourceItem res = resource_list.findByURI(pea.getId());
			if(res != null)
			{
				res.setProgress(pea.getProgress(1/*fudge of single item*/));
			}
			else
				System.out.println("ADAPT2TopicQGExtPService.doPersonalize Peronalized resource " + pea.getId() + " not found");
		}

		// compute folder progress data
		String saved_state = "";
		
		// for the channel
		if(_vis != null)
		{
			StmtIterator ch_iter = _result.listStatements(null, RDF.type, RSS.channel);
			//Dependant folder model URIs
			if (ch_iter.hasNext())
			{
				Resource channel = ch_iter.nextStatement().getSubject();
				Literal ch_annot = _result.createLiteral(_vis.getChannelAnnotation(_context_path, _trace).icon, true);
				_result.add(channel, DC.description, ch_annot);
			}
		}
		
		for(int i=0; i<folder_item_list.size(); i++)
		{// for all folder items
			ResourceItem _fi = folder_item_list.get(i);
			double folder_progress = _fi.getMeanResourceProgress();
//System.out.println("EG " + _fi.folder_uri + " sum=" + folder_progress);			
			Resource lo = _result.createResource(_fi.getURI());
			Literal lit_el = _result.createLiteral(Double.toString(folder_progress));
			_result.add(lo, DCTerms.extent, lit_el);
			if(_vis != null)
			{
				Annotation annot = _vis.getItemAnnotation(lo, _context_path, _trace);
				saved_state += lo.getURI() + "\t" + annot.summary +"\n";

				String s_annotation = annot.icon;
				if(s_annotation != null && s_annotation.length() > 0)
				{
					Literal val_annotation = _result.createLiteral(s_annotation, true);
					_result.add(lo, DC.description, val_annotation);
				}
				
//				String s_style = _vis.getItemStyle(lo);
//				if(s_style != null && s_style.length() > 0)
//				{
//					Literal val_style = top_model.createLiteral(s_style, true);
//					top_model.add(lo, style, val_style);
//				}
			}
				
		}// end of -- // for all folder items
		
		/**/
		
		_trace.saved_state = saved_state;
		
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
//		_params.put(PSERVICE_PARAM_SESSION_ID, _request.getParameter(PSERVICE_PARAM_SESSION_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_SESSION_ID + "=" + _request.getParameter(PSERVICE_PARAM_SESSION_ID));
//		_params.put(PSERVICE_PARAM_DATE, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_DATE),"UTF-8"));
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
		String token = "";
		
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
			"	<tr>\n" +
			"		<td valign='top'>Token</td>\n"+
			"		<td><input name='" + DataRobot.REST_SERVICE_INVOKE_TOKEN + "' type='text' maxlength='255' size='50' value='" + token + "'/></td>\n"+
			"	</tr>\n" +
			"	<tr>\n" +
			"		<td valign='top'>RDF</td>\n"+
			"		<td><textarea name='" + PSERVICE_PARAM_RDF + "' cols='46' rows='5'>" + rdf + "</textarea></td>\n"+
			"	</tr>\n";
		
		return result;
	}
	

}