package rpc.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import rpc.RPCException;
import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol.RPCCall;
import rpc.protobuf.RPCProtocol.RPCResult;

public class RPCRemoteServiceProvider
		extends
			RPCServiceProvider
{
	private InetAddress inetAddress;
	private int port;
	
	public RPCRemoteServiceProvider(final InetAddress inetAddress, int port)
			throws SocketException
	{
		this.inetAddress = inetAddress;
		this.port = port;
	}
	
	/**
	 * Diese Methode soll alle benötigten Informationen zum Ausführen des
	 * Methodenaufrufs serialisieren, dann alles in eine RPCCall-Message packen
	 * und diese übertragen. Danach wartet sie auf eine Antwort des Servers,
	 * wertet diese aus und gibt dann entweder das Ergebnis zurück oder wirft
	 * eine Exception.
	 */
	@Override
	public <R> R callexplicit(String classname, String methodname, Serializable[] params) throws RPCException
	{
		try
		{
			byte[] b;
			DatagramSocket socket = new DatagramSocket(0);
			
			RPCCall.Builder call = RPCCall.newBuilder()
				.setClassname(classname)
				.setMethodname(methodname);
			
			for(Serializable p: params)
			{
				call.addParameters(RPCSecrets.serialize(p));
			}
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
			call.build().writeDelimitedTo(stream);
			
			b = stream.toByteArray();
			DatagramPacket sendPackage = new DatagramPacket(b, b.length, inetAddress, port);
			socket.send(sendPackage);
			
			DatagramPacket recievePackage = new DatagramPacket(b, b.length);
			socket.receive(recievePackage);
			socket.close();
			
			b = recievePackage.getData();
			ByteArrayInputStream is = new ByteArrayInputStream(b);
			RPCResult result = RPCResult.parseDelimitedFrom(is);
			
			if(result.hasException())
			{
				throw new RPCException("Server exception", RPCSecrets.deserialize(result.getException()));
			}
			else if(result.hasResult())
			{
				return RPCSecrets.deserialize(result.getResult());
			}
			else
			{
				throw new RPCException("No answer from server");
			}
		}
		catch(IOException e)
		{
			throw new RPCException("Client exception", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new RPCException("Client exception", e);
		}
	}

}
