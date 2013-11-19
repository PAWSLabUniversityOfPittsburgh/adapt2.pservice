package edu.pitt.sis.adapt2.pservice.flooder;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.pitt.sis.paws.core.utils.SQLManager;

/** Thread that performs the flooding
 * @author Michael V. yudelson
 * @version %I%, %G%
 * @since 1.5
 */
 
public class FloodingThread extends Thread
{
	/** Database connection
	 * @since 1.5
	 */
	protected SQLManager sqlm;

	/** URL of the personalization service
	 * @since 1.5
	 */
	protected String pservice_url;
	
	/** Parameters for the personalization service
	 * @since 1.5
	 */
	protected String pservice_parameters;
	
	/** Experiment note
	 * @since 1.5
	 */
	protected String note;

	/** Unique request token
	 * @since 1.5
	 */
	protected String token;

	/** Flooding epoch
	 * @since 1.5
	 */
	protected int epoch;

	/** Working delay of the flooder
	 * @since 1.5
	 */
	protected int delay;

	/** Number of a run
	 * @since 1.5
	 */
	protected int run;

	/** 
	 * Number of a request in a run
	 * @since 1.5
	 */
	protected int request_no;
	
	/** Flooding thread constructor
	 * @param _conn database connection
	 * @param _conf configuration of the target UM database
	 * @param _delay working delay of the flooder
	 * @param _run number of a run
	 * @param _request number of a request in a run
	 * @since 1.5
	 */
	public FloodingThread(SQLManager _sqlm, String _pservice_url, String _pservice_parameters,
			String _note, String _token, int _epoch, int _delay, int _run, int _request_no)
	{
		sqlm = _sqlm;
		pservice_url = _pservice_url;
		pservice_parameters = _pservice_parameters;
		token = _token;
		note = _note;
		epoch = _epoch;
		delay = _delay;
		run = _run;
		request_no = _request_no;
	}
	
	public void run()
	{// run
		
		long start_ms = 0;
		long finish_ms = 0;

		// clock time before send request
		start_ms = System.nanoTime(); //currentTimeMillis();
//		Calendar s = new GregorianCalendar();
//		start_ms = s.getTimeInMillis();
//		s = null;

		Model model = ModelFactory.createDefaultModel();

		// send request
		String result = "OK";
		long response_size = -1;
		try
		{
	        URL url = new URL(pservice_url);
	        URLConnection pserv_conn = url.openConnection();
	        pserv_conn.setDoOutput(true);
	        OutputStreamWriter wr = new OutputStreamWriter(pserv_conn.getOutputStream());
	        wr.write(pservice_parameters);
	        wr.flush();
	        InputStream in  = pserv_conn.getInputStream();
	        model.read(in, "");
	        response_size = model.size();
	        model.close();
	        model = null;
	        in.close();
	        wr.close();
	        in = null;
	        wr = null;
	        
		}
		catch(MalformedURLException mue) { result = "MalformedURLException"; }
		catch(IOException ioe) { result = "IOException"; }
		catch(Exception e) { result = e.getMessage(); }
		
		// clock time after send request
		finish_ms = System.nanoTime(); //currentTimeMillis();
//		Calendar f = new GregorianCalendar();
//		finish_ms = f.getTimeInMillis();
//		f = null;
		
		String qry = "INSERT INTO ent_flooder_log (Note, Epoch, Delay, Run, RequestNo, " +
				"Token, StartTS, EndTS, DurationMS, Result, ResponseSize) VALUES('" + note + 
				"'," + epoch + "," + delay + "," + run + "," + request_no + ",'" + token + 
				"'," + start_ms + "," + finish_ms + "," + (finish_ms-start_ms) + ",'" + result + 
				"'," + response_size + ");";
		
		Connection conn = null;
		Statement stmt = null;
		try
		{
			conn = sqlm.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();
			stmt = null;
			conn.close();
			conn = null;
		}
		catch (SQLException e)
		{
			System.out.println("!!! {PService.FlooderThread} CANNOT LOG IN DB");
			e.printStackTrace(System.out); //*** TEST ONLY
		}
		finally
		{
			if (stmt != null) 
			{
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null)
			{
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
	
	}// end of -- run
	
	
	long getPingTime(String _url, int _ttl, int _count)
	{
		String pingResult = "";

		try
		{
			URL url = new URL(_url);
			String ip = InetAddress.getByName(url.getHost()).getHostAddress();
			
			String pingCmd = "ping -m " + _ttl + " -c " + _count + " " + ip;
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(pingCmd);
	
			BufferedReader in = new BufferedReader(new
			InputStreamReader(p.getInputStream()));
			String inputLine;
			int ii = 0;
			while ((inputLine = in.readLine()) != null && ii <2)
			{
				System.out.println(inputLine);
				pingResult += inputLine;
				ii ++;
			}
			in.close();
		}//try
		catch(MalformedURLException mue) { mue.printStackTrace(System.out); }
		catch(IOException ioe) { ioe.printStackTrace(System.out); }
		
		int time_st = pingResult.indexOf("time=") + 5;
		int time_fi = pingResult.indexOf(" ms", time_st);
		float time = Float.parseFloat(pingResult.substring(time_st, time_fi));

		return (long)((float)time*1000000);
	}
}
