package alpv_ws1415.ub1.webradio.webradio.server;

import java.net.*;
import java.io.*;

import alpv_ws1415.ub1.webradio.ui.Log;

/**
 * Nimmt Verbindungen von Clients an und erzeugt dafür einen ClientHandler
 * 
 * @author Elmar Frerichs
 */
public class ConnectionServerMC extends ConnectionServer
{
	private MulticastSocket socket;
	
	public ConnectionServerMC(int port, Player player) throws IOException, SocketException
	{
		super(player);
		
		Log.log("Connection setup on port "+port+"...");
		
		socket = new MulticastSocket(port);
		socket.setSoTimeout(2000);
		
		Log.log("Connection established...");
	}
	
	public void run()
	{
		if(socket == null)
			return;
		
		// Endlosschleife
		while(true)
		{
			// Schauen, ob wir anhalten sollen
			if(stop)
			{
				Log.log("ConnectionServer stopping...");
				break;
			}
			
			if(socket == null || socket.isClosed())
			{
				break;
			}
			
			try
			{
				// Erzeuge neuen ClientHandler
				ClientHandler ch = new ClientHandlerMC(socket);
				player.addClientHandler(ch);
			}
			catch(SocketTimeoutException e)
			{
				// Es ist wieder Zeit...
				Log.log("ServerSocket timed out");
				continue;
			}
			catch(SocketException e)
			{
				Log.log("SocketException while waiting for connections!");
			}
			catch(IOException e)
			{
				Log.error("IOException while waiting for connections!");
				e.printStackTrace();
				break;
			}
		}
		
		try
		{
			// Aufräumen
			socket.close();
		}
		catch(Exception e)
		{
			Log.error("Exception on server shutdown");
		}
		
		Log.log("ConnectionsServer stopped");
	}
	
	public void close()
	{
		Log.log("ConnectionServer closing...");
		
		super.close();
		
		this.stop = true;
		try
		{
			this.socket.close();
		}
		catch(Exception e)
		{ }
		
		Log.log("ConnectionServer closed");
	}
}
