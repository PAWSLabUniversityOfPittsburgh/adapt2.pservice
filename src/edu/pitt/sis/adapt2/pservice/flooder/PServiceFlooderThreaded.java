package edu.pitt.sis.adapt2.pservice.flooder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.adapt2.pservice.rest.RestServlet;

public class PServiceFlooderThreaded extends RestServlet
{
	static final long serialVersionUID = -2L;

	private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
//	private static final int NO_RUNS = 10;

	public void doGet(HttpServletRequest request,
			 HttpServletResponse response) throws ServletException, IOException
	{
		String pservice_url = getServletContext().getInitParameter("flooded_pservice");
		String pservice_parameters = getServletContext().getInitParameter("flood_parameters");
		String note = getServletContext().getInitParameter("flooding_note");
		int NO_RUNS = Integer.parseInt(getServletContext().getInitParameter("no_runs"));
		int pause_after_run = Integer.parseInt(getServletContext().getInitParameter("pause_after_run"));
		int pause_after_delay = Integer.parseInt(getServletContext().getInitParameter("pause_after_delay"));
		
		response.setContentType(CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>UMFlooder</title></head><body>");

		int epoch = Integer.parseInt(getServletContext().getInitParameter("epoch"));
		int delay_max = Integer.parseInt(getServletContext().getInitParameter("delay_max"));
		int delay_min = Integer.parseInt(getServletContext().getInitParameter("delay_min"));
		int req_size = Integer.parseInt(getServletContext().getInitParameter("req_size"));

		TimingThread timer_thread = new TimingThread();
		timer_thread.start();			
		
		Connection conn = null;
		try
		{// sql loop 
			conn = this.getSQLM().getConnection();
		
			for(int i=delay_max; i>=delay_min; i/=2)
			{// for all delays
System.out.print("/// {PService.Flooder} Delay = " + i);
				for(int j=0; j<NO_RUNS; j++)
				{// for all runs
					// create a storage for all threads
					Vector<Thread> threads_of_the_run = new Vector<Thread>();
					for(int k=0; k<(epoch/i); k++)
					{// for all requests
						// run thread
						String token = "n_" + note + "_e" + epoch + "d" + i + "r" + j + "#" + k;
						String mod_pservice_parameters = pservice_parameters + 
								"&" + URLEncoder.encode(DataRobot.REST_SERVICE_INVOKE_TOKEN , "UTF-8") + "=" + 
								URLEncoder.encode(token, "UTF-8") +
								"&" + URLEncoder.encode("uri", "UTF-8") + "=" + 
								URLEncoder.encode("http://" + request.getServerName() + ((request.getLocalPort() != 80)?":"+ 
			    				request.getLocalPort():"") + request.getContextPath() +
//			    				"/flooder_rss_" + req_size + ".rdf", "UTF-8" );
								"/t_flooder_rss_" + req_size + ".rdf", "UTF-8" );
				
						Thread thread = new FloodingThread(null, pservice_url, mod_pservice_parameters, 
								note, token, epoch, i, j, k);
						
						threads_of_the_run.add(thread);
						thread.start();				
	
						// delay between threads
	
						// Wait for the thread to finish but don't wait longer than a
						// specified time
						long delayMillis = i;
						try
						{
							timer_thread.join(delayMillis);
							if (timer_thread.isAlive())
							{
								;// Timeout occurred; thread has not finished
							}
							else
							{
								;// Finished
							}
						}
						catch (InterruptedException e)
						{
							;// Thread was interrupted
						}					
						
	
					}// end of -- for all requests
					// wait for all threads of the run to die
					for(int k=0; k<threads_of_the_run.size(); k++)
					{
						try
						{
							threads_of_the_run.get(k).join();
						}
						catch (InterruptedException e)
						{
							;// Thread was interrupted
						}					
					}
					// Wait between different runs
					try
					{
						timer_thread.join(pause_after_run);
						if (timer_thread.isAlive()) { ; }
						else {;}
					}
					catch (InterruptedException e) {;}		
					// end of -- Wait between different runs
					
				}// and of -- for all runs
				
				// Wait between different delays
				try
				{
					timer_thread.join(pause_after_delay);
					if (timer_thread.isAlive()) { ; }
					else {;}
				}
				catch (InterruptedException e) {;}		
				// end of -- Wait between different delays

				System.out.println(" - done!!");
			}// end of -- for all delays
		}// end of -- sql loop
		catch(SQLException sqle) { sqle.printStackTrace(System.out); }
		finally
		{
			try { if (conn != null) conn.close(); }
			catch (SQLException e) { e.printStackTrace(System.err); }
		}
		
		// stop timing thread
		timer_thread.doStop();

		System.out.println("OK");
		out.println("OK");
		out.println("</body></html>");
		out.close();
	}

}
