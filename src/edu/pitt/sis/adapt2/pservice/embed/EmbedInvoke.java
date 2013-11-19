package edu.pitt.sis.adapt2.pservice.embed;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.pitt.sis.adapt2.pservice.iPService;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceDetailItem;
import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;
import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.adapt2.pservice.rest.PServiceEngineDaemon;

/**
 * Servlet implementation class for Servlet: EmbedInvoke
 * 
 */
public class EmbedInvoke extends edu.pitt.sis.adapt2.pservice.rest.RestServlet implements javax.servlet.Servlet
{
	static final long serialVersionUID = -36L;

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public EmbedInvoke()
	{
		super();
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
//	{
//		PrintWriter out = response.getWriter();
//		out.println("GUI rart of Embeds is not supported yet!");
//		out.close();
//	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 *      HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		long start = System.nanoTime(); //currentTimeMillis();
		// Create trace
		PerformanceTraceItem trace = new PerformanceTraceItem();
		trace.st = start;

		String _invoke_token =  "emb" + start;
		String _invoke_token_suffix =  "";

		String _embed_id = request.getParameter(iEmbed.EMBED_PARAM_EMBED_ID);
		String _embed_user_id = request.getParameter(iEmbed.EMBED_PARAM_USER_ID);
		String _embed_group_id = request.getParameter(iEmbed.EMBED_PARAM_GROUP_ID);
//		String _embed_fragment = request.getParameter(iEmbed.EMBED_PARAM_FRAGMENT);
		String _embed_uri = request.getParameter(iEmbed.EMBED_PARAM_URI);

		// parameters to be either preset of live from request
		String a_user_id = "", a_group_id = "", a_uri = "";
		
		// find the embed
		PServiceEngineDaemon psed = PServiceEngineDaemon.getInstance();
		iEmbed embed = psed.getEmbedList().findByTitle(_embed_id);

//		System.out.println("embed = " + embed);
//		System.out.println("embed.getURI() = " + embed.getURI());
//		System.out.println("embed.getPresetUser() = " + embed.getPresetUser());
//		System.out.println("embed.getPresetGroup() = " + embed.getPresetGroup());
		
		if(embed == null)
		{
			//OOOPS; //TODO
			System.out.println("[PERSEUS] ERROR!! Embed '" + _embed_id + "' not found.");
		}
		else
		{// resolve preset and 'live' request parameters
			a_user_id = (_embed_user_id==null || _embed_user_id.length()==0)?embed.getPresetUser():_embed_user_id;
			a_group_id = (_embed_group_id==null || _embed_group_id.length()==0)?embed.getPresetGroup():_embed_group_id;
			a_uri = (_embed_uri==null || _embed_uri.length()==0)?embed.getURI():_embed_uri;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(iPService.PSERVICE_PARAM_URI, a_uri); // TODO use RDF as well
		params.put(iPService.PSERVICE_PARAM_USER_ID, a_user_id); // TODO use RDF as well
		params.put(iPService.PSERVICE_PARAM_GROUP_ID, a_group_id); // TODO use RDF as well
		params.put(DataRobot.REST_SERVICE_ID, embed.getPServiceID());
		params.put(DataRobot.REST_CONFIGURATION_ID, embed.getConfigID());
		params.put(DataRobot.REST_VISUALIZER_ID, embed.getVisualizerID());
		params.put(DataRobot.REST_SERVICE_INVOKE_TOKEN, _invoke_token);
		params.put(DataRobot.REST_SERVICE_INVOKE_TOKEN_SUFFIX, _invoke_token_suffix);
		params.put(DataRobot.REST_CONTEXT_PATH, "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());

		params = DataRobot.doPServiceIvoke(params, this.getSQLM(), request, trace);
		String personalized_model = params.get(DataRobot.REST_RESULT);
		
		// EMBED PART
		params = embed.retrieveParams(params, request);
		int d_count = trace.details.size();
		long emb_start = System.nanoTime(); //currentTimeMillis();
		
		String result = embed.invoke(personalized_model, params);

		long emb_finish = System.nanoTime(); //currentTimeMillis();
		PerformanceTraceDetailItem ptdi = new PerformanceTraceDetailItem(0, ++d_count, "embed " + _embed_id, emb_start, emb_finish, 
				emb_finish - emb_start, (int)result.length(), "char");
		trace.details.add(ptdi);
		// -- EMBED PART
		
		
		// timing
		long finish = System.nanoTime(); //currentTimeMillis();

		trace.co = finish - start;
		trace.fi = finish;

		// recycle params object
		params.clear();
		params = null;

		PrintWriter out = response.getWriter();
		response.setContentType("text/html;no-cache;charset=UTF-8");
		out.println(result);
		out.close();
		
		long save_st = System.nanoTime();
		trace.saveToDB(this.getSQLM());
//		System.out.println("::: save trace to db took " + (double)(System.nanoTime()-save_st)/1000000 + "ms");
		// end of -- timing		
		
	}
}