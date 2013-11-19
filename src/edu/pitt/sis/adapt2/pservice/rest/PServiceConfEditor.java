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
public class PServiceConfEditor extends RestServlet
{
	static final long serialVersionUID = -2L;
		
   /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PServiceConfEditor()
	{
		super();
	}   	

		
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String _service_id = request.getParameter(DataRobot.REST_SERVICE_ID);
		String _conf_id = request.getParameter(DataRobot.REST_CONFIGURATION_ID);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataRobot.REST_SERVICE_ID, _service_id);
		params.put(DataRobot.REST_CONFIGURATION_ID, _conf_id);
		params.put(DataRobot.REST_CONTEXT_PATH, "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());

		params = DataRobot.getPServiceConfEditor(params, this.getSQLM(), request);
		String result = params.get(DataRobot.REST_RESULT);
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;no-cache;charset=UTF-8");
		out.println(result);
		out.close();
	}  	
	
}