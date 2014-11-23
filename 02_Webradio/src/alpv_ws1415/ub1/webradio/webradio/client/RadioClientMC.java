package alpv_ws1415.ub1.webradio.webradio.client;

import java.io.*;
import java.net.*;

import javax.sound.sampled.*;

import alpv_ws1415.ub1.webradio.audioplayer.*;
import alpv_ws1415.ub1.webradio.ui.ClientUI;
import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.webradio.server.TextMessage;
import alpv_ws1415.ub1.webradio.protobuf.RadioPaketProtos.*;



public class RadioClientMC extends RadioClient
{
	// Private data
	private MulticastSocket clientSocket = null;
	
	private ClientGUI ui;
	
	private int messageId = 0;
	private int messageId() { return messageId++; }
	
	// Flags
	private boolean stop = false;
	
	/**
	 * Constructor
	 */
	public RadioClientMC(ClientGUI ui)
	{
		this.ui = ui;
	}
	
	/**
	 * Constructor
	 * 
	 * @param socketAddress	directly connect to address (like calling connect() afterwards)
	 */
	public RadioClientMC(ClientGUI ui, InetSocketAddress socketAddress) throws IOException
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
	public RadioClientMC(ClientGUI ui, InetAddress address, int port) throws IOException
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
			clientSocket.joinGroup(serverAddress.getAddress());
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
			clientSocket.joinGroup(serverAddress);
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
		byte[] data = null;
		
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
				//UDP/MC
				byte[] b = new byte[1024];
				DatagramPacket p = new DatagramPacket(b, b.length);
				clientSocket.receive(p);
				ByteArrayInputStream str = new ByteArrayInputStream(b);
				
				AudioPlayer player = null;
				
				// While connection is still available, receive data from server
				while(clientSocket != null)
				{
					RadioPaket paket = RadioPaket.parseDelimitedFrom(str);
					
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
						ui.setSong(af.getTitle()+" ("+af.getDuration()+")");
						ui.log("Next song: "+af.getTitle());
					}
					
					for(RadioPaket.TextMessage message: paket.getMessageList())
					{
						ui.pushChatMessage(message.getUser() + ": " + message.getMessage());
					}
					
					if(paket.hasMusicData())
					{
						data = paket.getMusicData().toByteArray();
						
						if(data == null)
							ui.log("keine Daten!");
						if(player == null)
							ui.log("Kein Player!");
						
						if(data != null && player != null)
						{
							//player.writeBytes(data);
						}
					}
				}
			}
			catch(IOException e)
			{
				ui.log("Lost connection to server");
				this.closeSocket();
			}
		}
		ui.log("Client stopped");
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
		ui.log("Client closing");
		this.closeSocket();
		this.stop = true;
	}
	
	protected void closeSocket()
	{
		ui.log("Closing socket");
		try
		{
			if(clientSocket != null)
			{
				clientSocket.close();
				clientSocket = null;
			}
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
		
		//MC
		ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
		paket.build().writeDelimitedTo(stream);
		byte b[] = stream.toByteArray();
		DatagramPacket p = new DatagramPacket(b, b.length);
		clientSocket.send(p);
	}
	
	public void sendChatMessage(String text) throws IOException
	{
		sendChatMessage(new TextMessage(ui.getUserName(), text));
	}
}
