package edu.pitt.sis.adapt2.pservice.flooder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.adapt2.pservice.rest.RestServlet;

/**
 * Servlet implementation class for Servlet: RSS4Flooder
 *
 */
 public class RSS4Flooder extends RestServlet
 {
	static final long serialVersionUID = -2L;
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public RSS4Flooder() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String number_s = request.getParameter("num");
		String short_list = "";
		String long_list = "";
		
		int number_i = Integer.parseInt(number_s);
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String qry = "SELECT n.* FROM portal_test2.rel_node_node nn JOIN portal_test2 .ent_node n " +
				"ON(nn.ChildNodeID=n.NodeID) WHERE nn.ParentNodeID IN (2466,2477,2489,2503,2417) " +
				"AND Title NOT LIKE '%Lecture%';";
		try
		{
			conn = this.getSQLM().getConnection();
			stmt = conn.prepareStatement(qry);
			rs = stmt.executeQuery();
			int count = 0;
			while(rs.next() && (count<number_i))
			{
				String title = rs.getString("Title");	
				String uri = rs.getString("URI").replaceAll("&", "&amp;");	
				String url = rs.getString("URL").replaceAll("&", "&amp;");	
				short_list += "				<rdf:li rdf:resource='" + uri + "'/>\n";
				long_list += 
					"	<item rdf:about='" + uri + "'>\n" +
					"		<title rdf:parseType='Literal'>" + title + "</title>\n" +
					"		<link>" + url + "</link>\n" +
					"	</item>\n";
				count ++;
			}
		}
		catch(Exception e) { e.printStackTrace(System.out); }
		finally
		{
			try { if (conn != null) conn.close(); }
			catch (SQLException e) { e.printStackTrace(System.err); }
		}
		
		String result = 
			"<?xml version='1.0' encoding='utf-8'?>\n" +
			"<rdf:RDF\n" +
			"	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
			"	xmlns='http://purl.org/rss/1.0/'\n" +
			">\n" +
			"	<channel rdf:about='http://adapt2.sis.pitt.edu/pservice/flooder_rss?num=" + number_i + "'>\n" +
			"		<title rdf:parseType='Literal'>PServices Flooder RSS</title>\n" +
			"		<link>http://adapt2.sis.pitt.edu/pservice/flooder_rss?num=" + number_i + "</link>\n" +
			"		<items>\n" +
			"			<rdf:Seq>\n" +
			short_list +
			"			</rdf:Seq>\n" +
			"		</items>\n" +
			"	</channel>\n" + 
			long_list +
			"</rdf:RDF>";

		PrintWriter out = response.getWriter();
		out.println(result);
		out.close();
	}  	
}