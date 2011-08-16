package mmo.webserver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import mmo.config.XMLConfig;
import mmo.gameserver.GameServer;

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

	private GameServer gameserver;
	
	public ChannelGroup getChannels()
	{
		return channels;
	}
	
	public int getPort()
	{
		return port;
	}
			
	public WebServer(XMLConfig xmlconfig, GameServer gameserver)
	{
		if (xmlconfig!= null) {
			port = xmlconfig.getInt("webserver/port", DEFAULT_PORT);
		} else {
			port = DEFAULT_PORT;
		}
		
		this.gameserver = gameserver; 
	}
	
	public void start() throws Exception {
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setPipelineFactory(new WebServerPipelineFactory(this,gameserver));

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
