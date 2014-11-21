package alpv_ws1415.ub1.webradio.webradio.client;

import java.io.IOException;

import alpv_ws1415.ub1.webradio.communication.Client;
import alpv_ws1415.ub1.webradio.webradio.server.TextMessage;


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
	
	public abstract void sendChatMessage(TextMessage message) throws IOException;
	
	protected abstract void closeSocket();
	public abstract boolean isClosed();
}
