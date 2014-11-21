package alpv_ws1415.ub1.webradio.webradio.server;

import java.util.*;

/**
 * Verwaltet die Playlist mit Liedern f�r den Server.
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
	 * Gibt das n�chste PlaylistItem zur�ck.
	 */
	public Song next()
	{
		// Hole das n�chste PlaylistItem aus der Queue und h�nge es hinten wieder an
		Song song = playlist.remove();
		playlist.add(song);
		
		return song;
	}
	
	/**
	 * F�gt ein neues PlaylistItem hinzu
	 */
	public void add(Song song)
	{
		// F�ge item zur Queue hinzu
		playlist.add(song);
	}
	
	/**
	 * Gibt die Gr��e der Liste zur�ck
	 */
	public int size()
	{
		return playlist.size();
	}
}
