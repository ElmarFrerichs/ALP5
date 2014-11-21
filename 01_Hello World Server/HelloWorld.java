/**
 * Connects to a server and prints the answers
 * @author Elmar Frerichs
 */

import java.net.*;
import java.io.*;

public class HelloWorld
{
	public static int port = 15101;
	private static String host = null;
	private static String mode;
	
	private static final String	USAGE = String.format("usage: java -jar UB1_Delor_Frerichs_Kant_1a.jar -m server|client [-p PORT] [-a HOSTADDRESS]");
	
	public static void main(String[] args)
	{
		// Argumente prüfen
		for(int i=0; args.length>i; i++)
		{
			if(args[i].equals("--help") || args[i].equals("-h"))
			{
				error(String.format(USAGE));
				return;
			}
			else if(args[i].equals("-p") && args.length > i+1)
			{
				try
				{
					// nächstes Argument ist der Port
					port = Integer.parseInt(args[++i]);
				}
				catch(NumberFormatException e)
				{
					// nimm Standard
				}
			}
			else if(args[i].equals("-a") && args.length > i+1)
			{
				host = args[++i];
			}
			else if(args[i].equals("-m") && args.length > i+1)
			{
				mode = args[++i];
			}
		}
		
		if(mode == null)
		{
			error("No mode (server or client) specified.\n"+USAGE);
			return;
		}
		
		if(mode.toLowerCase().equals("server"))
		{
			// Server
			try
			{
				ServerSocket server = new ServerSocket(port);
				
				log("Server started, listening...");
				
				// Endlosschleife
				while(true)
				{
					Socket client = server.accept();
					
					PrintWriter writer = new PrintWriter(client.getOutputStream());
					
					writer.println("Hello World");
					log("answered call from socket "+(client.getInetAddress().toString()));
					
					writer.close();
					client.close();
				}
			}
			catch(IOException e)
			{
				error("IOException!");
				error(e.toString());
			}
			
			log("shutdown complete");
		}
		else if(mode.toLowerCase().equals("client"))
		{
			// Client
			
			// Fange null-Host ab
			if(host == null)
			{
				error("No host specified.\n"+USAGE);
				return;
			}
			
			try
			{
				Socket socket = new Socket(InetAddress.getByName(host), port);
				
				// Versuche, eine Nachricht zu empfangen
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				log(reader.readLine());
				reader.close();
				socket.close();
			}
			catch(UnknownHostException e)
			{
				error("Host not found!");
				log(e.toString());
			}
			catch(IOException e)
			{
				error("IOException!");
				log(e.toString());
			}
			catch(Exception e)
			{
				error("Unknown Exception!");
				log(e.toString());
			}
		}
	}
	
	
	// Hilfsfunktion
	static void log(String text)
	{
		System.out.println(text);
	}
	static void error(String text)
	{
		System.err.println(text);
	}
}
