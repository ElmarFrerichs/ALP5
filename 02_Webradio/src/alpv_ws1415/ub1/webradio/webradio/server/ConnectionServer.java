package alpv_ws1415.ub1.webradio.webradio.server;

import java.net.*;
import java.io.*;
import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1415.ub1.webradio.communication.Server;
import alpv_ws1415.ub1.webradio.ui.Log;

/**
 * Nimmt Verbindungen von Clients an und erzeugt dafür einen ClientHandler
 * 
 * @author Elmar Frerichs
 */
public abstract class ConnectionServer implements Server, Runnable
{
	// Reference for new clients
	protected Player player;
	
	protected ConsoleListener serverUI;
	protected Thread uiThread;
	
	// should the Server stop at the next possible Moment?
	protected boolean stop = false;
	
	public ConnectionServer(Player player) throws IOException
	{
		this.player = player;
		
		// Setup console listener
		this.serverUI = new ConsoleListener(this);
		uiThread = new Thread(serverUI);
		uiThread.start();
	}
	
	public void playSong(String path) throws MalformedURLException, UnsupportedAudioFileException, IOException
	{
		Song song = new Song(path);
		
		player.addSong(song);
	}
	
	public void close()
	{
		// Laufenden Prozess anhalten
		this.stop = true;
		
		Log.log("CS (super): closing");
		
		try
		{
			this.serverUI.close();
			this.player.close();
		}
		catch(Exception e)
		{ }
		
		Log.log("CS (super): closed");
	}
}
