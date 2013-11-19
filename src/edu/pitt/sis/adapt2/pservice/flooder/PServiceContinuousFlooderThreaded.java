package edu.pitt.sis.adapt2.pservice.flooder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.concurrent.locks.LockSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.pitt.sis.adapt2.pservice.rest.DataRobot;
import edu.pitt.sis.adapt2.pservice.rest.RestServlet;

public class PServiceContinuousFlooderThreaded extends RestServlet
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
//		int NO_RUNS = Integer.parseInt(getServletContext().getInitParameter("no_runs"));
//		int pause_after_run = Integer.parseInt(getServletContext().getInitParameter("pause_after_run"));
//		int pause_after_delay = Integer.parseInt(getServletContext().getInitParameter("pause_after_delay"));
		
		response.setContentType(CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>UMFlooder</title></head><body>");

		int epoch = Integer.parseInt(getServletContext().getInitParameter("epoch"));
		int delay_max = Integer.parseInt(getServletContext().getInitParameter("delay_max"));
		int delay_min = Integer.parseInt(getServletContext().getInitParameter("delay_min"));
		int req_size_min = Integer.parseInt(getServletContext().getInitParameter("req_size_min"));
		int req_size_max = Integer.parseInt(getServletContext().getInitParameter("req_size_max"));

//		TimingThread timer_thread = new TimingThread();
//		timer_thread.start();	
//		ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(100);
		
		Vector<Thread> threads_of_the_run = new Vector<Thread>();
		
		System.out.println("Started...");
 
//		Connection conn = null;
//		try
//		{// sql loop 
//			conn = getSqlManager().getConnection();
			
			int prior_delay = 0;
			int count = 0;
			long time_start = System.currentTimeMillis();
			long time_now = System.currentTimeMillis();
			
			System.out.println("time_start = " + time_start);
			System.out.println("time_now = " + time_now);
			System.out.println("epoch = " + epoch);
			
			while((time_now-time_start)<epoch)
			{// while epoch lasting
				// randomize
				int rnd_delay = (int)Math.round( (delay_max-delay_min) * Math.random() + delay_min );
				int rnd_request_size = (int)Math.round( (req_size_max-req_size_min) * Math.random() + req_size_min);
				
				String token = note 	+ /*"_e" + epoch +*/ "_pd" + prior_delay + "_sz" + rnd_request_size + "_q" + (++count);
				
				String mod_pservice_parameters = pservice_parameters + 
					"&" + URLEncoder.encode(DataRobot.REST_SERVICE_INVOKE_TOKEN , "UTF-8") + "=" + 
					URLEncoder.encode(token, "UTF-8") +
					"&" + URLEncoder.encode("uri", "UTF-8") + "=" + 
					URLEncoder.encode("http://" + request.getServerName() + ((request.getLocalPort() != 80)?":"+ 
					request.getLocalPort():"") + request.getContextPath() +
//					"/flooder_rss_" + rnd_request_size + ".rdf", "UTF-8" );
					"/t_flooder_rss_" + rnd_request_size + ".rdf", "UTF-8" );

				Thread thread = new FloodingThread(this.getSQLM(), pservice_url, mod_pservice_parameters, 
						note, token, epoch, prior_delay, rnd_request_size, count);
				threads_of_the_run.add(thread);
				
				//Schedule task
//				pool.schedule(thread, (long)prior_delay * 1000000, TimeUnit.NANOSECONDS) ;
				LockSupport.parkNanos(rnd_delay*1000000);
				
				// run task
				thread.start();				

				// delay between threads

				// Wait for the thread to finish but don't wait longer than a
				// specified time
//				try
//				{
//					timer_thread.join(rnd_delay);
//					if (timer_thread.isAlive())
//					{
//						;// Timeout occurred; thread has not finished
//					}
//					else
//					{
//						;// Finished
//					}
//				}
//				catch (InterruptedException e)
//				{
//					;// Thread was interrupted
//				}					

				prior_delay = rnd_delay;
				time_now = System.currentTimeMillis();
								
			}// end of -- while epoch lasting
			
			System.out.println("finishing... waiting for threads ("  + System.currentTimeMillis() + ")");
			
			// wait for all threads to finish
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
			System.out.println("Finished!  ("  + System.currentTimeMillis() + ")");

//		}// end of -- sql loop
//		catch(SQLException sqle) { sqle.printStackTrace(System.out); }
//		finally
//		{
//			getSqlManager().freeConnection(conn);
//		}
		
		// close pool
//		pool.shutdown();
//		pool = null;
		// stop timing thread
//		timer_thread.doStop();

		System.out.println("OK (" + count + ")");
		out.println("OK (" + count + ")");
		out.println("</body></html>");
		out.close();
	}

}
