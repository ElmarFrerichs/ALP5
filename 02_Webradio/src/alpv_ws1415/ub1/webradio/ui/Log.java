package alpv_ws1415.ub1.webradio.ui;

/**
 * Loggt Hinweise und Fehler
 */
public class Log
{
	/**
	 * Fehlermeldungen
	 */
	public static void error(String text)
	{
		System.err.println(text);
	}
	
	/**
	 * Einfache Meldungen
	 */
	public static void notice(String text)
	{
		System.out.println(text);
	}
	
	/**
	 * Unwichtige Log-Meldungen
	 */
	public static void log(String text)
	{
		//System.out.println(text);
	}
}