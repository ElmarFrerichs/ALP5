package alpv_ws1415.ub1.webradio.webradio.server;

/**
 * Übernimmt einen Socket und eine PlaylistReader-Verbindung kommuniziert mit dem Client
 * 
 * @author Elmar Frerichs
 * @author Stefan Kant
 * @author Mika Delor
 */
public class TextMessage
{
	private String user;
	private String text;
	
	public TextMessage(String username, String text)
	{
		this.user = username;
		this.text = text;
	}
	
	public String getUsername()
	{
		return this.user;
	}
	public String getText()
	{
		return this.text;
	}
}
