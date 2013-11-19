package edu.pitt.sis.adapt2.pservice.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class for Servlet: RestUser
 *
 */
public class PServiceView extends RestServlet
{
	static final long serialVersionUID = -2L;
		
   /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PServiceView()
	{
		super();
	}   	

		
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String _service_id = request.getParameter(DataRobot.REST_SERVICE_ID);
		String _format = request.getParameter(DataRobot.REST_FORMAT);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataRobot.REST_SERVICE_ID, _service_id);
		params.put(DataRobot.REST_FORMAT, _format);
		params.put(DataRobot.REST_CONTEXT_PATH, "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());

		params = DataRobot.getPServiceInfo(params, this.getSQLM(), false /*multiple pservices*/, request);
		String result = params.get(DataRobot.REST_RESULT);
		
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
		String _service_id = request.getParameter(DataRobot.REST_SERVICE_ID);
		String _service_rdf_id = request.getParameter(DataRobot.REST_SERVICE_RDF_ID);

		String _name = request.getParameter(DataRobot.REST_SERVICE_NAME);
		String _class_name = request.getParameter(DataRobot.REST_SERVICE_CLASSNAME);
		String _desc = request.getParameter(DataRobot.REST_SERVICE_DESCRIPTION);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataRobot.REST_CONTEXT_PATH, 
				"http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());
		params.put(DataRobot.REST_SERVICE_ID, _service_id);
		params.put(DataRobot.REST_SERVICE_RDF_ID, _service_rdf_id);
		params.put(DataRobot.REST_SERVICE_NAME, _name);
		params.put(DataRobot.REST_SERVICE_CLASSNAME, _class_name);
		params.put(DataRobot.REST_SERVICE_DESCRIPTION, _desc);
		
		params = DataRobot.setPServiceInfo(params, this.getSQLM(), false /*multiple pservices*/);
		String result = params.get(DataRobot.REST_RESULT);
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;no-cache;charset=UTF-8");
		out.println(result);
		out.close();
	}   	  	    
}