package alpv_ws1415.ub1.webradio.webradio.server;

import java.io.*;
import java.net.MalformedURLException;
import javax.sound.sampled.*;

/**
 * Enthält die MetaInformationen eines Liedes
 * 
 * @author Elmar Frerichs
 */
public class Song
{
	private String path;
	private AudioFileFormat format;
	
	private double duration;
	private String title;
	
	/**
	 * Konstruktor
	 */
	public Song(String path) throws MalformedURLException, IOException, UnsupportedAudioFileException
	{
		this.path = path;
		File file = new File(path);
		format = AudioSystem.getAudioFileFormat(file);
		
		Object prop = format.getProperty("duration");
		if(prop != null)
		{
			duration = (long) prop / 1000;
		}
		else
		{
			duration = (format.getFrameLength()+0.0) / format.getFormat().getFrameRate();			
		}
		
		prop = format.getProperty("title");
		if(prop != null)
		{
			title = (String) prop;
		}
		else
		{
			title = file.getName();
			if (title.indexOf(".") > 0) {
				title = title.substring(0, title.lastIndexOf("."));
			}
		}
	}
	
	public String getPath()
	{
		return path;
	}
	
	public AudioFileFormat getAudioFileFormat()
	{
		return format;
	}
	
	public AudioFormat getAudioFormat()
	{
		return format.getFormat();
	}
	
	public double getDuration()
	{
		return duration;
	}
	
	public String getTitle()
	{
		return title;
	}
}
