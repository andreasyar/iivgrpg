package mmo.webserver;

import static org.jboss.netty.channel.Channels.*;

import mmo.gameserver.GameServer;

import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class WebServerPipelineFactory implements ChannelPipelineFactory {
	private WebServer webserver;
	private GameServer gameserver;

	WebServerPipelineFactory(WebServer webserver, GameServer gameserver) throws Exception {
		if (webserver == null)
			throw new Exception("WebServer is null");
		this.webserver = webserver;
		this.gameserver = gameserver;		
	}

	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("channelGroupHandler", new SimpleChannelUpstreamHandler() {
			public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
				webserver.getChannels().add(e.getChannel());
			}
		});

		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", new WebServerHandler(gameserver));

		return pipeline;
	}
}