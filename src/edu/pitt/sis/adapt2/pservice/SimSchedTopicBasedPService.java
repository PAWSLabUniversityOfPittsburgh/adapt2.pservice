package edu.pitt.sis.adapt2.pservice;

//import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

public class SimSchedTopicBasedPService extends PService
{
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace)
//			throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException
	{
		String signature = "";

		Model model = ModelFactory.createDefaultModel();
		try
		{
//			// 1. load RDF from URI's
//			for(Iterator<URI> it=_conf.uri_list.iterator(); it.hasNext(); )
//			{
//				InputStream in = FileManager.get().open( it.next().toString() );
//				model.read(in, "");
//				in.close();
//				in = null;
//			}
			
			// 2. load raw RDF
			for(Iterator<String> it=_conf.rdfs.iterator(); it.hasNext(); )
			{
				StringReader sr = new StringReader( it.next().toString() );
				model.read(sr, "");
				sr.close();
				sr = null;
			}
			signature+=_conf.rdfs.size() + ".";
			signature+=_conf.uris.size() + ".";
			
			// 3. load RDF from datasources as per context(s)
			for(Iterator<PServiceDataSource> itds=_conf.datasource_list.iterator(); itds.hasNext(); )
			{// for all datasources
				PServiceDataSource psds = itds.next();

				for(Iterator<Entry<String,URI>> itc=_conf.uris.entrySet().iterator(); itc.hasNext(); )
				{// for all contexts
					Entry<String,URI> entry = itc.next();
					if("context".equals(entry.getValue()))
					{//if it is a context
						String context = itc.next().toString();
						if(psds.type == PServiceDataSource.PSERVICE_DATASOURCE_SESAME)
						{
							// query repository
							String qry =
								"SELECT T\n" +
								"FROM {C} rdf:type {rdfs:Class},\n" +
								"	{C} dc:relation {T}\n" + //, 
	//							"	{T} rdf:type {rss:channel}\n" +
								"WHERE C = <" + context + ">\n" +
								"USING NAMESPACE\n" +
								"	dc = <http://purl.org/dc/elements/1.1/>,\n" +
								"	rss = <http://purl.org/rss/1.0/>";
							ArrayList<ArrayList<String>> result = psds.executeTableQuery(qry);
							// pull the info
							InputStream in = null; 
							String uri = "";
							for(Iterator<ArrayList<String>> it_r=result.iterator(); it_r.hasNext(); )
							{
								uri = it_r.next().get(0);
								in = FileManager.get().open( uri.toString() );
								model.read(in, "");
								in.close();
								in = null;
							}
						}
					}// end of -- if it is a context
				}// end of -- for all contexts
			}// end of --for all datasources
			// 4. add filters to the model
			//TODO
		}
//		catch(IOException ioe)
//		{
//			ioe.printStackTrace(System.out);
//			return model;
//		}
//		catch (AccessDeniedException ade)
//		{
//			ade.printStackTrace(System.out);
//			return model;
//		}
//		catch (MalformedQueryException mqe)
//		{
//			mqe.printStackTrace(System.out);
//			return model;
//		}
//		catch (QueryEvaluationException qee)
//		{
//			qee.printStackTrace(System.out);
//			return model;
//		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
			return model;
		}
	
		return model;
	}
	
	public String doPersonalize(Model _model, Configuration _conf, iPServiceResultVisualizer _vis, Map<String, 
			String> _params, PerformanceTraceItem _trace) throws URISyntaxException, IOException, ClassNotFoundException
	{
		// Fake parameters so far
		String _user = _params.get(iPService.PSERVICE_PARAM_USER_ID);
		String _group = _params.get(iPService.PSERVICE_PARAM_GROUP_ID);
		String _session = _params.get(iPService.PSERVICE_PARAM_SESSION_ID);
		String _date = _params.get(iPService.PSERVICE_PARAM_DATE);
		String _rdf = _params.get(PSERVICE_PARAM_RDF);
		String _uri = _params.get(PSERVICE_PARAM_URI);
		
		_trace.user_group = _user + ":" + _group;
		
		// If URI specified add its content to the model
		if(_uri != null && _uri.length() >0)
		{
			try
			{
				InputStream in = FileManager.get().open( _uri );
				_model.read(in, "");
				in.close();
				in = null;
			}
			catch (IOException e)
			{
				e.printStackTrace(System.out);
			}
		}
			
		// If RDF specified add it to the model
		if(_rdf != null && _rdf.length() >0)
		{
			// TODO
			//_model.add(_rdf);
		}
			
		// Fake parameters so far
//		_user = "myudelson";
//		_group = "admins";
//		_session = "pst01";
//		_date = "1999-09-08T10:00:00-05:00";
		
		String qry2 = 
			"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
			"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX  dc:   <http://purl.org/dc/elements/1.1/>" +
			"PREFIX  dcterms: <http://purl.org/dc/terms/>" +
			"PREFIX  rss:  <http://purl.org/rss/1.0/>" +
			"PREFIX  lom: <http://purl.org/lom/terms/>" +
			"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>" +
			"CONSTRUCT" + 
			"{" +
			"	?lo ?p ?s ." +
			"	?lo_past lom:Status \"past\" ." +
			"	?lo_current lom:Status \"current\" ." +
			"	?lo_future lom:Status \"future\" ." +
			"}" +
			"WHERE" +
			"{ " +
			"	{" +
			"		?lo ?p ?s ." + 
			"		?lo rdf:type rss:item ." +
//			"		FILTER (?p = rss:title || ?p = rss:description || ?p = rss:link || (?p = rdf:type && ?s = rss:item ))" +
			"	}" +
			"	UNION" +
			"	{" +
			"		?lo_past ?p ?s . " +
			"		?lo_past rdf:type rss:item ." +
			"		?t_past rdf:type rdfs:Class ." +
			"		?t_past dc:date ?_per_past ." +
			"		?_per_past rdf:type dcterms:Period ." +
			"		?_per_past dcterms:end ?end_past ." +
			"		?lo_past dc:subject ?t_past ." +
			"		FILTER (?end_past < \"" + _date + "\"^^xsd:dateTime) ." +
//			"		FILTER (?p = rss:title || ?p = rss:description || ?p = rss:link || ?p = rdf:type) " +
			"	}" +
			"	UNION" +
			"	{" +
			"		?lo_current ?p ?s . " +
			"		?lo_current rdf:type rss:item ." +
			"		?t_current rdf:type rdfs:Class ." +
			"		?t_current dc:date ?_per_current ." +
			"		?_per_current rdf:type dcterms:Period ." +
			"		?_per_current dcterms:start ?start_current ." +
			"		?_per_current dcterms:end ?end_current ." +
			"		?lo_current dc:subject ?t_current ." +
			"		FILTER (?start_current <= \"" + _date + "\"^^xsd:dateTime) ." +
			"		FILTER (?end_current > \"" + _date + "\"^^xsd:dateTime) ." +
//			"		FILTER (?p = rss:title || ?p = rss:description || ?p = rss:link || ?p = rdf:type) " +
			"	}" +
			"	UNION" +
			"	{" +
			"		?lo_future ?p ?s . " +
			"		?lo_future rdf:type rss:item ." +
			"		?t_future rdf:type rdfs:Class ." +
			"		?t_future dc:date ?_per_future ." +
			"		?_per_future rdf:type dcterms:Period ." +
			"		?_per_future dcterms:start ?start_future ." +
			"		?lo_future dc:subject ?t_future ." +
			"		FILTER (?start_future > \"" + _date + "\"^^xsd:dateTime) ." +
//			"		FILTER (?p = rss:title || ?p = rss:description || ?p = rss:link || ?p = rdf:type) " +
			"	}" +
			"}";
			
		Query query2 = QueryFactory.create(qry2);
		QueryExecution qe2 = QueryExecutionFactory.create(query2, _model);
		
		Model pers_model =  qe2.execConstruct();
		
		// Update URLs of the LOs
		Property _link = _model.getProperty("http://purl.org/rss/1.0/link");
		Literal _lit = null;
		
		StmtIterator iter2 = pers_model.listStatements(null, _link, _lit);
		ArrayList<Statement> statements = new ArrayList<Statement>(); 
		
		while (iter2.hasNext())
			statements.add(iter2.nextStatement());
		for(int i=0; i<statements.size(); i++)
		{
			Statement stmt = statements.get(i);
			
			String _obj_literal = stmt.getObject().toString();
			_obj_literal += ( (_obj_literal.indexOf("?")==-1)?"?":"&" ) +
				"usr=" +_user + "&grp=" + _group + "&sid=" + _session;
			stmt.changeObject(_obj_literal);
		}
		
		return Model2String(pers_model);
	}
	
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
//		Map<String, String> params = new HashMap<String, String>();
		_params.put(PSERVICE_PARAM_USER_ID, _request.getParameter(PSERVICE_PARAM_USER_ID));
		_params.put(PSERVICE_PARAM_GROUP_ID, _request.getParameter(PSERVICE_PARAM_GROUP_ID));
		_params.put(PSERVICE_PARAM_SESSION_ID, _request.getParameter(PSERVICE_PARAM_SESSION_ID));
		_params.put(PSERVICE_PARAM_DATE, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_DATE),"UTF-8"));
		_params.put(PSERVICE_PARAM_RDF, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_RDF),"UTF-8"));
		_params.put(PSERVICE_PARAM_URI, URLDecoder.decode(_request.getParameter(PSERVICE_PARAM_URI),"UTF-8"));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_USER_ID + "=" + _request.getParameter(PSERVICE_PARAM_USER_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_GROUP_ID + "=" + _request.getParameter(PSERVICE_PARAM_GROUP_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_SESSION_ID + "=" + _request.getParameter(PSERVICE_PARAM_SESSION_ID));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_DATE + "=" + _request.getParameter(PSERVICE_PARAM_DATE));
//System.out.println(this.getClass().getName() + "::retrieveParams " + PSERVICE_PARAM_RDF + "=" + _request.getParameter(PSERVICE_PARAM_RDF));
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
