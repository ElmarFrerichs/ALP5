package alpv_ws1415.ub1.webradio.webradio.server;

import java.util.*;
import java.io.*;

import javax.sound.sampled.*;

import alpv_ws1415.ub1.webradio.ui.Log;

/**
 * Spielt die Lieder ab, die er sich von seiner Playlist holt
 * 
 * @author Elmar Frerichs
 */
public class Player
{
	// Private Variablen
	private LinkedList<ClientHandler> clientHandlers;
	private Playlist playlist;
	private Song nowPlaying;
	
	private Object playlistLock = new Object();
	private Object nowPlayingLock = new Object();
	private Object clientHandlersLock = new Object();
	
	// Flags
	private boolean nextSong = false;
	private boolean stopPlayer = false;
	
	private Thread streamerThread;
	
	// Konstanten
	// zu sendender Puffer in Millisekunden
	private final int sendBufferMillis = 2000;
	// Zusätzliche Größe für den Buffer
	private final int bufferOverhang = 300;
	
	/**
	 * Konstruktor
	 */
	public Player()
	{
		Log.log("New Player");
		clientHandlers = new LinkedList<ClientHandler>();
		playlist = new Playlist();
		nowPlaying = null;
		
		Thread streamerThread = new Thread(new Streamer());
		streamerThread.start();
		Log.log("Player started Streamer ("+streamerThread.getName()+")");
	}
	
	/**
	 * Fügt einen weiteren Client hinzu
	 * @returns true, wenn der Client hinzugefügt wurde
	 */
	public boolean addClientHandler(ClientHandler clientHandler)
	{
		synchronized(clientHandlersLock)
		{
			if(stopPlayer)
			{
				Log.notice("addClientHandler() failed: Player stopping");
				return false;
			}
			
			clientHandlers.add(clientHandler);
		}
		
		clientHandler.setPlayer(this);
		
		synchronized(nowPlayingLock)
		{
			clientHandler.changeSong(nowPlaying);	
		}
		return true;
	}
	
	/**
	 * Entfernt einen Client aus der Liste
	 */
	public void removeClientHandler(ClientHandler clientHandler)
	{
		synchronized(clientHandlersLock)
		{
			clientHandlers.remove(clientHandler);
		}
		clientHandler.unsetPlayer();
	}
	
	/**
	 * Füge ein Lied hinzu
	 */
	public void addSong(Song song)
	{
		synchronized(playlistLock)
		{
			playlist.add(song);
		}
		Log.log("Player: Song added");
	}
	
	/**
	 * Nächstes Lied
	 */
	public void nextSong()
	{
		nextSong = true;
	}
	
	/**
	 * Sendet eine Textnachricht an alle Clients
	 */
	public void sendTextMessage(TextMessage message)
	{
		synchronized(clientHandlersLock)
		{
			Log.notice("Sending message to "+clientHandlers.size()+" clients: "+message.getText());
			for(ClientHandler handler: clientHandlers)
			{
				handler.sendTextMessage(message);
			}
		}
	}
	
	/**
	 * Räume auf und beende den Player
	 */
	@SuppressWarnings("unchecked")
	public void close()
	{
		// Flag auf true setzten
		stopPlayer = true;
		
		// Kopie der Handler-Liste anlegen, damit wir synchronisiert bleiben
		LinkedList<ClientHandler> chs;
		synchronized(clientHandlersLock)
		{
			chs = (LinkedList<ClientHandler>) clientHandlers.clone();
		}
		
		// ClientHandler abmelden
		for(Iterator<ClientHandler> it = chs.listIterator(0); it.hasNext(); )
		{
			it.next().close();
		}
		
		Log.log("Waiting for streamer to terminate");
		
		// Warte auf Streamer
		while(streamerThread.getState() != Thread.State.TERMINATED)
		{
			try
			{
				streamerThread.join();
			}
			catch(InterruptedException e)
			{
				Log.notice("Player: Exeption while waiting to close: "+e.toString());
			}
		}
		
		Log.log("Player closed");
	}
	
