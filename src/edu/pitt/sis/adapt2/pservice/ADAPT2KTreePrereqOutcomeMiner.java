package edu.pitt.sis.adapt2.pservice;

/*
 * TODO
 * Weighted quiz calculation
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTypes;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;

import edu.pitt.sis.adapt2.pservice.datamodel.ConceptItem;
import edu.pitt.sis.adapt2.pservice.datamodel.IndexedResourceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.ResourceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.paws.core.Item;
import edu.pitt.sis.paws.core.Item2Vector;
import edu.pitt.sis.paws.core.ItemVector;
import edu.pitt.sis.paws.core.iHierarchicalItem2;


public class ADAPT2KTreePrereqOutcomeMiner extends PService
{
	private Item2Vector<ResourceItem> folder_item_list = null;
	private Item2Vector<IndexedResourceItem> resource_list = null;
//	private ArrayList<ProgressEstimatorReport> reqArray = null;
//	private ArrayList<ProgressEstimatorReport> resArray = null;
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace) throws IOException, URISyntaxException
	{
		String _uri = _params.get(PSERVICE_PARAM_URI);
		// read RSS from URI
		Model result = ModelFactory.createDefaultModel();
		
		// master URI
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

//System.out.println("ADAPT2TopicQGPService.doPersonalize model read of size=" + top_model.size());		
		
		folder_item_list = new Item2Vector<ResourceItem>();
		resource_list = new Item2Vector<IndexedResourceItem>();
//		reqArray = new ArrayList<ProgressEstimatorReport>();
		
		// OUT OF ORDER
//		StmtIterator top_iter = result.listStatements(null, RDF.type, DCTypes.Collection);
		
		// IN ORDER
		Seq res_seq = null;
		Seq sub_seq = null;
		StmtIterator top_iter_seq = result.listStatements(null, RDF.type, RDF.Seq);
		if(top_iter_seq.hasNext())
		{
			Statement seq_stmt = top_iter_seq.nextStatement();
			res_seq = result.getSeq(seq_stmt.getSubject());
		}
		
//		try
//		{
		//Dependant folder model URIs
		
		// OUT OF ORDER
//		while (top_iter.hasNext())
		for(int i=1; i<=res_seq.size(); i++)
		{// for all folders
			// OUT OF ORDER
//			String folder_uri = top_iter.nextStatement().getSubject().toString();
			// IN ORDER
			Resource ress = res_seq.getResource(i);
			if(! result.contains(ress, RDF.type, DCTypes.Collection))
				continue;
			String folder_uri = ress.toString();
			
			ResourceItem fi = new ResourceItem(folder_uri);
			folder_item_list.add(fi);
			
			// get quizzes into a map
			Model sub_model = ModelFactory.createDefaultModel();
			URI folder_checking_uri = new URI(folder_uri);

//System.out.println("folder_checking_uri=" + folder_checking_uri);
			
			conn = folder_checking_uri.toURL().openConnection();
			in = conn.getInputStream();
			
//			in = FileManager.get().open( folder_checking_uri.toString() );
			sub_model.read(in, "");
			in.close();
			in = null;

			// OUT OF ORDER
//			StmtIterator sub_iter = sub_model.listStatements(null, RDF.type, RSS.item);
			// IN ORDER
			StmtIterator sub_iter_seq = sub_model.listStatements(null, RDF.type, RDF.Seq);
			if(sub_iter_seq.hasNext())
			{
				Statement seq_stmt = sub_iter_seq.nextStatement();
				sub_seq = result.getSeq(seq_stmt.getSubject());
			}
			NodeIterator sub_iter = sub_seq.iterator();
			
			while (sub_iter.hasNext())
			{// for all sub-items
				// OUT OF ORDER
//				String quiz_uri = sub_iter.nextStatement().getSubject().toString();
				// IN ORDER
				String quiz_uri = sub_iter.nextNode().toString();
				
				boolean filtered = false;
				int no_filters = 0;
				for(Iterator<Entry<String,URI>> iter = _conf.uris.entrySet().iterator(); iter.hasNext();)
				{
					Entry<String,URI> entry = iter.next();
					no_filters += ("uri_filter".equals(entry.getKey()))?1:0;
					if( ("uri_filter".equals(entry.getKey())) && (quiz_uri.indexOf(entry.getValue().toString()) > -1) )
					{
						filtered = true;
						break;
					}
				}
				if(no_filters==0) filtered = true;
				if(filtered) // if it is in the filter(s)
//				if(quiz_uri.indexOf("/sqlknot/") >0) // if it is in the filter(s)
				{
					IndexedResourceItem res = resource_list.findByURI(quiz_uri);
					if(res==null)
					{
						res = new IndexedResourceItem(quiz_uri);
						resource_list.add(res);
//						ProgressEstimatorReport pea = new ProgressEstimatorReport(quiz_uri, -1, -1.0, -1.0, -1.0);
//						reqArray.add(pea);
					}
					fi.getSubs().add(res);
				}
			}// end of --  for all sub-items
			
			// recycle sub_model
			sub_model.close();
			sub_model = null;
		}// end of -- for all folders
		
//		StringBuffer sb = new StringBuffer();
//		for(int i=0; i<resource_list.size(); i++)
//		{
//			sb.append(((sb.length()>0)?",":"") + URLEncoder.encode(resource_list.get(i).getURI(), "UTF-8"));
//		} 
//		System.out.println("\n" + sb.toString() + "\n");
//		System.out.println(sb.toString().length());
		
		return result;
	}
	
	public String doPersonalize(Model _ini_model, Configuration _conf, iPServiceResultVisualizer _vis, 
			Map<String, String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException
	{
		Model _result = ModelFactory.createDefaultModel();
		_result.add(_ini_model);
		
		// Read parameters
		String _domain = _params.get(iPService.PSERVICE_PARAM_DOMAIN);
//		String _context_path = _params.get(DataRobot.REST_CONTEXT_PATH);
		String _inv_token = _params.get(DataRobot.REST_SERVICE_INVOKE_TOKEN);
		
		_trace.user_group = "no_user_necessary";

		// Request progress from user model
		// UM timing
		long um_start = System.nanoTime(); //currentTimeMillis();
		
		//http://localhost:8080/cbum/ReportManager?typ=act2con&dir=in&frm=rdf&app=-1
		//act=1,2,3 dom
		URI adapt2_cumulate_reportmanager = _conf.uris.get("um_rep");
		
		StringBuffer act_list = new StringBuffer();
		for(int i=0; i<resource_list.size(); i++)
		{
			act_list.append(((act_list.length()>0)?",":"") + URLEncoder.encode(resource_list.get(i).getURI(), "UTF-8"));
		} 
		
		String params = "&" + URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(_inv_token, "UTF-8") +
			"&" + URLEncoder.encode("dom", "UTF-8") + "=" + URLEncoder.encode(_domain, "UTF-8") +
			"&" + URLEncoder.encode("act", "UTF-8") + "=" + act_list.toString();
//System.out.println("\n" + act_list.toString() + "\n");
		
		URL url = new URL(adapt2_cumulate_reportmanager + "");
		
		URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(params);
        wr.flush();

        // Get the response
		InputStream in  = conn.getInputStream();
		Model metadata_model = ModelFactory.createDefaultModel();
		metadata_model.read(in, "");
		in.close();
        wr.close();
        
		// UM timing
		long um_finish = System.nanoTime(); //currentTimeMillis();
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.um", um_start, um_finish, um_finish - um_start, (int)metadata_model.size(), "rdf3");
		_trace.details.add(ptdi);
//		_trace.external_cost = um_finish - um_start;
//		_trace.external_ts_start = um_start;
//		_trace.external_ts_end = um_finish;
		// end of -- UM timing		

        // Extract indexing concepts
        ItemVector<ConceptItem> concept_list = new ItemVector<ConceptItem>();
        StmtIterator iter = metadata_model.listStatements(null, DC.subject, (RDFNode)null);
        while(iter.hasNext())
        {
        	Statement stmt = iter.nextStatement();
        	IndexedResourceItem ires = resource_list.findByURI(stmt.getSubject().toString());
        	String concept = stmt.getObject().toString();
        	
        	ConceptItem new_concept = concept_list.findByTitle(concept);
        	if(new_concept == null)
        	{
        		new_concept = new ConceptItem(0, concept);
            	concept_list.add(new_concept);
        	}
        	
        	ires.getConcepts().add(new_concept);
        }
        
		// Perform prerequisite-outcome mining
		for(Iterator<ResourceItem> ifolder = folder_item_list.iterator(); ifolder.hasNext();)
		{// for all folders
			ResourceItem folder = ifolder.next();
			// 1. divide prerequisites/outputs
			Set<Item> all_folder_concepts = new HashSet<Item>();
			for(Iterator<iHierarchicalItem2> ires = folder.getSubs().iterator(); ires.hasNext();)
			{
				IndexedResourceItem res = (IndexedResourceItem)ires.next();
				all_folder_concepts.addAll(res.getConcepts());
				res.splitConcepts();
//				System.out.println("|> " + res.getURI() + " c=" + res.getConcepts().size() + 
//						" p=" + res.getPrerequisiteConcepts().size() +
//						" o=" + res.getOutcomeConcepts().size());
			}
			// 2. tag all concepts of the folder
			for(Iterator<Item> iconcept = all_folder_concepts.iterator(); iconcept.hasNext(); )
				iconcept.next().setId(IndexedResourceItem.CONCEPT_LEARNED);
			
			all_folder_concepts.clear();
			all_folder_concepts = null;
		}// end of -- for all folders
        
		// Create indexing model 
		String lom_ns = "http://purl.org/lom/terms/";
		Model _indexed_model = ModelFactory.createDefaultModel();
		Resource anno_type = _indexed_model.createResource(lom_ns + "Annotation");
		
		// ADD SEQ
		Seq _idx_seq = _indexed_model.createSeq();
		int seq_counter = 0;
		
		for(Iterator<IndexedResourceItem> _n_res = resource_list.iterator(); _n_res.hasNext();)
		{// for all resources
			IndexedResourceItem ri = _n_res.next();
			Resource r = _indexed_model.createResource(ri.getURI());
			
			// Add sequencing
			_idx_seq.add(++seq_counter, r);
			
			for(Iterator<ConceptItem> _n_p_iter = ri.getPrerequisiteConcepts().iterator(); _n_p_iter.hasNext(); )
			{
//				Property eD = _indexed_model.createProperty(lom_ns, "educationalDescription");
				Resource annot = _indexed_model.createResource(anno_type);
				
				_indexed_model.add(_indexed_model.createStatement(r, DC.description, annot));
				_indexed_model.add(_indexed_model.createStatement(annot, DC.subject, _n_p_iter.next().getTitle()));
				_indexed_model.add(_indexed_model.createStatement(annot, RDF.value, String.valueOf(IndexedResourceItem.CONCEPT_PREREQUISITE)));
			}
			for(Iterator<ConceptItem> _n_o_iter = ri.getOutcomeConcepts().iterator(); _n_o_iter.hasNext(); )
			{
//				Property eD = _indexed_model.createProperty(lom_ns, "educationalDescription");
				Resource annot = _indexed_model.createResource(anno_type);
				
				_indexed_model.add(r, DC.description, annot);
				_indexed_model.add(annot, DC.subject, _n_o_iter.next().getTitle());
				_indexed_model.add(annot, RDF.value, String.valueOf(IndexedResourceItem.CONCEPT_OUTCOME));
			}
		}// end of -- for all resources
		
//		System.out.println("_indexed_model size=" + _indexed_model.size() + "  res#=" + resource_list.size());
		_result = _indexed_model;
		
//		<rdf:Description rdf:about='http://www.sis.pitt.edu/~paws/ont/quizpack.rdf#for1'>
//			<dc:subject>main_function</dc:subject>
//			<dc:subject>int</dc:subject>
//			<dc:subject>variable_declaration</dc:subject>
//			<dc:subject>for</dc:subject>
//			<lom:description>
//				<lom:Annotation>
//					<dc:subject>main_function</dc:subject>
//					<rdf:value>1</rdf:value>
//				</lom:Annotation>
//			</lom:description>	
//		</rdf:Description>

		return Model2String(_result);
	}
	
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException
	{
		_params.put(PSERVICE_PARAM_USER_ID, _request.getParameter(PSERVICE_PARAM_USER_ID));
		_params.put(PSERVICE_PARAM_DOMAIN, _request.getParameter(PSERVICE_PARAM_DOMAIN));
		_params.put(PSERVICE_PARAM_GROUP_ID, _request.getParameter(PSERVICE_PARAM_GROUP_ID));
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
		String dom = "";
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
			"		<td valign='top'>Domain</td>\n"+
			"		<td><input name='" + PSERVICE_PARAM_DOMAIN + "' type='text' maxlength='255' size='50' value='" + dom + "'/></td>\n"+
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

