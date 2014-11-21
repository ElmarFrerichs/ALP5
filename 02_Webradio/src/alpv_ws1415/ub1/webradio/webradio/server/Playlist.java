package alpv_ws1415.ub1.webradio.webradio.server;

import java.util.*;

/**
 * Verwaltet die Playlist mit Liedern für den Server.
 * 
 * @author Elmar Frerichs
 */
public class Playlist
{
	private Queue<Song> playlist;
	
	public Playlist()
	{
		playlist = new LinkedList<Song>();
	}
	
	/**
	 * Gibt das nächste PlaylistItem zurück.
	 */
	public Song next()
	{
		// Hole das nächste PlaylistItem aus der Queue und hänge es hinten wieder an
		Song song = playlist.remove();
		playlist.add(song);
		
		return song;
	}
	
	/**
	 * Fügt ein neues PlaylistItem hinzu
	 */
	public void add(Song song)
	{
		// Füge item zur Queue hinzu
		playlist.add(song);
	}
	
	/**
	 * Gibt die Größe der Liste zurück
	 */
	public int size()
	{
		return playlist.size();
	}
}
