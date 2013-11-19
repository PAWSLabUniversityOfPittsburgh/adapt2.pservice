package edu.pitt.sis.adapt2.pservice.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.adapt2.pservice.datamodel.PerformanceTraceItem;

/**
 * Servlet implementation class for Servlet: RestUser
 *
 */
public class PServiceInvoke extends RestServlet
{
	static final long serialVersionUID = -2L;
		
   /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PServiceInvoke()
	{
		super();
	}   	

		
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String _service_id = DataRobot.clrStr(request.getParameter(DataRobot.REST_SERVICE_ID));
		String _conf_id = DataRobot.clrStr(request.getParameter(DataRobot.REST_CONFIGURATION_ID));
		String _vis_id = DataRobot.clrStr(request.getParameter(DataRobot.REST_VISUALIZER_ID));
		String _invoke_token =  DataRobot.clrStr( request.getParameter(DataRobot.REST_SERVICE_INVOKE_TOKEN) );
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataRobot.REST_SERVICE_ID, _service_id);
		params.put(DataRobot.REST_CONFIGURATION_ID, _conf_id);
		params.put(DataRobot.REST_VISUALIZER_ID, _vis_id);
		params.put(DataRobot.REST_SERVICE_INVOKE_TOKEN, _invoke_token);
		params.put(DataRobot.REST_CONTEXT_PATH, "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());

		params = DataRobot.getPServiceIvokeUI(params, this.getSQLM(), request);
		String result = params.get(DataRobot.REST_RESULT);
		
		// recycle params object
		params.clear();
		params = null;
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;no-cache;charset=UTF-8");
		out.println(result);
		out.close();
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		long start = System.nanoTime(); //currentTimeMillis();
		// Create trace
		PerformanceTraceItem trace = new PerformanceTraceItem();
		trace.st = start;

		String _service_id = request.getParameter(DataRobot.REST_SERVICE_ID);
		String _conf_id = request.getParameter(DataRobot.REST_CONFIGURATION_ID);
		String _vis_id = request.getParameter(DataRobot.REST_VISUALIZER_ID);
		String _invoke_token =  DataRobot.clrStr( request.getParameter(DataRobot.REST_SERVICE_INVOKE_TOKEN) );
		String _invoke_token_suffix =  DataRobot.clrStr( request.getParameter(DataRobot.REST_SERVICE_INVOKE_TOKEN_SUFFIX) );

		Map<String, String> params = new HashMap<String, String>();
		params.put(DataRobot.REST_SERVICE_ID, _service_id);
		params.put(DataRobot.REST_CONFIGURATION_ID, _conf_id);
		params.put(DataRobot.REST_VISUALIZER_ID, _vis_id);
		params.put(DataRobot.REST_SERVICE_INVOKE_TOKEN, _invoke_token);
		params.put(DataRobot.REST_SERVICE_INVOKE_TOKEN_SUFFIX, _invoke_token_suffix);
		params.put(DataRobot.REST_CONTEXT_PATH, "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());

		params = DataRobot.doPServiceIvoke(params, this.getSQLM(), request, trace);
		String result = params.get(DataRobot.REST_RESULT);
		
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
		System.out.println("::: save trace to db took " + (double)(System.nanoTime()-save_st)/1000000 + "ms");
		// end of -- timing		
	}   	  	    
}