	/**
	 * Streamt Daten an alle Clients
	 */
	private class Streamer implements Runnable
	{
		// Tue etwas sinnvolles
		public void run()
		{
			int bufferSize = 4096;
			byte[] buffer = new byte[bufferSize];
			FileInputStream file = null;
			int numRead = 0;
			
			while(!stopPlayer)
			{
				// Playlist leer oder Clientliste leer?
				if(playlist.size() == 0 || clientHandlers.size() == 0)
				{
					// Warten und nichts tun
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e)
					{ }
				}
				
				// Haben wir noch nicht angefangen oder ist das Lied zu Ende?
				if(nowPlaying == null || file == null || nextSong == true)
				{
					// aufräumen
					if(file != null)
					{
						try
						{
							file.close();
						}
						catch(IOException e)
						{
							Log.error("IOException while closing file stream!");
						}
					}
					
					// nächster Song
					AudioFormat audioFormat;
					
					synchronized(nowPlayingLock)
					{
						synchronized(playlistLock)
						{
							if(playlist.size() == 0)
							{
								Log.notice("Streamer: No playlist...");
								
								try
								{
									Thread.sleep(1000);
								}
								catch(InterruptedException e)
								{ }
								
								continue;
							}
							
							nowPlaying = playlist.next();
						}
						audioFormat = nowPlaying.getAudioFormat();
					}
					
					Log.log("Streaming file '"+nowPlaying.getPath()+"'");
					
					// Datei öffnen
					try
					{
						file = new FileInputStream(new File(nowPlaying.getPath()));
					}
					catch(FileNotFoundException e)
					{
						Log.error("File not found!");
					}
					
					// Puffer an Bitrate anpassen
					bufferSize = (int) (sendBufferMillis * 2 / 1000 * audioFormat.getSampleRate() * audioFormat.getSampleSizeInBits() / 8) + bufferOverhang;
					
					Log.log("Buffer size: "+bufferSize);
				}
				
				// Zeit messen, um den Stream timen zu können
				long startTime = System.currentTimeMillis();
				// Wann sollten wir fertig sein?
				long targetTime = startTime + sendBufferMillis;
				
				buffer = new byte[bufferSize];
				
				try
				{
					Log.log("read(buffer, 0, "+bufferSize+"), b.len = "+buffer.length);
					numRead = file.read(buffer, 0, bufferSize);
					
					if(numRead < 0)
					{
						// EndOfFile
						break;
					}
				}
				catch (IOException e)
				{
					Log.error("IOException!");
					e.printStackTrace();
				}
				
				Log.notice("Streaming to clients (count="+clientHandlers.size()+")");
				
				// Send buffer to every connected client
				synchronized(clientHandlersLock)
				{
					for (Iterator<ClientHandler> it=clientHandlers.listIterator(0); it.hasNext();)
					{
						ClientHandler clientHandler = it.next();
						
						if(nextSong)
						{
							synchronized(nowPlayingLock)
							{
								clientHandler.changeSong(nowPlaying);
							}
						}
						
						clientHandler.sendData(buffer);
					}
				}
				
				nextSong = false;
				
				long endTime = System.currentTimeMillis();
				Log.log("Schlafen: "+(targetTime - endTime));
				
				endTime = System.currentTimeMillis();
				
				// Eine Runde ausschlafen
				try
				{
					long sleeptime = targetTime - endTime;
					Thread.sleep((sleeptime < 0 ? 0 : sleeptime));
				}
				catch(InterruptedException e)
				{ }
			}
			
			// Ende, schließe Datei
			try
			{
				if(file != null)
				{
					file.close();
				}
			}
			catch(IOException e)
			{
				Log.error("IOException while closing file stream!");
			}
			
			Log.log("Streamer stopped");
		}
	}
}
