package mmo.websocketserver;

import java.net.InetSocketAddress;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class WebServer {
	private static final int DEFAULT_PORT = 8015;
	
	private int port;
	private ServerBootstrap bootstrap;
	private ChannelGroup channels = new DefaultChannelGroup();
	
	public ChannelGroup getChannels()
	{
		return channels;
	}
	
	public int getPort()
	{
		return port;
	}
		
	
	public WebServer(ResourceBundle resourceBundle)
	{
		if (resourceBundle != null) {
			port = Integer.parseInt(resourceBundle.getString("webserver.port"));
		} else {
			port = DEFAULT_PORT;
		}
	}
	
	public void start() throws Exception {
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setPipelineFactory(new WebServerPipelineFactory(this));

		Channel serverChannel = bootstrap.bind(new InetSocketAddress(port));
		
		channels.add(serverChannel);
	}
	
	public void stop() throws Exception {
		channels.close().awaitUninterruptibly();
	}
	
	protected void finalize()  throws Exception {
		 stop();
	}
}
