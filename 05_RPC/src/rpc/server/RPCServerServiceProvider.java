package rpc.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

import com.google.protobuf.ByteString;

/**
 * Biete einen RPC-Service auf einen gegebenen Port an; so das statischen Methoden von
 * beliebigen Klassen ueber Netzwerk mit Hilfe des <tt>RPCRemoteServiceProvider</tt> aufgerufen werden koennen.
 */
public class RPCServerServiceProvider
		implements
			Runnable
{
	// Stoppe den Server
	private boolean stop = false;
	
	private RPCServiceProvider serviceProvider;
	private int port;
	
	/**
	 * @param serviceProvider der RPC-Service, der genutz werden soll, um die Methode aufzurufen.
	 * @param port Port, auf dem der Server den RPC Service anbietet
	 */
	public RPCServerServiceProvider(RPCServiceProvider serviceProvider, int port) throws SocketException
	{
		this.serviceProvider = serviceProvider;
		this.port = port;
	}

	@Override
	public void run()
	{
		try
		{
			byte[] b;
			DatagramSocket socket = new DatagramSocket(port);
			
			while(!stop)
			{
				b = new byte[1024];
				DatagramPacket recievePackage = new DatagramPacket(b, b.length);
				socket.receive(recievePackage);
				
				ByteArrayInputStream is = new ByteArrayInputStream(b);
				RPCCall call = RPCCall.parseDelimitedFrom(is);
				
				ByteString result = null;
				boolean isException = false;
				
				try
				{
					serviceProvider.callsave(call.getClassname(), call.getMethodname(), RPCSecrets.deserialize(call.getParametersList()));
				}
				catch(RPCException e)
				{
					result = RPCSecrets.serialize(e);
					isException = true;
				}
				catch(ClassNotFoundException e)
				{
					result = RPCSecrets.serialize(e);
					isException = true;
				}
				
				RPCProtocol.RPCResult.Builder r = RPCResult.newBuilder();
				
				if(isException)
				{
					r.setException(result);
				}
				else
				{
					r.setResult(result);
				}
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
				r.build().writeDelimitedTo(stream);
				
				b = stream.toByteArray();
				DatagramPacket sendPackage = new DatagramPacket(b, b.length, recievePackage.getAddress(), recievePackage.getPort());
				socket.send(sendPackage);
			}
			
			socket.close();
		}
		catch(IOException e)
		{
			return;
		}
	}

	/**
	 * Terminiert den Server.
	 */
	public void terminate()
	{
		stop = true;
	}

}
