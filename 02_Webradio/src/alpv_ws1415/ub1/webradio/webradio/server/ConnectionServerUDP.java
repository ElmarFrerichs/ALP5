package alpv_ws1415.ub1.webradio.webradio.server;

import java.net.*;
import java.io.*;

import alpv_ws1415.ub1.webradio.ui.Log;

/**
 * Nimmt Verbindungen von Clients an und erzeugt daf�r einen ClientHandler
 * 
 * @author Elmar Frerichs
 */
public class ConnectionServerUDP extends ConnectionServer
{
	private ServerSocket socket;
	
	public ConnectionServerUDP(int port, Player player) throws IOException, SocketException
	{
		super(player);
		
		Log.log("Connection setup on port "+port+"...");
		
		socket = new ServerSocket(port);
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
			
			// Warte auf neue Verbindungen
			try
			{
				Log.log("ConnectionServer ready and waiting...");
				Socket client = socket.accept();
				Log.log("New client connecting");
				
				// Erzeuge neuen ClientHandler
				DatagramSocket dsocket = new DatagramSocket(0);
				ClientHandler ch = new ClientHandlerUDP(dsocket, client.getRemoteSocketAddress());
				
				// Port mitteilen
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				bw.write(dsocket.getLocalPort()+"\n");
				bw.close();
				
				Log.notice("Port: "+dsocket.getLocalPort());
				
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
			// Aufr�umen
			socket.close();
		}
		catch(IOException e)
		{
			Log.error("IOException on server shutdown");
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