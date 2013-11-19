package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Map;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RSS;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * Abstract Personalization Service class
 * @author michael_yudelson
 *
 */

public abstract class PService implements iPService
{
//	public Model invokeM(Configuration _conf, iPServiceResultVisualizer _vis, Map<String, String> _params,
//			PerformanceTraceItem _trace) throws URISyntaxException, IOException, ClassNotFoundException
	public String invokeM(Configuration _conf, iPServiceResultVisualizer _vis, Map<String, String> _params,
			PerformanceTraceItem _trace) throws URISyntaxException, IOException, ClassNotFoundException
	{
		// Initialize PService
		long ini_start = System.nanoTime(); //currentTimeMillis();
		
		Model ini_model = init(_conf, _params, _trace);
		
		long ini_finish = System.nanoTime();
		
		int d_count = _trace.details.size();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_count, "psinit", ini_start, ini_finish, 
				ini_finish - ini_start, (int)ini_model.size(), "rdf3");
		_trace.details.add(ptdi);
		
		// Personalize model
		long pers_start = System.nanoTime();

//		// == old
//		Model pers_model = doPersonalize(ini_model, _conf, _vis, _params, _trace);
		// == new
		String result = doPersonalize(ini_model, _conf, _vis, _params, _trace); 
		
		long pers_finish = System.nanoTime();

//		ptdi = new PerformanceTraceDetailItem(0, _trace.details.size()+1, "pspers", pers_start, pers_finish, 
//				pers_finish - pers_start, (int)pers_model.size(), "rdf3");
		ptdi = new PerformanceTraceDetailItem(0, _trace.details.size()+1, "pspers", pers_start, pers_finish, 
				pers_finish - pers_start, result.length(), "char");
		_trace.details.add(ptdi);
//		_trace.sz = (int)pers_model.size();
		_trace.sz = result.length();
		
		// Recycle inited model
		ini_model.close();
		ini_model = null;

//		// == old
//		return pers_model;
		// == new
		 return result;
	}
	
	public String invoke(Configuration _conf, iPServiceResultVisualizer _vis, Map<String, String> _params,
			PerformanceTraceItem _trace) throws URISyntaxException, IOException, ClassNotFoundException
	{
//		// == old
//		Model model = invokeM(_conf, _vis, _params, _trace);
//
//		StringWriter sw = new StringWriter();
//		
//		RDFWriter w = model.getWriter("RDF/XML-ABBREV");
//		w.setProperty("prettyTypes", new Resource[]{RSS.channel, RSS.item});
//		w.setProperty("blockRules", "propertyAttr");
//		model.write(sw,"RDF/XML-ABBREV");
//		String result = sw.toString();
//			
//		model.close();
//		model = null;
//		
//		return result;
		
		// == new
		 return invokeM(_conf, _vis, _params, _trace);
		
	}
	
	public String Model2String(Model _model)
	{
		StringWriter sw = new StringWriter();
		
		RDFWriter w = _model.getWriter("RDF/XML-ABBREV");
		w.setProperty("prettyTypes", new Resource[]{RSS.channel, RSS.item});
		w.setProperty("blockRules", "propertyAttr");
		_model.write(sw,"RDF/XML-ABBREV");
		String result = sw.toString();
			
		_model.close();
		_model = null;
		
		return result;
	}
}

