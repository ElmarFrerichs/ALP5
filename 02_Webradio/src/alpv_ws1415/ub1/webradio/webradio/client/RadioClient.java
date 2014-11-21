package alpv_ws1415.ub1.webradio.webradio.client;

import alpv_ws1415.ub1.webradio.communication.Client;


public abstract class RadioClient implements Client, Runnable
{
	// Flags
	protected boolean stop = false;
	
	/**
	 * Close socket
	 */
	public void close()
	{
		this.closeSocket();
		this.stop = true;
	}
	
	protected abstract void closeSocket();
	
}
