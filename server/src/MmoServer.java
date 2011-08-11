import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import mmo.websocketserver.WebSocketServerPipelineFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class MmoServer {
	public static void main(String[] args) {

		try {
			ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));

			// Set up the event pipeline factory.
			bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());

			// Bind and start to accept incoming connections.
			Channel channel = bootstrap.bind(new InetSocketAddress(8015));
			System.out.println("Server bind on port "+8015);

			
			System.in.read();
			
			channel.disconnect();
			while(channel.isConnected()){}
			System.out.println("Chanel disconnscted");
			
			channel.unbind();
			while(channel.isBound()){}
			System.out.println("Chanel unbinded");
			
			channel.close();
			while(channel.isOpen()){}
			System.out.println("Chanel closed");
			
			System.out.println("Server shuts down");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}