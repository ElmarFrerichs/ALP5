package rpc.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import rpc.RPCSecrets;
import rpc.RPCServiceProvider;
import rpc.protobuf.RPCProtocol;

import com.google.protobuf.ByteString;

/**
 * Biete einen RPC-Service auf einen gegebenen Port an; so das statischen Methoden von
 * beliebigen Klassen ueber Netzwerk mit Hilfe des <tt>RPCRemoteServiceProvider</tt> aufgerufen werden koennen.
 */
public class RPCServerServiceProvider
		implements
			Runnable
{
	/**
	 * @param serviceProvider der RPC-Service, der genutz werden soll, um die Methode aufzurufen.
	 * @param port Port, auf dem der Server den RPC Service anbietet
	 */
	public RPCServerServiceProvider(RPCServiceProvider serviceProvider, int port) throws SocketException {

	}

	@Override
	public void run() {

	}

	/**
	 * Terminiert den Server.
	 */
	public void terminate() {

	}

}
