package edu.pitt.sis.adapt2.pservice.rest;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AnnotationExplorationAJAXRobotRecorder extends RestServlet implements Servlet
{
	static final long serialVersionUID = 1L;
	
	private static final String PARAM_TIME_OBSERVED = "t";
	private static final String PARAM_URI_ID = "id";
	private static final String PARAM_TRACE_REFERENCE = "r";
	private static final String PARAM_PSERVICE = "s";
	private static final String PARAM_USER_GROUP = "u";
	private static final String PARAM_ANNOTATION_TEXT = "a";

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String s_time = request.getParameter(PARAM_TIME_OBSERVED);
		String s_trqace_ref = request.getParameter(PARAM_TRACE_REFERENCE);
		String s_user_group = request.getParameter(PARAM_USER_GROUP);
		String s_pservice = request.getParameter(PARAM_PSERVICE);
		String s_uri = request.getParameter(PARAM_URI_ID);
		String s_annotation_text = request.getParameter(PARAM_ANNOTATION_TEXT);
		s_uri = (s_uri!=null)?URLDecoder.decode(s_uri,"UTF-8"):s_uri;
		
//		System.out.println("[AnnotRobo] time reported=" + s_time + "ms trace id = " + s_trqace_ref +
//				" user_group=" + s_user_group + " service_id=" + s_pservice + " uri=" + s_uri);
		
		Connection conn = null;
		Statement stmt = null;
		String qry = "";
		try
		{
			conn = this.getSQLM().getConnection();
			qry = "INSERT INTO log_annot_explore (ServiceInvokeNS,UserGroup,Service_rdfID,TimeSpentMS,ResourceURI,AnnotationText)" +
					" VALUES(" + s_trqace_ref + ",'" + s_user_group + "','" + s_pservice + "'," + s_time+ ",'" + s_uri + "','" + s_annotation_text + "')";
			stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			
			stmt.close();
			stmt = null;
			conn.close();
			conn = null;
		}
		catch(SQLException sqle) { sqle.printStackTrace(System.out); }

	}
}