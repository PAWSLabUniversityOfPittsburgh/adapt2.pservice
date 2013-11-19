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
public class PServiceEditor extends RestServlet
{
	static final long serialVersionUID = -2L;
		
   /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PServiceEditor()
	{
		super();
	}   	

		
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String _service_id = request.getParameter(DataRobot.REST_SERVICE_ID);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(DataRobot.REST_SERVICE_ID, _service_id);
		params.put(DataRobot.REST_CONTEXT_PATH, "http://" + request.getServerName() + 
				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());

		params = DataRobot.getPServiceEditor(params, this.getSQLM(), request);
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
//		String _user_id = request.getParameter(DataRobot.REST_USER_ID);
//		String _user_login = request.getParameter(DataRobot.REST_USER_LOGIN);
//
//		String _login = request.getParameter(DataRobot.REST_USER_F_LOGIN);
//		String _name = request.getParameter(DataRobot.REST_USER_F_NAME);
//		String _email = request.getParameter(DataRobot.REST_USER_F_EMAIL);
//		String _org = request.getParameter(DataRobot.REST_USER_F_ORGANIZATION);
//		String _country = request.getParameter(DataRobot.REST_USER_F_COUNTRY);
//		String _city = request.getParameter(DataRobot.REST_USER_F_CITY);
//		String _how = request.getParameter(DataRobot.REST_USER_F_HOW);
//		
//		Map<String, String> params = new HashMap<String, String>();
//		params.put(DataRobot.REST_USER_ID, _user_id);
//		params.put(DataRobot.REST_USER_LOGIN, _user_login);
//		params.put(DataRobot.REST_CONTEXT_PATH, 
//				"http://" + request.getServerName() + 
//				((request.getLocalPort() != 80)?":"+ request.getLocalPort():"") + request.getContextPath());
//
//		params.put(DataRobot.REST_USER_F_LOGIN, _login);
//		params.put(DataRobot.REST_USER_F_NAME, _name);
//		params.put(DataRobot.REST_USER_F_EMAIL, _email);
//		params.put(DataRobot.REST_USER_F_ORGANIZATION, _org);
//		params.put(DataRobot.REST_USER_F_COUNTRY, _country);
//		params.put(DataRobot.REST_USER_F_CITY, _city);
//		params.put(DataRobot.REST_USER_F_HOW, _how);
//		
//		params = DataRobot.setUserInfo(params, this.getSqlManager(), false /*multiple users*/);
//		String result = params.get(DataRobot.REST_RESULT);
//		
//		PrintWriter out = response.getWriter();
//		out.println(result);
	}   	  	    
}