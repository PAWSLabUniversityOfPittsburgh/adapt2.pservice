package edu.pitt.sis.adapt2.pservice;

//import java.util.HashMap;
//import java.io.IOException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

//import org.openrdf.sesame.config.AccessDeniedException;
//import org.openrdf.sesame.query.MalformedQueryException;
//import org.openrdf.sesame.query.QueryEvaluationException;

/**
 * @author michael_yudelson
 *
 */
public interface iPService
{
	// Constants - common parameters
	/**
	 * Http request parameter name for user identity
	 */
	public static final String PSERVICE_PARAM_USER_ID = "user_id";
	/**
	 * Http request parameter name for group identity
	 */
	public static final String PSERVICE_PARAM_GROUP_ID = "group_id";
	/**
	 * Http request parameter name for current date
	 */
	public static final String PSERVICE_PARAM_DATE = "date";
	/**
	 * Http request parameter name for user session identity
	 */
	public static final String PSERVICE_PARAM_SESSION_ID = "session_id";
	/**
	 * Http request parameter name for additional uri
	 */
	public final static String PSERVICE_PARAM_URI = "uri";
	/**
	 * Http request parameter name for supplementary rdf
	 */
	public final static String PSERVICE_PARAM_RDF = "rdf";
	
	/**
	 * Http request parameter name for knowledge domain
	 */
	public final static String PSERVICE_PARAM_DOMAIN = "dom";
	
	/** Invokes P-Service on given configuration and parameters
	 * @param _conf configuration of the P-Service
	 * @param _params set of additional parameters
	 * @return serialized RDF
	 */
	public String invoke(Configuration _conf, iPServiceResultVisualizer _vis, Map<String, String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException;
	
	/** Invokes P-Service on given configuration and parameters
	 * @param _conf configuration of the P-Service
	 * @param _params set of additional parameters
	 * @return Jena Model
	 */
	public String invokeM(Configuration _conf, iPServiceResultVisualizer _vis, Map<String, String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException;
	
	/** Pre-invokation initialization of the P-Service
	 * @param _conf configuration of the P-Service
	 * @param _params set of additional parameters
	 * @return Jena Model as described in configuration
	 */
	public Model init(Configuration _conf, Map<String, String> _params, PerformanceTraceItem _trace)
			throws IOException, URISyntaxException;
	
	/** Performs the personalization on the assembled Jena Model and given parameters
	 * @param _model assembled Jena Model
	 * @param _params parameters
	 * @return personalized Jena Model
	 */
//	public Model doPersonalize(Model _model, Configuration _conf, iPServiceResultVisualizer _vis, Map<String, 
//			String> _params, PerformanceTraceItem _trace)
//			throws URISyntaxException, IOException, ClassNotFoundException;
	
	public String doPersonalize(Model _model, Configuration _conf, iPServiceResultVisualizer _vis, Map<String, 
			String> _params, PerformanceTraceItem _trace)
			throws URISyntaxException, IOException, ClassNotFoundException;
	
	/** Retrieve a list of Http Servlet Request parameters relevant for P-Service
	 * @param _request Http Servlet Request
	 * @return a Map with list of Http Servlet Request parameters and values
	 */
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException;

	/** Constructs HTML UI for assembling a request to invoke the service
	 * @param _conf configuration of the P-Service
	 * @return HTML UI in he form of table rows and cells between and HTML form
	 */
	public String getInvokerUI(Configuration _conf);
	
}
