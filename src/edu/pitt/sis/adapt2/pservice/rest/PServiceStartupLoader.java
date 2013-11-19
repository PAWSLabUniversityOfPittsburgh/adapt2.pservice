package edu.pitt.sis.adapt2.pservice.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;

/**
 * Servlet implementation class for Servlet: PServiceStartupLoader
 *
 */
public class PServiceStartupLoader extends RestServlet
{
	static final long serialVersionUID = -2L;
	 /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PServiceStartupLoader()
	{
		super();
	}   	
	
	public void init() throws ServletException
	{
		super.init();
		
		
		Connection conn = null;
		try
		{
			conn = this.getSQLM().getConnection();
			String sql = "SELECT Name, ClassName FROM ent_pservice;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("... [PService] initialization ...");
			while(rs.next())
			{
				String pserviceName = rs.getString("Name");
				String pserviceClassName = rs.getString("ClassName");
				
				if(pserviceClassName != null && pserviceClassName.length() > 0)
					Class.forName(pserviceClassName).newInstance();
				
				System.out.println("... [PService] '" + pserviceName + "' service loaded");
			}
			System.out.println("... [PService] initialized!");
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace(System.out);
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace(System.out);
		}
		catch(IllegalAccessException iae)
		{
			iae.printStackTrace(System.out);
		}
		catch(InstantiationException ie)
		{
			ie.printStackTrace(System.out);
		}
	}

}