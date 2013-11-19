package edu.pitt.sis.adapt2.pservice.flooder;



/** Stoppable thread with infinite run period used for timing
 * @author Michael V. Yudelson
 * @version %I%, %G%
 * @since 1.5
 */
public class TimingThread extends Thread
{
	protected boolean do_stop;
	
	public TimingThread() { do_stop = false; }
	
	public void run()
	{
		while (true)
		{
			// Empty cycling
			if (do_stop)
				return;
			// Empty cycling
		}
	}
	
	public void doStop() { do_stop = true; }
}
