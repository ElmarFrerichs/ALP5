package alpv_ws1415.ub1.webradio.webradio;

import java.io.IOException;
import java.net.InetAddress;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1415.ub1.webradio.communication.*;
import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.webradio.server.*;
import alpv_ws1415.ub1.webradio.webradio.client.*;

public class Main
{
	private static final String	USAGE	= String.format("usage: java -jar webradio.jar server tcp|udp|mc PORT%n" +
														"       java -jar webradio.jar --gui server tcp|udp|mc%n" +
														"         (to start a server)%n" +
														"       java -jar webradio.jar client tcp|udp|mc SERVERIPADDRESS SERVERPORT USERNAME%n" +
														"       java -jar webradio.jar --gui client tcp|udp|mc%n" +
														"         (to start a client)");
	
	/**
	 * Starts a server/client according to the given arguments, using a GUI or
	 * just the command-line according to the given arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			boolean useGUI = false;
			int i = -1;

			// Parse options. Add additional options here if you have to. Do not
			// forget to mention their usage in the help-string!
			while(args[++i].startsWith("-"))
			{
				if(args[i].toLowerCase().equals("--help")
					|| args[i].toLowerCase().equals("-h"))
				{
					System.out.println(USAGE);
					System.exit(0);
				}
				else if(args[i].toLowerCase().equals("--gui"))
				{
					useGUI = true;
				}
			}
			
			String mode = args[i++];
			String connectionType = args[i++];
			
			Thread mainThread = null;

			if(mode.toLowerCase().equals("server"))
			{
				int port = Integer.parseInt(args[i++]);
				Player player = new Player();
					
				// Start server
				if(connectionType.toLowerCase().equals("tcp"))
				{
					Server server = new ConnectionServerTCP(port, player);
					
					mainThread = new Thread(server);
					mainThread.start();
					Log.log("Main: Starting Thread ConnectionServer ("+mainThread.getName()+")");
				}
				else if(connectionType.toLowerCase().equals("udp"))
				{
					Server server = new ConnectionServerUDP(port, player);
					
					mainThread = new Thread(server);
					mainThread.start();
					Log.log("Main: Starting Thread ConnectionServer ("+mainThread.getName()+")");
				}
				else
					throw new IllegalArgumentException("Illegal connection method");
				
				try
				{
					player.addSong(new Song("..\\test.wav"));
				}
				catch (UnsupportedAudioFileException e)
				{ }
			}
			else if(mode.toLowerCase().equals("client"))
			{
				// Start client
				if(connectionType.toLowerCase().equals("tcp"))
				{
					if(useGUI)
					{
						ClientGUI gui = new ClientGUI();
						RadioClientTCP client = new RadioClientTCP(gui);
						gui.setContext(client);
						
						mainThread = new Thread(client);
						new Thread(gui).start();
						mainThread.start();
						Log.log("Main: Starting Thread RadioClient ("+mainThread.getName()+")");
					}
					else
					{
						String host = args[i++];
						int port = Integer.parseInt(args[i++]);
						// String username = args[i++];
						
						RadioClient client = new RadioClientTCP(InetAddress.getByName(host), port);
						
						mainThread = new Thread(client);
						mainThread.start();
						Log.log("Main: Starting Thread RadioClient ("+mainThread.getName()+")");
					}
				}
				else if(connectionType.toLowerCase().equals("udp"))
				{
					if(useGUI)
					{
						ClientGUI gui = new ClientGUI();
						RadioClientUDP client = new RadioClientUDP(gui);
						gui.setContext(client);
						
						mainThread = new Thread(client);
						new Thread(gui).start();
						mainThread.start();
						Log.log("Main: Starting Thread RadioClient ("+mainThread.getName()+")");
					}
					else
					{
						String host = args[i++];
						int port = Integer.parseInt(args[i++]);
						// String username = args[i++];
						
						RadioClient client = new RadioClientUDP(InetAddress.getByName(host), port);
						
						mainThread = new Thread(client);
						mainThread.start();
						Log.log("Main: Starting Thread RadioClient ("+mainThread.getName()+")");
					}
				}
				else
					throw new IllegalArgumentException("Illegal connection method");
			}
			else
				throw new IllegalArgumentException("Illegal program mode");
			
			if(mainThread != null)
			{
				// Schlieﬂen, wenn Thread beendet
				mainThread.join();
			}
			Log.notice("Main: shuting down");
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			Log.error("ArrayIndexOutOfBoundsException");
			Log.error(USAGE);
		}
		catch(NumberFormatException e)
		{
			Log.error("NumberFormatException");
			Log.error(USAGE);
		}
		catch(IllegalArgumentException e)
		{
			Log.error("IllegalArgumentException");
			Log.error(USAGE);
		}
		catch(IOException e)
		{
			Log.error("IOException");
			Log.error(USAGE);
		}
		catch(InterruptedException e)
		{
			Log.error("Interrupted, shuting down");
		}
	}
}
