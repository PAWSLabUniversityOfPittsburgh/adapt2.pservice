package edu.pitt.sis.adapt2.pservice.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import edu.pitt.sis.paws.core.utils.SQLManager;

public class PerformanceTraceItem
{
	public String pservice_rdfid;
	public String conf_rdfid;
	public String viz_rdfid;
	public String token;
	public String token_suffix;
	
	public long st;
	public long fi;
	public long co;
	public int sz;
	
	public String user_group;
	public String result = "OK";
	public String saved_state;
	
	public Vector<PerformanceTraceDetailItem> details;
	
	public PerformanceTraceItem()
	{
		details = new Vector<PerformanceTraceDetailItem>();
	}
	
	public void saveToDB(SQLManager _sqlm)
	{
		Connection conn = null;
		Statement stmt1 = null;
		Statement stmt2 = null;
		ResultSet rs = null;
		String qry = "";
		try
		{// try to save data
			conn = _sqlm.getConnection();
			qry = "INSERT INTO log_request (Service_rdfID, Conf_rdfID, Viz_rdfID, Token, TokenSuffix, " +
					"InvokeSt, InvokeFi, InvokeCo, InvokeSz, UserGroup, Result, SavedState) VALUES" +
					"('" + pservice_rdfid + "','" + conf_rdfid + "','" + viz_rdfid + "','" + token + 
					"','" + token_suffix + "'," + st +"," + fi + "," + co + "," + sz + ",'" + user_group + 
					"','" + result + "','" + saved_state + "');";
			
			stmt1 = conn.createStatement();
			stmt1.executeUpdate(qry, Statement.RETURN_GENERATED_KEYS);
			int k = 0;
			rs = stmt1.getGeneratedKeys();
			if(rs.next())
				k = rs.getInt(1);
			
			StringBuffer sb = new StringBuffer();
			sb.append("INSERT INTO log_request_detail " + PerformanceTraceDetailItem.getColumnNames() + " VALUES ");
			for(int i=0; i<details.size(); i++)
			{
				PerformanceTraceDetailItem ptdi = details.get(i);
				ptdi.request_id = k;
				sb.append( ((i>0)?",":"") + ptdi.toString());
			}
			sb.append(";");
			
			stmt2 = conn.createStatement();
			stmt2.executeUpdate(sb.toString());
			
			if(rs!=null)
			{
				rs.close();
				rs = null;
			}
			if(stmt1!=null)
			{
				stmt1.close();
				stmt1 = null;
			}
			if(stmt2!=null)
			{
				stmt2.close();
				stmt2 = null;
			}
			if(conn!=null)
			{
				conn.close();
				conn = null;
			}
		}// end of -- try to save data
		catch(SQLException sqle) { sqle.printStackTrace(System.out); }
	}
}
