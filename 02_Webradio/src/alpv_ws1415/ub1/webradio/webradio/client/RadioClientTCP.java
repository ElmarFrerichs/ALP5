package alpv_ws1415.ub1.webradio.webradio.client;

import java.io.*;
import java.net.*;

import javax.sound.sampled.*;

import alpv_ws1415.ub1.webradio.audioplayer.*;
import alpv_ws1415.ub1.webradio.ui.ClientUI;
import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.webradio.server.TextMessage;
import alpv_ws1415.ub1.webradio.protobuf.RadioPaketProtos.*;



public class RadioClientTCP extends RadioClient
{
	// Private data
	private Socket clientSocket = null;
	
	private ClientUI ui;
	
	private int messageId = 0;
	private int messageId() { return messageId++; }
	
	// Flags
	private boolean stop = false;
	
	/**
	 * Constructor
	 */
	public RadioClientTCP(ClientUI ui)
	{
		this.ui = ui;
	}
	
	/**
	 * Constructor
	 * 
	 * @param socketAddress	directly connect to address (like calling connect() afterwards)
	 */
	public RadioClientTCP(ClientUI ui, InetSocketAddress socketAddress) throws IOException
	{
		this(ui);
		this.connect(socketAddress);
	}
	
	/**
	 * Constructor
	 * 
	 * @param address	directly connect to address (like calling connect() afterwards)
	 * @param port		specify which port to use
	 */
	public RadioClientTCP(ClientUI ui, InetAddress address, int port) throws IOException
	{
		this(ui);
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
	public void run ()
	{
		byte[] data = new byte[4096];
		
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
				InputStream str = clientSocket.getInputStream();
				
				
				AudioPlayer player = null;
				
				// While connection is still available, receive data from server
				while(clientSocket != null)
				{
					RadioPaket paket = RadioPaket.parseFrom(str);
					
					if(paket.hasFormat())
					{
						RadioPaket.AudioFormat af = paket.getFormat();
						
						player = new AudioPlayer(new AudioFormat(
							new AudioFormat.Encoding(af.getEncoding()),
							af.getSampleRate(),
							af.getSampleSizeInBits(),
							af.getChannels(),
							af.getFrameSize(),
							af.getFrameRate(),
							af.getBigEndian()
						));
						player.start();
					}
					
					for(RadioPaket.TextMessage message: paket.getMessageList())
					{
						ui.pushChatMessage(message.getUser() + ": " + message.getMessage());
					}
					
					if(paket.hasMusicData())
					{
						paket.getMusicData().copyTo(data, 0);
						
						if(data != null && player != null)
						{
							player.writeBytes(data);
						}
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
	
	public boolean isClosed()
	{
		return (clientSocket == null);
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
	
	
	public void sendChatMessage(TextMessage message) throws IOException
	{
		if(clientSocket == null)
		{
			throw new IOException("No Socket");
		}
		
		RadioAntwortPaket.Builder paket = RadioAntwortPaket.newBuilder()
			.setId(messageId());
		
		paket.setMessage(RadioAntwortPaket.TextMessage.newBuilder()
			.setUser(message.getUsername())
			.setMessage(message.getText()));
		
		paket.build().writeTo(clientSocket.getOutputStream());
	}
	
	public void sendChatMessage(String text)
	{
		// Wegen Compilerfehler
	}
}
