package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;

public class ADAPT2RecommendExamples extends PService implements iPService
{
	// Constants
//	public final static String ADAPT2_CUMULATE_REPORTMNAGER = "http://localhost:8080/cbum/ReportManager?typ=act&dir=in&frm=dat";
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace) throws IOException, URISyntaxException
	{
//		String _uri = _params.get(PSERVICE_PARAM_URI);
//
//		// read RSS from URI
//		Model _result = ModelFactory.createDefaultModel();
//		
//		URI checking_uri = new URI(_uri);
//		
//		long caller_st = System.nanoTime();
//		URLConnection conn = checking_uri.toURL().openConnection();
//		InputStream in = conn.getInputStream();
//		_result.read(in, "");
//		long caller_fi = System.nanoTime();
//		int d_size = _trace.details.size();
//		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "psinit.getKTmodel", caller_st, caller_fi, caller_fi - caller_st, (int)_result.size(), "rdf3");
//		_trace.details.add(ptdi);
//		
//		in.close();
//		in = null;
//
//		return _result;
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

		/**/
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
	    		"&" + URLEncoder.encode(DataRobot.REST_SERVICE_INVOKE_TOKEN, "UTF-8") + "=" + URLEncoder.encode(_inv_token, "UTF-8");
	        // Send data
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
		/**/
		
		// join all URI comma-separated
		String uris = "";
		StmtIterator iter = _result.listStatements(null, RDF.type, RSS.item);

		while (iter.hasNext())
			uris += ((uris.length()>0)?",":"") + URLEncoder.encode(iter.nextStatement().getSubject().toString(), "UTF-8");
		

		// RecSys timing
		long recsys_start = System.nanoTime(); //currentTimeMillis();;

		URI rec_sys_api = _conf.uris.get("rec_api");
		String params = "uri=" + uris + "&num=" + 5;

		// send request and receive response
        URLConnection conn = rec_sys_api.toURL().openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(params);
        wr.flush();
    
        // Get the response
		InputStream in  = conn.getInputStream();
		
		Model _rec_sys = ModelFactory.createDefaultModel();
		_rec_sys.read(in, "");
		
//System.out.println("\n----");
//StringBuffer out = new StringBuffer();
//byte[] b = new byte[4096];
//for (int n; (n = in.read(b)) != -1;)
//{
//    out.append(new String(b, 0, n));
//}
//System.out.println( out.toString() );
//System.out.println("----\n");
		
		in.close();
        wr.close();
        in = null;
        wr = null;

//ResIterator _right_iter = _rec_sys.listResourcesWithProperty(DC.rights);
//int c = 0;
//if(_right_iter.hasNext())
//	c++;
//System.out.println("rights = " + c);		
//_right_iter = _rec_sys.listResourcesWithProperty(RDF.value);
//c = 0;
//if(_right_iter.hasNext())
//	c++;
//System.out.println("values = " + c);		
//_right_iter = _rec_sys.listResourcesWithProperty(RSS.link);
//c = 0;
//if(_right_iter.hasNext())
//	c++;
//System.out.println("links = " + c);		

		// RecSys timing
		long recsys_finish = System.nanoTime(); //currentTimeMillis();
		/*int/**/ d_size = _trace.details.size();
		/*PerformanceTraceDetailItem/**/ ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.recsys", recsys_start, recsys_finish, (recsys_finish - recsys_start), (int)_rec_sys.size(), "rdf3");
		_trace.details.add(ptdi);

		// Pers timing
		long pers_start = System.nanoTime(); //currentTimeMillis();;

		// Add the recommendations
		Seq result_seq = null;
		Seq recsys_seq = null;
		StmtIterator _seq_iter = _result.listStatements(null, RDF.type, RDF.Seq);
		if(_seq_iter.hasNext())
		{
			Statement seq_stmt = _seq_iter.nextStatement();
			result_seq = _result.getSeq(seq_stmt.getSubject());
		}
		_seq_iter = _rec_sys.listStatements(null, RDF.type, RDF.Seq);
		if(_seq_iter.hasNext())
		{
			Statement seq_stmt = _seq_iter.nextStatement();
			recsys_seq = _rec_sys.getSeq(seq_stmt.getSubject());
		}
		
		int size_before = (int)_result.size();
		String saved_state = "";
		
		NodeIterator rec_sys_iter = recsys_seq.iterator();
		// Iterate recommendations and add to result
		while (rec_sys_iter.hasNext())
		{
			RDFNode node = rec_sys_iter.nextNode();
			String uri = node.toString();
			Resource res = _rec_sys.getResource(uri);
			String title = res.getProperty(RSS.title).getString();
			String url = res.getProperty(RSS.link).getString();
			String value = res.getProperty(RDF.value).getString();
			String rights = res.getProperty(DC.rights).getString();
//System.out.println("~~uri=" + uri);			
//System.out.println("~~title=" + title);			
//System.out.println("~~url=" + url);			
//System.out.println("~~value=" + value);			
			Resource new_r = _result.createResource(uri);
//System.out.println("~ size before = " + result_seq.size());
			result_seq = result_seq.add(result_seq.size()+1, new_r);
//System.out.println("~ size after = " + result_seq.size() +"\n");

			// add user and group to URL
			url = url + "&amp;usr=" + _user + "&amp;grp=" + _group;
			
			_result.add(_result.createStatement(new_r, RSS.title, title));
			_result.add(_result.createStatement(new_r, RDF.type, RSS.item));
			_result.add(_result.createStatement(new_r, RSS.link, url));
			_result.add(_result.createStatement(new_r, RDF.value, value));
			_result.add(_result.createStatement(new_r, DC.rights, rights));
			_result.add(_result.createStatement(new_r, DC.relation, "recommended"));
			
			if(_vis != null)
			{
				Annotation annot = _vis.getItemAnnotation(new_r, _context_path, _trace);
				String s_annot = annot.icon;
				String s_style = annot.style;
				
				saved_state += new_r.getURI() + "\t" + annot.summary +"\n";
				
				if(s_annot != null && s_annot.length() > 0)
				{
					Literal val_annotation = _result.createLiteral(s_annot, true);
					_result.add(new_r, DC.description, val_annotation);
				}
				if(s_style != null && s_style.length() > 0)
				{
					Literal val_style = _result.createLiteral(s_style, true);
					_result.add(new_r, DC.format, val_style);
				}
				
			}

		}
		
		// for the channel
		if(_vis != null)
		{
			StmtIterator ch_iter = _result.listStatements(null, RDF.type, RSS.channel);
			//Dependant folder model URIs
			if (ch_iter.hasNext())
			{
				Resource channel = ch_iter.nextStatement().getSubject();
				Literal ch_annot = _result.createLiteral(_vis.getChannelAnnotation(_context_path, _trace).icon, true);
				
				channel.removeAll(DC.description);
				_result.add(channel, DC.description, ch_annot);
			}
		}
		
		_trace.saved_state = saved_state; /**/

		// Pers timing
		long pers_finish = System.nanoTime(); //currentTimeMillis();
		d_size = _trace.details.size();
		ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.aggreg", pers_start, pers_finish, pers_finish - pers_start, ((int)_result.size()-size_before),"rdf3");
		_trace.details.add(ptdi);

		return Model2String(_result);
	}
	
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
//System.out.println(this.getClass().getName() + "::retrieveParams starting...");		
//		Map<String, String> params = new HashMap<String, String>();
		
