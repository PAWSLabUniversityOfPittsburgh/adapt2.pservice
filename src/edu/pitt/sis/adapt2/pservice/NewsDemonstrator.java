package edu.pitt.sis.adapt2.pservice;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

public class NewsDemonstrator extends PService
{
	// Constants
	public final static String PSERVICE_PARAM_SORTING = "sort";
	public final static String PSERVICE_PARAM_SORTING_ALFA_INC = "alfa_inc";
	public final static String PSERVICE_PARAM_SORTING_ALFA_DEC = "alfa_dec";
	public final static String NEWS_DEMONSTRATOR_KEYWORDS = "http://adapt2.sis.pitt.edu/pservice/setup";
	public final static String ADAPT2_CUMULATE_REPORTMNAGER = "http://localhost:8080/cbum/ReportManager?typ=act&dir=in&frm=dat";
	
//	// Variables
	protected URL feed_url;
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace)
//			throws IOException, AccessDeniedException, MalformedQueryException, QueryEvaluationException
	{
		try
		{
//			System.out.println("feed " + _conf.uri_list.get(0));
			feed_url = _conf.uris.get("rss1_2conv").toURL();
		}
		catch(MalformedURLException mue)
		{
			mue.printStackTrace(System.out);
		}
		
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
//			signature+=_conf.uri_list.size() + ".";
			
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
			
//			// 3. load RDF from datasources as per context(s)
//			for(Iterator<PServiceDataSource> itds=_conf.datasource_list.iterator(); itds.hasNext(); )
//			{// for all datasources
//				PServiceDataSource psds = itds.next();
//				
//				for(Iterator<URI> itc=_conf.context_list.iterator(); itc.hasNext(); )
//				{// for all contexts
//					String context = itc.next().toString();
//					if(psds.type == PServiceDataSource.PSERVICE_DATASOURCE_SESAME)
//					{
//						// query repository
//						String qry =
//							"SELECT T\n" +
//							"FROM {C} rdf:type {rdfs:Class},\n" +
//							"	{C} dc:relation {T}\n" + //, 
////							"	{T} rdf:type {rss:channel}\n" +
//							"WHERE C = <" + context + ">\n" +
//							"USING NAMESPACE\n" +
//							"	dc = <http://purl.org/dc/elements/1.1/>,\n" +
//							"	rss = <http://purl.org/rss/1.0/>";
//						ArrayList<ArrayList<String>> result = psds.executeTableQuery(qry);
//						// pull the info
//						InputStream in = null; 
//						String uri = "";
//						for(Iterator<ArrayList<String>> it_r=result.iterator(); it_r.hasNext(); )
//						{
//							uri = it_r.next().get(0);
//							in = FileManager.get().open( uri.toString() );
//							model.read(in, "");
//							in.close();
//							in = null;
//						}
//					}
//				}// end of -- for all contexts
//			}// end of --for all datasources
//			signature+=_conf.context_list.size() + ".";
//			// 4. add filters to the model
//			//TODO
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
		/**/
		
		return model;
	}
	
	public String doPersonalize(Model _model, Configuration _conf, iPServiceResultVisualizer _vis, 
			Map<String, String> _params, PerformanceTraceItem _trace)
	{
//		Model pers_model = ModelFactory.createDefaultModel();
//		pers_model.add(_model);

		// Read parameters
//		String _sorting = _params.get(PSERVICE_PARAM_SORTING);
		
		// read RSS from URI
		Model _feed_model = ModelFactory.createDefaultModel();
		
		try
		{
			InputStream in = FileManager.get().open( feed_url.toString() );
			_feed_model.read(in, "");
			in.close();
			in = null;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		
		// Compile a list of keywords
		ArrayList<String> keywords = new ArrayList<String>();
		
		Resource _o = _model.getResource(NEWS_DEMONSTRATOR_KEYWORDS);
		StmtIterator iter = _model.listStatements(_o, DC.subject, (RDFNode)null);
		while(iter.hasNext())
		{
			String kw = iter.nextStatement().getObject().toString();
			keywords.add(kw);
			System.out.println("keyword " + kw );
		}
		
		// Transform the feed
		ResIterator channels = _feed_model.listSubjectsWithProperty(RDF.type, RSS.channel);
		Resource channel = null;
		if (channels.hasNext())
			channel = (Resource)channels.next();
		if (channel != null && channel.hasProperty(RSS.items))
		{
			Seq items = channel.getProperty(RSS.items).getSeq();
			for (int i=1; i<= items.size(); i++)
			{		
				Resource res_item = items.getResource(i);
				String desc_prop = res_item.getProperty(RSS.description).getString();
				String title_prop = res_item.getProperty(RSS.title).getString();
				String new_desc = desc_prop;
				String new_title = title_prop;
				Iterator<String> kw_iter = keywords.iterator();
				while(kw_iter.hasNext())
				{
					String kw = kw_iter.next();
//					System.out.println("Index of " + kw + " " + desc_prop.indexOf(kw));
					new_desc = new_desc.replaceAll(kw, "<span style='background-color:red'>" + kw + "</span>");
					new_title = new_title.replaceAll(kw, "<span style='background-color:red'>" + kw + "</span>");
				}
				res_item.removeAll(RSS.description);
				res_item.addProperty(RSS.description, new_desc);
				res_item.removeAll(RSS.title);
				res_item.addProperty(RSS.title, new_title);
			}
		}
		
		return Model2String(_feed_model);
	}
	
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
		String _sorting = _request.getParameter(PSERVICE_PARAM_SORTING);
		if(_sorting != null && _sorting.length()>0)
			_params.put(PSERVICE_PARAM_SORTING, URLDecoder.decode(_sorting, "UTF-8"));
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
			"	<tsr>\n"+
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
