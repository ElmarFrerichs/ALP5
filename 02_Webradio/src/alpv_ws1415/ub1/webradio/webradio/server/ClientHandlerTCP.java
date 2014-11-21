package alpv_ws1415.ub1.webradio.webradio.server;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.sound.sampled.AudioFormat;

import com.google.protobuf.ByteString;

import alpv_ws1415.ub1.webradio.ui.Log;
import alpv_ws1415.ub1.webradio.protobuf.RadioPaketProtos.*;

/**
 * Übernimmt einen Socket und eine PlaylistReader-Verbindung kommuniziert mit dem Client
 * 
 * @author Elmar Frerichs
 */
public class ClientHandlerTCP extends ClientHandler
{
	private Socket socket;
	private ClientReciever rc;
	
	private int messageId = 0;
	private int messageId() { return messageId++; }
	
	/**
	 * Konstruktor
	 * @throws IOException
	 */
	public ClientHandlerTCP(Socket socket) throws IOException
	{
		stream = socket.getOutputStream();
		this.socket = socket;
		rc = new ClientReciever(this);
		new Thread(rc).start();
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
		rc.close();
		
		try
		{
			if(socket != null)
			{
				socket.close();
			}
		}
		catch(IOException e)
		{
			// Ist jetzt auch egal...
		}
		
		super.close();
	}
	
	private class ClientSender implements Runnable
	{
		private byte[] data;
		private ClientHandler context;
		
		public ClientSender(byte[] data, ClientHandler context)
		{
			this.data = data;
			this.context = context;
		}
		
		public void run()
		{
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
				
				paket.build().writeDelimitedTo(stream);
			}
			catch(IOException e)
			{
				Log.notice("Verbindung verloren");
				context.close();
			}
		}
	}
	
	private class ClientReciever implements Runnable
	{
		private boolean stop;
		private ClientHandler context;
		
		public ClientReciever(ClientHandler context)
		{
			this.context = context;
		}
		
		public void close()
		{
			this.stop = true;
		}
		
		public void run()
		{
			Log.notice("Neuer ClientReciever!");
			try
			{
				while(!stop)
				{
					RadioAntwortPaket paket = RadioAntwortPaket.parseDelimitedFrom(socket.getInputStream());
					
					TextMessage message = new TextMessage(paket.getMessage().getUser(), paket.getMessage().getMessage());
					
					player.sendTextMessage(message);
				}
			}
			catch(IOException e)
			{
				Log.notice("Verbindung verloren");
				context.close();
			}
		}
	}
}
