package mmo.websocketserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class WebServer {
	private ServerBootstrap bootstrap;
	private ChannelGroup channels = new DefaultChannelGroup();
	
	public ChannelGroup getChannels()
	{
		return channels;
	}
	
	public void start(int port) throws Exception {
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
