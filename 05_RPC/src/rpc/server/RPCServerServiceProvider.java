package rpc.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
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
	
	private DatagramSocket socket;
	
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
		while(!stop)
		{
			try
			{
				byte[] b;
				socket = new DatagramSocket(port);
				
				b = new byte[1024];
				DatagramPacket recievePackage = new DatagramPacket(b, b.length);
				socket.receive(recievePackage);
				
				ByteArrayInputStream is = new ByteArrayInputStream(b);
				RPCCall call = RPCCall.parseDelimitedFrom(is);
				
				ByteString result = null;
				boolean isException = false;
				
				try
				{
					Serializable[] params = (Serializable[]) RPCSecrets.deserialize(call.getParametersList());
					
					result = RPCSecrets.serialize(serviceProvider.callexplicit(call.getClassname(), call.getMethodname(), params));
				}
				catch(RPCException e)
				{
					e.printStackTrace();
					result = RPCSecrets.serialize(e);
					isException = true;
				}
				catch(ClassNotFoundException e)
				{
					e.printStackTrace();
					result = RPCSecrets.serialize(e);
					isException = true;
				}
				
				RPCResult.Builder response = RPCResult.newBuilder();
				
				if(isException)
				{
					response.setException(result);
				}
				else
				{
					response.setResult(result);
				}
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
				response.build().writeDelimitedTo(stream);
				
				b = stream.toByteArray();
				DatagramPacket sendPackage = new DatagramPacket(b, b.length, recievePackage.getAddress(), recievePackage.getPort());
				socket.send(sendPackage);
				
				socket.close();
			}
			catch(IOException e)
			{
				return;
			}
		}
	}

	/**
	 * Terminiert den Server.
	 */
	public void terminate()
	{
		stop = true;
		socket.close();
	}

}
