package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorReport;

public class EnsebmleSocialNavigationPService extends PService
{
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace) throws IOException, URISyntaxException
	{
		
//        StreamTokenizer streamTokenizer = null;
//        StringReader reader = new StringReader(_uri);
//        
//        try {
//            
//            streamTokenizer = new StreamTokenizer(reader);
//            
//            while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
//                
//                if (streamTokenizer.ttype == StreamTokenizer.TT_EOL)
//                	System.out.println("~~~ [PService] Ensemble-SN-Stub URI: " + streamTokenizer.nextToken() ) ;
//            }
//            
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
		
		
		// read RSS from URI
		Model _result = ModelFactory.createDefaultModel();
		
		/*
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
		in = null; /**/

		return _result;
	}
	
	public String doPersonalize(Model _ini_model, Configuration _conf, iPServiceResultVisualizer _vis, 
			Map<String, String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException
	{
		String _result = "";
		Model _default = ModelFactory.createDefaultModel();
		
		String _uri = _params.get(PSERVICE_PARAM_URI);

		// traverse uri list
		ArrayList<String> urls = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(_uri, "\r\n" ); //System.getProperty("line.separator") );
		while(st.hasMoreTokens())
		{// for all tokens
			urls.add(st.nextToken());
		}// -- for all tokens
		
		
//		for(String s:urls)
//			System.out.println("~~~ [PService] Ensemble-SN-Stub URI: " + s);

		String _context_path = _params.get(DataRobot.REST_CONTEXT_PATH);
		String _user = _params.get(iPService.PSERVICE_PARAM_USER_ID);
		String _group = _params.get(iPService.PSERVICE_PARAM_GROUP_ID);
		
		String _inv_token = _params.get(DataRobot.REST_SERVICE_INVOKE_TOKEN);
		Random rand = new Random();
		_inv_token = (_inv_token == null || "".equals(_inv_token))?"ensemble"+rand.nextInt(10000):_inv_token;
		
		_trace.user_group = _user + ":" + _group;
		
		/*
		_result.add(_ini_model);
		
		// Read parameters
		String _session = _params.get(iPService.PSERVICE_PARAM_SESSION_ID);
		String _date = _params.get(iPService.PSERVICE_PARAM_DATE);
//		String _rdf = _params.get(PSERVICE_PARAM_RDF);
//		String _uri = _params.get(PSERVICE_PARAM_URI);
		

		String vis_id = _params.get(DataRobot.REST_VISUALIZER_ID);
		
//		// read RSS from URI
//		_model = ModelFactory.createDefaultModel();
//		
//		URI checking_uri = new URI(_uri);
//		
//		InputStream in = FileManager.get().open( checking_uri.toString() );
//		_model.read(in, "");
//		in.close();
//		in = null;
//		
//		// If RDF specified add it to the model
//		if(_rdf != null && _rdf.length() >0)
//		{
//			// TODO
//			//_model.add(_rdf);
//		}
		
		
		// compile list of URI's and request to CUMULATE
		StmtIterator iter = _result.listStatements(null, RDF.type, RSS.item);
		*/
		
		
		ArrayList<ProgressEstimatorReport> reqArray = new ArrayList<ProgressEstimatorReport>();
		ArrayList<ProgressEstimatorReport> resArray = null;

		for(String url1:urls)
		{
			ProgressEstimatorReport pea = new ProgressEstimatorReport(url1);
			pea.setProgress(-1, 1);
			reqArray.add(pea);
		}
		
		
		// UM timing
		long um_start = System.nanoTime(); //currentTimeMillis();;

		URI adapt2_cumulate_reportmanager = _conf.uris.get("um");
		// send request and receive response
		URL url = new URL(adapt2_cumulate_reportmanager.toString() + "&usr=" + _user + "&grp=" + _group); 
//				"&token=" + URLEncoder.encode( _inv_token, "UTF-8"));
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
		oo = null;

		ObjectInputStream ii = new ObjectInputStream(con.getInputStream());
		resArray = (ArrayList)ii.readObject();
		ii.close();
		ii = null;

		// UM timing
		long um_finish = System.nanoTime(); //currentTimeMillis();
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.um", um_start, um_finish, um_finish - um_start, (int)resArray.size(),"arri");
		_trace.details.add(ptdi);
		

		// end of -- UM timing		
		
		// upload responce data
		
		
		Property p_el = _default.createProperty(DCTerms.educationLevel.toString());
		Property p_gp = _default.createProperty(DCTerms.audience.toString());
		Property annotation = _default.createProperty(DC.description.toString());

		String saved_state = "";
		/*
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
		*/
		
//		for(int i=0; i<resArray.size(); i++)
		for(ProgressEstimatorReport pea:resArray)
		{// for all resources
//			ProgressEstimatorReport pea = resArray.get(i);
//			Resource lo = _result.createResource(pea.getId());
//			ProgressEstimatorReport pea = new ProgressEstimatorReport(url);
			String uurl = pea.getId();


			Resource lo = _default.createResource(uurl, RSS.item);
			_default.add(lo, RSS.link, uurl);

			Literal lit_el = _default.createLiteral(Double.toString(pea.getCount(1)));
//			Literal lit_gp = _result.createLiteral(Double.toString(pea.getGroupProgress(1)));
			if(pea.getCount(1)>=0)// .getProgress(1)>=0)
				_default.add(lo, p_el, lit_el);
//			if(pea.getGroupProgress(1)>=0)
//				_result.add(lo, p_gp, lit_gp);
			
//			// add user and group ids to URL - NEW!!!!
//			String _link_prop = lo.getProperty(RSS.link).getString();
//			lo.removeAll(RSS.link);
//			lo.addProperty(RSS.link, _link_prop+"&amp;usr=" + _user + "&amp;grp=" + _group);
			// -- add user and group ids to URL - NEW!!!!

			if(_vis != null)
			{
				Annotation annot = _vis.getItemAnnotation(lo, _context_path, _trace);
				String s_annotation = annot.icon;
//				String s_style = annot.style;
				
				saved_state += lo.getURI() + "\t" + annot.summary +"\n";
				
				if(s_annotation != null && s_annotation.length() > 0)
				{
//					Literal val_annotation = _result.createLiteral(s_annotation, true);
//					_result.add(lo, annotation, val_annotation);
					_result += ((_result.length()>0)?"\n":"") + s_annotation;
				}
				
////				String s_style = _vis.getItemStyle(lo);
//				if(s_style != null && s_style.length() > 0)
//				{
//					Literal val_style = _result.createLiteral(s_style, true);
//					_result.add(lo, style, val_style);
//				}
			}
				
		}// end of -- for all resources
		
//		reqArray.clear();
//		reqArray = null;
//		
//		resArray.clear();
//		resArray = null;
		
		_trace.saved_state = saved_state; /**/
		

		return _result;
	}
	
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
		String _existing_user_id = _params.get(PSERVICE_PARAM_USER_ID);
		if( _existing_user_id == null || _existing_user_id.length() == 0)
			_params.put(PSERVICE_PARAM_USER_ID, _request.getParameter(PSERVICE_PARAM_USER_ID));
		
		String _existing_group_id = _params.get(PSERVICE_PARAM_GROUP_ID);
		if( _existing_group_id == null || _existing_group_id.length() == 0)
			_params.put(PSERVICE_PARAM_GROUP_ID, _request.getParameter(PSERVICE_PARAM_GROUP_ID));
		
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
			"		<td><textarea name='" + PSERVICE_PARAM_URI + "' cols='46' rows='5'>" + uri + "</textarea></td>\n"+
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

	public static void main(String[] args)
	{
		String s = "" + 
			"ensemble.cs.pdx.edu%2Fnode%2F360\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F355\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F354\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F353\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F352\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F351\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F350\n" +
			"ensemble.cs.pdx.edu%2Fnode%2F340";
		
	}
}
