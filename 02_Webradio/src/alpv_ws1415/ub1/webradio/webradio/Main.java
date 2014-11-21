package alpv_ws1415.ub1.webradio.webradio;

import java.net.*;

import alpv_ws1415.ub1.webradio.communication.*;
import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.webradio.server.*;
import alpv_ws1415.ub1.webradio.webradio.client.*;

public class Main
{
	private static final String	USAGE	= String.format("usage: java -jar UB%%X_%%NAMEN [-options] server tcp|udp|mc PORT%n" +
														"         (to start a server)%n" +
														"or:    java -jar UB%%X_%%NAMEN [-options] client tcp|udp|mc SERVERIPADDRESS SERVERPORT USERNAME%n" +
														"         (to start a client)");
	
	/**
	 * Starts a server/client according to the given arguments, using a GUI or
	 * just the command-line according to the given arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	
		try {
			boolean useGUI = false;
			int i = -1;

			// Parse options. Add additional options here if you have to. Do not
			// forget to mention their usage in the help-string!
			while(args[++i].startsWith("-")) {
				if(args[i].toLowerCase().equals("-help")) {
					System.out.println(USAGE + String.format("%n%nwhere options include:"));
					System.out.println("  -help      Show this text.");
					System.out.println("  -gui       Show a graphical user interface.");
					System.exit(0);
				}
				else if(args[i].toLowerCase().equals("-gui")) {
					useGUI = true;
				}
			}
			
			String mode = args[i++];
			String connectionType = args[i++];

			if(mode.toLowerCase().equals("server"))
			{
				int port = Integer.parseInt(args[i++]);
				
				// Start server
				if(connectionType.toLowerCase().equals("tcp"))
				{
					try
					{
						Player player = new Player();
						Server server = new ConnectionServerTCP(port, player);
						
						Thread cs = new Thread(server);
						cs.start();
						Log.log("Main: Starting Thread ConnectionServer ("+cs.getName()+")");
						
						cs.join();
						
						Log.notice("Main: shuting down");
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if(mode.toLowerCase().equals("client"))
			{
				String host = args[i++];
				int port = Integer.parseInt(args[i++]);
				String username = args[i++];
				
				// Start client
				if(connectionType.toLowerCase().equals("tcp"))
				{
					try
					{
						ClientGUI gui = new ClientGUI();
						RadioClientTCP client = new RadioClientTCP(gui);
						gui.setContext(client);
						
						Thread rc = new Thread(client);
						rc.start();
						Log.log("Main: Starting Thread RadioClient ("+rc.getName()+")");
						
						client.connect(new InetSocketAddress(host, port));
						
						rc.join();
						
						Log.notice("Main: shuting down");
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				else
					throw new IllegalArgumentException("Illegal connection method");
			}
			else
				throw new IllegalArgumentException("Illegal program mode");
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.err.println(USAGE);
		}
		catch(NumberFormatException e) {
			System.err.println(USAGE);
		}
		catch(IllegalArgumentException e) {
			System.err.println(USAGE);
		}
	}
}
