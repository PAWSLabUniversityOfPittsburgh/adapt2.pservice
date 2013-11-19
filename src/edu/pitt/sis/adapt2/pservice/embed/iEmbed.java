package edu.pitt.sis.adapt2.pservice.embed;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import edu.pitt.sis.paws.core.iItem;

public interface iEmbed extends iItem, Serializable
{
	// Constants - common parameters
	/**
	 * Http request parameter name for user identity
	 */
	public static final String EMBED_PARAM_EMBED_ID = "id";
	/**
	 * Http request parameter name for user identity
	 */
	public static final String EMBED_PARAM_USER_ID = "user_id";
	/**
	 * Http request parameter name for group identity
	 */
	public static final String EMBED_PARAM_GROUP_ID = "group_id";	
	/**
	 * Http request parameter name for knowledge domain
	 */
	public final static String EMBED_PARAM_FRAGMENT = "fragment";
	/**
	 * Http request parameter name for knowledge domain
	 */
	public final static String EMBED_PARAM_URI = "uri";


	/** Retrieve a list of Http Servlet Request parameters relevant for Embed
	 * @param _request Http Servlet Request
	 * @return a Map with list of Http Servlet Request parameters and values
	 */
	public Map<String, String> retrieveParams(Map<String, String> _params, HttpServletRequest _request) throws UnsupportedEncodingException;

	/** Invokes Embed on given configuration and parameters
	 */
	public String invoke(String personalized_model, Map<String, String> _params) throws IOException, UnsupportedEncodingException;

	public ArrayList<Integer> parseFragments(String _framgents_param);
	
	public String getPServiceID();
	public String getConfigID();
	public String getVisualizerID();

	public String getURI();
	public String getRDF();

	public String getPresetUser();
	public String getPresetGroup();

}
