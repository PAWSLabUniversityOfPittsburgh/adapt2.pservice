package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;

public class URIFilteringPService extends PService
{
	// Constants
//	public final static String ADAPT2_CUMULATE_REPORTMNAGER = "http://localhost:8080/cbum/ReportManager?typ=act&dir=in&frm=dat";
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace) throws IOException, URISyntaxException
	{
		String _uri = _params.get(PSERVICE_PARAM_URI);

		// read RSS from URI
		Model _result = ModelFactory.createDefaultModel();
		
		URI checking_uri = new URI(_uri);
		
		long caller_st = System.nanoTime();
		URLConnection conn = checking_uri.toURL().openConnection();
		InputStream in = conn.getInputStream();
		_result.read(in, "");
		long caller_fi = System.nanoTime();
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "psinit.getKTmodel", caller_st, caller_fi, caller_fi - caller_st, (int)_result.size(), "rdf3");
		_trace.details.add(ptdi);
		
		in.close();
		in = null;

		return _result;
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
//		String _session = _params.get(iPService.PSERVICE_PARAM_SESSION_ID);
//		String _date = _params.get(iPService.PSERVICE_PARAM_DATE);
//		String _rdf = _params.get(PSERVICE_PARAM_RDF);
//		String _uri = _params.get(PSERVICE_PARAM_URI);
//		String _context_path = _params.get(DataRobot.REST_CONTEXT_PATH);
//		String _inv_token = _params.get(DataRobot.REST_SERVICE_INVOKE_TOKEN);
		
		_trace.user_group = _user + ":" + _group;

		Set<Entry<String,URI>> filters = _conf.uris.entrySet();
//		ArrayList<String> uri_filter = _conf.uri.get("uri_filter").toString();
		String filter_list = "";
		ArrayList<String> uri_filters = new ArrayList<String>();
		for(Iterator<Entry<String,URI>> map_iter=filters.iterator(); map_iter.hasNext();)
		{
			Entry<String,URI> entry = map_iter.next();
			String key = entry.getKey();
			String value = entry.getValue().toString();
			if(key.indexOf("uri_filter")>=0)
			{
				uri_filters.add(value);
//System.out.println("added filter = " + value);				
				filter_list += ((filter_list.length()>0)?", ":"") + key;
			}
		}
		
		StmtIterator iter = _result.listStatements(null, RDF.type, RSS.item);
		
		int removed_count = 0;
		List<Statement> statemets_2remove = new ArrayList<Statement>();
		List<Statement> sqe_statemets_2remove = new ArrayList<Statement>();
		while (iter.hasNext())
		{
			Statement stmt = iter.nextStatement();
			String uri = stmt.getSubject().toString();
			
			boolean filter_out = false;
			for(Iterator<String> filter_iterator = uri_filters.iterator(); filter_iterator.hasNext() && !filter_out;)
				if(uri.indexOf(filter_iterator.next())>=0)
					filter_out = true;
			
			if(filter_out)
			{
				_result.enterCriticalSection(false);//set write lock
				StmtIterator del_iter = _result.listStatements (stmt.getSubject(), null, (RDFNode)null);
				statemets_2remove.addAll(del_iter.toList());
				del_iter = _result.listStatements (null, null, stmt.getSubject());
				sqe_statemets_2remove.addAll(del_iter.toList());
				removed_count ++;
			}
		}
		
		if(statemets_2remove.size()>0)
		{
			_result.remove(statemets_2remove);
			
			ResIterator channels = _result.listSubjectsWithProperty(RDF.type, RSS.channel);
			Resource channel = null;
			if (channels.hasNext())
				channel = (Resource)channels.next();

			if(channel != null && channel.hasProperty(RSS.items))
			{
				Seq seq_items = channel.getProperty(RSS.items).getSeq();
				int i=0;
				for(Iterator<Statement> s_iter=sqe_statemets_2remove.iterator(); s_iter.hasNext();)
				{
					Statement stmt = s_iter.next();
//					_result.enterCriticalSection(false);
//					seq_items.remove(stmt);
//					_result.leaveCriticalSection();
					int idx = seq_items.indexOf(stmt.getObject());
					seq_items.remove(idx);
					i++;
				}
			}
		}
		
		String saved_state = "removed " + removed_count + " resources where uri matches " + filter_list;
		
		_trace.saved_state = saved_state; /**/

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
