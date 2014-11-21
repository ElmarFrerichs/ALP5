package alpv_ws1415.ub1.webradio.webradio.server;

import java.io.*;
import java.util.LinkedList;

/**
 * Übernimmt einen Socket und eine PlaylistReader-Verbindung kommuniziert mit dem Client
 * 
 * @author Elmar Frerichs
 */
public abstract class ClientHandler
{
	protected OutputStream stream;
	
	// Rückreferenz auf Player, null = kein Player
	protected Player player = null;
	
	protected Song changeSong = null;
	protected Object changeSongLock = new Object();
	
	protected LinkedList<TextMessage> messages;
	protected Object messagesLock = new Object();
	
	/**
	 * Sende Audio-Daten an den Client
	 * @throws IOException
	 */
	public abstract void sendData(byte[] audioData);
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
	public void unsetPlayer()
	{
		this.player = null;
	}
	
	/**
	 * Ändert das Lied
	 */
	public void changeSong(Song song)
	{
		synchronized(changeSongLock)
		{
			changeSong = song;
		}
	}
	
	/**
	 * Sendet eine Textnachricht an den Client
	 */
	public void sendTextMessage(TextMessage message)
	{
		synchronized(messagesLock)
		{
			messages.add(message);
		}
	}
	
	public void close()
	{
		try
		{
			stream.close();
		}
		catch(IOException e)
		{ }
		
		// Beim Player abmelden
		if(player != null)
		{
			player.removeClientHandler(this);
		}
	}
}
