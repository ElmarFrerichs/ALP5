package alpv_ws1415.ub1.webradio.webradio.server;

import java.io.*;

import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.ui.ServerUI;
import alpv_ws1415.ub1.webradio.communication.Server;

/**
 * Horcht auf stdin auf Befehle und leitet diese weiter
 * 
 * @author Elmar Frerichs
 */
public class ConsoleListener implements ServerUI, Runnable
{
	private Server server;
	private Reader reader;
	private BufferedReader in;
	
	private boolean stop = false;
	
	public ConsoleListener(Server server)
	{
		this.server = server;
		this.reader = new InputStreamReader(System.in);
		this.in = new BufferedReader(reader);
	}
	
	public void run()
	{
		try
		{
			while(!stop)
			{
				String input = in.readLine();
				
				if(input == null)
				{
					continue;
				}
				else if(input.startsWith("h") || input.startsWith("help"))
				{
					printHelp();
				}
				else if(input.startsWith("e") || input.startsWith("exit"))
				{
					// Schlieﬂe Server und sich selbst
					Log.notice("shuting down");
					server.close();
					return;
				}
				else if(input.startsWith("a ") || input.startsWith("add "))
				{
					String path = "";
					if(input.startsWith("a "))
					{
						path = input.substring(2);
					}
					else
					{
						path = input.substring(4);
					}
					
					try
					{
						server.playSong(path);
					}
					catch(Exception e)
					{
						print("Exception while adding Song! "+e.toString());
						e.printStackTrace();
					}
					Log.notice("Song added");
				}
				else
				{
					printHelp();
				}
			}
		}
		catch(Exception e)
		{
			print("Exception! "+e.toString());
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		this.stop = true;
		
		try
		{
			// Harte Methode: Geschlossener Stream bricht den readLine() ab
			this.reader.close();
		}
		catch(IOException e)
		{ }
		
		Log.log("ConsoleListener closed");
	}
	
	public static void print(String text)
	{
		System.out.println(text);
	}
	
	public void printHelp()
	{
		print("Options:");
		print("exit         Exits the program");
		print("add <path>   Adds <path> to playlist");
	}
}