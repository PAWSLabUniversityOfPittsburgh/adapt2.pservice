package edu.pitt.sis.adapt2.pservice;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestServlet
 */
public class TestServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("[PService] TestServlet vvvvv");
		Enumeration pnames = request.getParameterNames();
		for(;pnames.hasMoreElements();)
		{
			String pname = (String)pnames.nextElement();
			System.out.println("\t" + pname + " = " + request.getParameter(pname));
		}
		System.out.println("^^^^^");
	}

}
