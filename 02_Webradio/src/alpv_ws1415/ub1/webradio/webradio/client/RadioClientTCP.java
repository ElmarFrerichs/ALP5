package alpv_ws1415.ub1.webradio.webradio.client;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

import alpv_ws1415.ub1.webradio.audioplayer.*;
import alpv_ws1415.ub1.webradio.communication.Client;
import alpv_ws1415.ub1.webradio.ui.Log;


public class RadioClientTCP extends RadioClient
{
	// Private data
	private Socket clientSocket = null;
	
	private int messageId = 0;
	private int messageId() { return messageId++; }
	
	// Flags
	private boolean stop = false;
	
	/**
	 * Constructor
	 */
	public RadioClientTCP()
	{
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param socketAddress	directly connect to address (like calling connect() afterwards)
	 */
	public RadioClientTCP(InetSocketAddress socketAddress) throws IOException
	{
		this.connect(socketAddress);
	}
	
	/**
	 * Constructor
	 * 
	 * @param address	directly connect to address (like calling connect() afterwards)
	 * @param port		specify which port to use
	 */
	public RadioClientTCP(InetAddress address, int port) throws IOException
	{
		this.connect(address, port);
	}
	
	/**
	 * Connects to server address
	 * 
	 * @param serverAddress Information about server
	 */
	public void connect(InetSocketAddress serverAddress) throws IOException
	{
		try
		{
			clientSocket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void connect(InetAddress serverAddress, int port) throws IOException
	{
		try
		{
			clientSocket = new Socket(serverAddress, port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads packet from input stream.
	 * A packet consists of <len of packet><data>
	 * 
	 * @param dis	Input stream where to grab data from
	 * @return		Return buffer containing the data
	 */
	private byte[] readPacket()
	{
		int bufferSize = 4096;
		byte[] data = new byte[4096];
		
		try
		{
			InputStream str = clientSocket.getInputStream();
			
			str.read(data, 0, bufferSize);
			
			return data;
		}
		catch (Exception e)
		{
			Log.error("Lost connection to server");
			this.closeSocket();
			return null;
		}
		
	}
	
	public void run ()
	{
		byte[] data;
		
		while(!stop)
		{
			// Haben wir eine Verbindung?
			if(clientSocket == null)
			{
				// Wait
				try
				{
					Thread.sleep(2000);
				}
				catch(InterruptedException e)
				{ }
				
				continue;
			}
			
			try
			{
				// hier wird geschummelt, weil die Datei in Reichweite ist
				// TODO hier kommt Protobuf hin
				File testFile = new File("test.wav");
				AudioFileFormat aff = AudioSystem.getAudioFileFormat(testFile);
				AudioPlayer player = new AudioPlayer(aff.getFormat());
				
				player.start();
				
				// While connection is still available, receive data from server
				while(clientSocket != null)
				{
					data = readPacket();
					if(data != null)
					{
						player.writeBytes(data);
					}
				}
			}
			catch(Exception e)
			{
				Log.notice("Lost connection to server");
				this.closeSocket();
			}
		}
		Log.log("Client stopped");
	}
	
	
	/**
	 * Close socket
	 */
	public void close()
	{
		Log.log("Client closing");
		this.closeSocket();
		this.stop = true;
	}
	
	protected void closeSocket()
	{
		Log.log("Closing socket");
		try
		{
			clientSocket.close();
			clientSocket = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void sendChatMessage(String message) throws IOException
	{
		
	}
	
}