//		System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_USER_ID + "=" + _request.getParameter(PSERVICE_PARAM_USER_ID));
		String _existing_user_id = _params.get(PSERVICE_PARAM_USER_ID);
		if( _existing_user_id == null || _existing_user_id.length() == 0)
			_params.put(PSERVICE_PARAM_USER_ID, _request.getParameter(PSERVICE_PARAM_USER_ID));
		
//		System.out.println(this.getClass().getName() + "::PSERVICE_PARAM_GROUP_ID " + PSERVICE_PARAM_GROUP_ID + "=" + _request.getParameter(PSERVICE_PARAM_GROUP_ID));
		String _existing_group_id = _params.get(PSERVICE_PARAM_GROUP_ID);
		if( _existing_group_id == null || _existing_group_id.length() == 0)
			_params.put(PSERVICE_PARAM_GROUP_ID, _request.getParameter(PSERVICE_PARAM_GROUP_ID));
		
//		System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_URI + "=" + _request.getParameter(PSERVICE_PARAM_URI));
		String _existing_uri = _params.get(PSERVICE_PARAM_URI);
		if( _existing_uri == null || _existing_uri.length() == 0)
			_params.put(PSERVICE_PARAM_URI, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_URI),"UTF-8"));
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
			"	<tr>\n"+
			"		<td valign='top'>Token</td>\n"+
			"		<td><input name='" + DataRobot.REST_SERVICE_INVOKE_TOKEN + "' type='text' maxlength='255' size='50' value='" + token + "'/></td>\n"+
			"	</tr>\n" +
			"	<tr>\n"+
			"		<td valign='top'>RDF</td>\n"+
			"		<td><textarea name='" + PSERVICE_PARAM_RDF + "' cols='46' rows='5'>" + rdf + "</textarea></td>\n"+
			"	</tr>\n";
		
		return result;
	}

}
