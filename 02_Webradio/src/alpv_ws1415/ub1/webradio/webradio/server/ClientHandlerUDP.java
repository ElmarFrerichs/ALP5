package alpv_ws1415.ub1.webradio.webradio.server;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.sound.sampled.AudioFormat;

import com.google.protobuf.ByteString;

import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.protobuf.RadioPaketProtos.*;

/**
 * ‹bernimmt einen Socket und eine PlaylistReader-Verbindung kommuniziert mit dem Client
 * 
 * @author Elmar Frerichs
 */
public class ClientHandlerUDP extends ClientHandler
{
	private DatagramSocket socket;
	private SocketAddress clientAddress;
	
	// static, damit es nur einen Reciever gibt
	private static ClientReciever rc = null;
	private int clients = 0;
	public void resetReciever() { clients = 0; }
	
	private int messageId = 0;
	private int messageId() { return messageId++; }
	
	/**
	 * Konstruktor
	 * @throws IOException
	 */
	public ClientHandlerUDP(DatagramSocket socket, SocketAddress clientAddress) throws IOException
	{
		this.socket = socket;
		this.clientAddress = clientAddress;
		
		// Es kann nur einen (ClientReciever) geben
		if(rc == null)
		{
			rc = new ClientReciever(this);
			new Thread(rc).start();
		}
		clients++;
	}
	
	/**
	 * Sende Audio-Daten an den Client
	 * @throws IOException
	 */
	public void sendData(byte[] audioData)
	{
		Thread t = new Thread(new ClientSender(audioData, this));
		t.start();
	}
	
	public void close()
	{
		// Schlieﬂen, wenn wir der letzte sind
		if(--clients == 0)
		{
			rc.close();
		}
		
		if(socket != null)
		{
			socket.close();
		}

		super.close();
	}
	
	private class ClientSender implements Runnable
	{
		private byte[] data;
		private ClientHandler context;
		
		public ClientSender(byte[] data, ClientHandler context)
		{
			if(socket == null)
				Log.notice("Socket ist null... (constructor)");
			this.data = data;
			this.context = context;
		}
		
		public void run()
		{
			if(socket == null)
				Log.notice("Socket ist null... (run)");
			try
			{
				RadioPaket.Builder paket = RadioPaket.newBuilder()
					.setId(messageId());
				
				synchronized(changeSongLock)
				{
					if(changeSong != null)
					{
						AudioFormat format = changeSong.getAudioFormat();
						
						paket.setFormat(RadioPaket.AudioFormat.newBuilder()
							.setTitle(changeSong.getTitle())
							.setDuration(changeSong.getDuration())
							.setEncoding(format.getEncoding().toString())
							.setSampleRate(format.getSampleRate())
							.setSampleSizeInBits(format.getSampleSizeInBits())
							.setChannels(format.getChannels())
							.setFrameRate(format.getFrameRate())
							.setFrameSize(format.getFrameSize())
							.setBigEndian(format.isBigEndian()));
						
						changeSong = null;
					}
				}
				
				if(this.data != null)
				{
					paket.setMusicData(ByteString.copyFrom(data));
				}
				
				synchronized(messagesLock)
				{
					if(messages.size() != 0)
					{
						Iterator<TextMessage> it = messages.iterator();
						while(it.hasNext())
						{
							TextMessage tm = it.next();
							
							paket.addMessage(RadioPaket.TextMessage.newBuilder()
									.setUser(tm.getUsername())
									.setMessage(tm.getText()));
						}
						messages.clear();
					}
				}
				
				ByteArrayOutputStream str = new ByteArrayOutputStream(10000);
				
				paket.build().writeDelimitedTo(str);
				
				byte[] data = str.toByteArray();
				Log.notice("Data size: "+data.length);
				DatagramPacket dp = new DatagramPacket(data, data.length, clientAddress);
				
				if(socket == null)
					throw new IOException();
				
				socket.send(dp);
			}
			catch(IOException e)
			{
				Log.notice("Verbindung verloren (ClientSender)");
				e.printStackTrace();
				context.close();
			}
		}
	}
	
	private class ClientReciever implements Runnable
	{
		private boolean stop;
		private ClientHandlerUDP context;
		
		public ClientReciever(ClientHandlerUDP context)
		{
			this.context = context;
		}
		
		public void close()
		{
			this.stop = true;
		}
		
		public void run()
		{
			byte[] data = new byte[1024];
			Log.notice("Neuer ClientReciever!");
			DatagramSocket socket = null;
			
			try
			{
				socket = new DatagramSocket(15102);
				while(!stop)
				{
					DatagramPacket dp = new DatagramPacket(data, data.length);
					socket.receive(dp);
					ByteArrayInputStream str = new ByteArrayInputStream(data);
					
					RadioAntwortPaket paket = RadioAntwortPaket.parseDelimitedFrom(str);
					
					TextMessage message = new TextMessage(paket.getMessage().getUser(), paket.getMessage().getMessage());
					
					player.sendTextMessage(message);
				}
			}
			catch(IOException e)
			{
				Log.notice("Verbindung verloren (ClientReciever)");
				context.resetReciever();
			}
			
			if(socket != null)
			{
				socket.close();
			}
		}
	}
}
