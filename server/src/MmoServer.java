import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import mmo.httpserver.WebSocketServerPipelineFactory;

import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.socket.nio.*;

public class MmoServer {
	public static void main(String[] args) {
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());

		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(8015));
	}
}