package edu.pitt.sis.adapt2.pservice;

/*
 * TODO
 * Weighted quiz calculation
 * 
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.datamodel.ConceptItem;
import edu.pitt.sis.adapt2.pservice.datamodel.IndexedResourceItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.paws.cbum.iProgressEstimator;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorMultiLevelConceptReport;
import edu.pitt.sis.paws.cbum.report.ProgressEstimatorReport;
import edu.pitt.sis.paws.core.Item2Vector;
import edu.pitt.sis.paws.core.ItemVector;

public class ADAPT2ConceptBasedPService extends PService
{
	private Item2Vector<IndexedResourceItem> resource_list = null;
	private Item2Vector<IndexedResourceItem> resource_list_2pers = null;
	private ItemVector<ConceptItem> concept_list = null;
	private ArrayList<ProgressEstimatorReport> reqP_Array = null;
	private ArrayList<ProgressEstimatorReport> resP_Array = null;
	private ArrayList<ProgressEstimatorMultiLevelConceptReport> reqK_Array = null;
	private ArrayList<ProgressEstimatorMultiLevelConceptReport> resK_Array = null;
	
	private ArrayList<String> problem_filter_list = null;
	private ArrayList<String> example_filter_list = null;
	
	/** Uses configuration URI filters to determine whether the resource URI matches pattern(s) of an example
	 * @param _uri - resource URI
	 * @return true if resource is an example, false otherwise
	 */
	private boolean isExample(String _uri)
	{
		boolean result = false;
		for(Iterator<String> iter = example_filter_list.iterator(); iter.hasNext() && !result; )
		{
			if(_uri.indexOf(iter.next())>-1)
				result = true;
		}
		return result;
	}
	
	/** Uses configuration URI filters to determine whether the resource URI matches pattern(s) of a problem
	 * @param _uri - resource URI
	 * @return true if resource is a problem, false otherwise
	 */
	private boolean isProblem(String _uri)
	{
		boolean result = false;
		for(Iterator<String> iter = problem_filter_list.iterator(); iter.hasNext() && !result; )
		{
			if(_uri.indexOf(iter.next())>-1)
				result = true;
		}
		return result;
	}
	
	
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace) throws IOException, URISyntaxException
	{
		// 0. Seprate filters
		problem_filter_list = new ArrayList<String>();
		example_filter_list = new ArrayList<String>();
		for(Iterator<Entry<String,URI>> conf_iter = _conf.uris.entrySet().iterator(); conf_iter.hasNext(); )
		{// end of -- for all resource filters
			Entry<String,URI> entry = conf_iter.next();
			if(entry.getKey().indexOf("prob_uri_filter")>-1)
			{// for each problem filter
				problem_filter_list.add(entry.getValue().toString());
			}// end of -- for each problem filter
			if(entry.getKey().indexOf("eg_uri_filter")>-1)
			{// for each example filter
				example_filter_list.add(entry.getValue().toString());
			}// end of -- for each example filter
		}// end of -- for all resource filters

		
		// 1. Get portal resource feed
		
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
		
		// 2. Read Concept Model
		Model concept_map = ModelFactory.createDefaultModel();
		
		in = new ByteArrayInputStream(_conf.rdfs.get(0).getBytes("UTF-8"));
		concept_map.read(in, "");
		in.close();
		in = null;

		// 3. Populate resource-concept map
		resource_list = new Item2Vector<IndexedResourceItem>();
		resource_list_2pers = new Item2Vector<IndexedResourceItem>();
		concept_list = new ItemVector<ConceptItem>();
		reqK_Array = new ArrayList<ProgressEstimatorMultiLevelConceptReport>(); 
		reqP_Array = new ArrayList<ProgressEstimatorReport>(); 
		
		Seq cmap_seq = null;
		StmtIterator cmap_seq_iter = concept_map.listStatements(null, RDF.type, RDF.Seq);
		if(cmap_seq_iter.hasNext())
		{
			Statement seq_stmt = cmap_seq_iter.nextStatement();
			cmap_seq = result.getSeq(seq_stmt.getSubject());
		}
		
		for(int i=1; i<=cmap_seq.size(); i++)
		{// for all resources
			String res_uri = cmap_seq.getResource(i).toString();

			IndexedResourceItem d_res = new IndexedResourceItem(res_uri);
			resource_list.add(d_res);
			
			Resource r_res = concept_map.getResource(res_uri);
			boolean has_desc = r_res.hasProperty(DC.description);
			if(has_desc)
			{// if there's desc.
				for(StmtIterator iter=r_res.listProperties(DC.description); iter.hasNext();)
				{// over all annotations
					Statement stmt = iter.nextStatement();
					String s_concept = stmt.getProperty(DC.subject).getObject().toString();
					int i_value = stmt.getProperty(RDF.value).getInt();
					ConceptItem concept = concept_list.findByTitle(s_concept);
					if(concept==null)
					{
						concept = new ConceptItem(0, s_concept);
						concept_list.add(concept);
						
						ProgressEstimatorMultiLevelConceptReport pea = new ProgressEstimatorMultiLevelConceptReport(s_concept);
						reqK_Array.add(pea);
					}
					if(i_value==IndexedResourceItem.CONCEPT_OUTCOME)
						d_res.getOutcomeConcepts().add(concept);
					else if(i_value==IndexedResourceItem.CONCEPT_PREREQUISITE)
						d_res.getPrerequisiteConcepts().add(concept);
					
				}// end of -- over all annotations
			}// end of -- if there's desc.
		}// end of -- for all resources

		// Create collection of all resources to be personalized
		StmtIterator res_p_iter = result.listStatements(null, RDF.type, RSS.item);
		while (res_p_iter.hasNext())
		{// for all resources to be personalized 
			String res_p_uri = res_p_iter.nextStatement().getSubject().toString();
			IndexedResourceItem rr = resource_list.findByURI(res_p_uri);
			
			// vvv N CASE SOMETHING IS NOT IN CONCEPT-MAP
			if(rr == null)
				rr = new IndexedResourceItem(res_p_uri);
			// ^^^ IN CASE SOMETHING IS NOT IN CONCEPT-MAP
			
			resource_list_2pers.add(rr);
		}// end of -- for all resources to be personalized
		
		// Request progress of all resources in the course
		for(Iterator<IndexedResourceItem> res_all_iter = resource_list.iterator(); res_all_iter.hasNext(); )
		{// for all resources in the course 
			String res_all_uri = res_all_iter.next().getURI().toString();
			boolean isProblem = isProblem(res_all_uri);
			boolean isExample = isExample(res_all_uri);
			boolean isInRes2Pers = resource_list_2pers.findByURI(res_all_uri)!=null;
			
			if(isExample || (isProblem && isInRes2Pers))
			{
				ProgressEstimatorReport pea = new ProgressEstimatorReport(res_all_uri);
				reqP_Array.add(pea);
			}
		}// end of -- for all resources in the course 
		
		return result;
	}
	
	public String doPersonalize(Model _ini_model, Configuration _conf, iPServiceResultVisualizer _vis, 
			Map<String, String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException
	{
		Model _result = ModelFactory.createDefaultModel();
		_result.add(_ini_model);

//System.out.println("ADAPT2TopicQGPService.doPersonalize starting...");		
		// Read parameters
		String _user = _params.get(iPService.PSERVICE_PARAM_USER_ID);
		String _group = _params.get(iPService.PSERVICE_PARAM_GROUP_ID);
//		String _session = _params.get(iPService.PSERVICE_PARAM_SESSION_ID);
//		String _date = _params.get(iPService.PSERVICE_PARAM_DATE);
//		String _rdf = _params.get(PSERVICE_PARAM_RDF);
//		String _uri = _params.get(PSERVICE_PARAM_URI);
		String _context_path = _params.get(DataRobot.REST_CONTEXT_PATH);
		String _inv_token = _params.get(DataRobot.REST_SERVICE_INVOKE_TOKEN);
		
		_trace.user_group = _user + ":" + _group;

		// Request progress from user model
		// UM timing
		long umK_start = System.nanoTime(); //currentTimeMillis();
//http://adapt2.sis.pitt.edu/cbum/ReportManager?typ=con&dir=in&frm=dat
		
		// query for knowledge
		URI adapt2_cumulate_reportmanager_K = _conf.uris.get("um_repK");
		URL url_k = new URL(adapt2_cumulate_reportmanager_K + "&usr=" + _user + "&grp=" + _group + "&app=24" + "&token=" + URLEncoder.encode(_inv_token, "UTF-8"));
//System.out.println("url=" + url_k.toString());
		URLConnection con_k = url_k.openConnection();
		con_k.setUseCaches(false);
		con_k.setDefaultUseCaches(false);
		con_k.setDoOutput(true);
		con_k.setDoInput(true);
		con_k.setRequestProperty("Content-Type","java-internal/" + reqK_Array.getClass().getName());
		ObjectOutputStream oo_k = new ObjectOutputStream(con_k.getOutputStream());
		oo_k.writeObject(reqK_Array);
		oo_k.flush();
		oo_k.close();

		ObjectInputStream ii_k = new ObjectInputStream(con_k.getInputStream());
		resK_Array = (ArrayList)ii_k.readObject();
		ii_k.close();
		ii_k = null;
		long umK_finish = System.nanoTime(); //currentTimeMillis();
		
		long umP_start = System.nanoTime(); //currentTimeMillis();
		// query for progress
		URI adapt2_cumulate_reportmanager_P = _conf.uris.get("um_repP");
		URL url_p = new URL(adapt2_cumulate_reportmanager_P + "&usr=" + _user + "&grp=" + _group + "&token=" + URLEncoder.encode(_inv_token, "UTF-8"));
		URLConnection con_p = url_p.openConnection();
		con_p.setUseCaches(false);
		con_p.setDefaultUseCaches(false);
		con_p.setDoOutput(true);
		con_p.setDoInput(true);
		con_p.setRequestProperty("Content-Type","java-internal/" + reqP_Array.getClass().getName());
		ObjectOutputStream oo_p = new ObjectOutputStream(con_p.getOutputStream());
		oo_p.writeObject(reqP_Array);
		oo_p.flush();
		oo_p.close();

		ObjectInputStream ii_p = new ObjectInputStream(con_p.getInputStream());
		resP_Array = (ArrayList)ii_p.readObject();
		ii_p.close();
		ii_p = null;
		
		// UM timing
		long umP_finish = System.nanoTime(); //currentTimeMillis();
		
		int d_size = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.umK", umK_start, umK_finish, umK_finish - umK_start, (int)resK_Array.size(), "arri");
		_trace.details.add(ptdi);
		ptdi = new PerformanceTraceDetailItem(0, ++d_size, "pspers.umP", umP_start, umP_finish, umP_finish - umP_start, (int)resP_Array.size(), "arri");
		_trace.details.add(ptdi);
		
//		_trace.external_cost = um_finish - um_start;
//		_trace.external_ts_start = um_start;
//		_trace.external_ts_end = um_finish;
		// end of -- UM timing		
		
		// upload concept knowledge data
		for(int i=0; i<resK_Array.size(); i++)
		{
			ProgressEstimatorMultiLevelConceptReport pea = resK_Array.get(i);
//System.out.println("UM K " + pea.getId() + " progress=" + pea.getProgress(iProgressEstimator.BLOOM_IDX_COMPREHENSION));			
			ConceptItem d_concept = concept_list.findByTitle(pea.getId());
			if(d_concept != null)
			{
				// trust problem solving more
				double kn_comp = pea.getProgress(iProgressEstimator.BLOOM_IDX_COMPREHENSION);
				double kn_appl = pea.getProgress(iProgressEstimator.BLOOM_IDX_APPLICATION);
				double kn = (kn_appl==0 || kn_appl<0)?kn_comp:kn_appl;
				d_concept.setProgress(kn);
//System.out.println("CONCEPT  " + d_concept.getTitle() + " progress=" + d_concept.getProgress());
				
				// for examples declare concepts "learned" if progress >.5
				d_concept.setId( (d_concept.getProgress()>.5)?1:0 );
			}
			else
				System.out.println("ADAPT2TopicQGPService.doPersonalize Peronalized resource " + pea.getId() + " not found");
		}
		
//		// for examples declare concepts "learned" if progress >.5
//		for(Iterator<ConceptItem> c_iter = concept_list.iterator() ; c_iter.hasNext();)
//		{
//			ConceptItem c =c_iter.next();
//			c.setId( (c.getProgress()>.5)?1:0 );
//		}
		
		// upload activity progress data
		for(int i=0; i<resP_Array.size(); i++)
		{
			ProgressEstimatorReport pea = resP_Array.get(i);
			IndexedResourceItem rr = resource_list.findByURI(pea.getId());
			rr.setProgress( pea.getProgress(1/*fudge*/) );
			
//			if(resource_list_2pers.findByURI(rr.getURI()) != null)
//			{
//				System.out.println("REPORTED " + rr.getURI() + " progress=" + rr.getProgress());
//			}
		}
		
		// a. add learned by example progress
		for(Iterator<IndexedResourceItem> p_iter = resource_list.iterator() ;p_iter.hasNext() ;)
		{// for all resources in the concept map
			IndexedResourceItem res2p = p_iter.next();
			if( isExample(res2p.getURI()) )
			{// if it's an example
				int learned = 0;
				for(int i=0; i<res2p.getConcepts().size(); i++)
				{
					if(res2p.getConcepts().get(i).getId()>0)
						learned ++;
				}
				
				//		compute threshold
				double threshold = 0.0;
				threshold = (float)0.8*((res2p.getOutcomeConcepts().size()+res2p.getPrerequisiteConcepts().size() - learned)/
						(res2p.getOutcomeConcepts().size()+res2p.getPrerequisiteConcepts().size()));
				//		roughen threshold
				if(threshold<=0.3) threshold = (float)0.3;
				else if (threshold<=0.6) threshold = (float)0.6;
				else threshold = (float)1.0;
				
				//		set secondary "leaned" by example status ==2 (==1 if learned by problem solving)
				if(res2p.getProgress() >= threshold)
				{
//					res2p.setId(1);
					for(int i=0; i<res2p.getConcepts().size(); i++)
						res2p.getConcepts().get(i).setProgress( 
								(res2p.getConcepts().get(i).getId()==0)? 2 : res2p.getConcepts().get(i).getId() );
				}
			}// end of -- if it's an example
			
			/*
			for(Iterator<Entry<String,URI>> conf_iter = _conf.uris.entrySet().iterator(); conf_iter.hasNext() && (!isProblem && !isExample);)
			{// end of -- for all resource filters
				Entry<String,URI> entry = conf_iter.next();
				if(entry.getKey().indexOf("prob_uri_filter")>-1)
				{// for each problem filter
					if( res2p.getURI().toString().indexOf(entry.getValue().toString())>-1 )
					{
						isProblem = true;
					}
				}// end of -- for each problem filter
				if(entry.getKey().indexOf("eg_uri_filter")>-1)
				{// for each example filter
					if( res2p.getURI().toString().indexOf(entry.getValue().toString())>-1 )
					{
						isExample = true;
						// perform (c) example progress computation
						//		count learned concepts
						int learned = 0;
						for(int i=0; i<res2p.getConcepts().size(); i++)
						{
							if(res2p.getConcepts().get(i).getId()>0)
								learned ++;
						}
						
						//		compute threshold
						double threshold = 0.0;
						threshold = (float)0.8*((res2p.getOutcomeConcepts().size()+res2p.getPrerequisiteConcepts().size() - learned)/
								(res2p.getOutcomeConcepts().size()+res2p.getPrerequisiteConcepts().size()));
						//		roughen threshold
						if(threshold<=0.3) threshold = (float)0.3;
						else if (threshold<=0.6) threshold = (float)0.6;
						else threshold = (float)1.0;
						
						//		set secondary "leaned" by example status ==2 (==1 if learned by problem solving)
						if(res2p.getProgress() >= threshold)
						{
//							res2p.setId(1);
							for(int i=0; i<res2p.getConcepts().size(); i++)
								res2p.getConcepts().get(i).setProgress( 
										(res2p.getConcepts().get(i).getId()==0)? 2 : res2p.getConcepts().get(i).getId() );
						}
					}
				}// end of -- for each example filter
			}// end of -- for all resource filters
			/**/
		}// end of -- for all resources in the concept map
		
		
		// b. compute problem progresses = mean of outcome concept progress
		// c. compute progress of examples
		for(Iterator<IndexedResourceItem> p_iter = resource_list_2pers.iterator() ; p_iter.hasNext();)
		{// for all resources awaiting personalization
			IndexedResourceItem res2p = p_iter.next();
			boolean isProblem = isProblem(res2p.getURI());
			boolean isExample = isExample(res2p.getURI());
			
			if(isProblem)
			{
				// perform (b) problem progress computation
				double progr_sum = 0.0;
//System.out.println("COMPUTING " + res2p.getURI().toString() + " outcomes=" + res2p.getOutcomeConcepts().size() );
				for(Iterator<ConceptItem> res_c_inter = res2p.getOutcomeConcepts().iterator(); res_c_inter.hasNext(); )
				{
					ConceptItem _progr_concept = res_c_inter.next();
					progr_sum += _progr_concept.getProgress();
//System.out.println("\tconcept " + _progr_concept.getTitle() + " progress=" + _progr_concept.getProgress() );
				}
				progr_sum = (progr_sum>0) ? (double)progr_sum/res2p.getOutcomeConcepts().size() : 0;
				res2p.setProgress(progr_sum);
//System.out.println("COMPUTED " + res2p.getURI().toString() + " outcomes=" + res2p.getOutcomeConcepts().size() + " progr=" + res2p.getProgress());
			}
//			System.out.println("Progress Recomputed " + res2p.getURI() + " progress=" + res2p.getProgress());
			
			if((isExample || isProblem) && res2p.getId()!=1)
			{// it is a targeted resource
				int learned_prereqs = 0;
				for(int i=0; i<res2p.getPrerequisiteConcepts().size(); i++)
				{
					if(res2p.getPrerequisiteConcepts().get(i).getId()>0)
						learned_prereqs ++;
				}
				res2p.setId( (learned_prereqs == res2p.getPrerequisiteConcepts().size() || res2p.getProgress()>0)?1 :0 );
			}// end of -- it is a targeted resource
			
			if( !isExample && !isProblem)
			{
				res2p.setProgress(-1);
			}
			
			/*
			for(Iterator<Entry<String,URI>> conf_iter = _conf.uris.entrySet().iterator(); conf_iter.hasNext() && (!isProblem && !isExample);)
			{// end of -- for all resource filters
				Entry<String,URI> entry = conf_iter.next();
				if(entry.getKey().indexOf("prob_uri_filter")>-1)
				{// for each problem filter
					if( res2p.getURI().toString().indexOf(entry.getValue().toString())>-1 )
					{
						isProblem = true;
						// perform (b) problem progress computation
						double progr_sum = 0.0;
//System.out.println("COMPUTING " + res2p.getURI().toString() + " outcomes=" + res2p.getOutcomeConcepts().size() );
						for(Iterator<ConceptItem> res_c_inter = res2p.getOutcomeConcepts().iterator(); res_c_inter.hasNext(); )
						{
							ConceptItem _progr_concept = res_c_inter.next();
							progr_sum += _progr_concept.getProgress();
//System.out.println("\tconcept " + _progr_concept.getTitle() + " progress=" + _progr_concept.getProgress() );
						}
						progr_sum = (progr_sum>0) ? (double)progr_sum/res2p.getOutcomeConcepts().size() : 0;
						res2p.setProgress(progr_sum);
//System.out.println("COMPUTED " + res2p.getURI().toString() + " outcomes=" + res2p.getOutcomeConcepts().size() + " progr=" + res2p.getProgress());
					}
					
				}// end of -- for each problem filter
				if(entry.getKey().indexOf("eg_uri_filter")>-1)
				{// for each example filter
					if( res2p.getURI().toString().indexOf(entry.getValue().toString())>-1 )
					{
						isExample = true;
					}
				}// end of -- for each example filter
				
				if((isExample || isProblem) && res2p.getId()!=1)
				{// it is a targeted resource
					int learned_prereqs = 0;
					for(int i=0; i<res2p.getPrerequisiteConcepts().size(); i++)
					{
						if(res2p.getPrerequisiteConcepts().get(i).getId()>0)
							learned_prereqs ++;
					}
					res2p.setId( (learned_prereqs == res2p.getPrerequisiteConcepts().size() || res2p.getProgress()>0)?1 :0 );
				}// end of -- it is a targeted resource

				
			}// end of -- for all resource filters
			
			if( !isExample && !isProblem)
			{
				res2p.setProgress(-1);
			}
			/**/
		}// end of -- for all resources awaiting personalization

		
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

		
		for(Iterator<IndexedResourceItem> final_iter = resource_list_2pers.iterator(); final_iter.hasNext(); )
		{// for all folder items
			IndexedResourceItem _ri = final_iter.next();
			double res_progress = _ri.getProgress();
			Resource lo = _result.getResource(_ri.getURI());
			
//System.out.println("Res " + _ri.getURI() + " status " + _ri.getId() + " progress " + res_progress);			
			
			// '-' is added if the "ready" flag is off, namely when the "X" icon will appear
			Literal lit_el = _result.createLiteral( ((_ri.getId()==1 || res_progress==-1)?Double.toString(res_progress):"-.5") );
			_result.add(lo, DCTerms.educationLevel, lit_el);
			
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
				
			}
				
		}// end of -- // for all folder items
		
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
