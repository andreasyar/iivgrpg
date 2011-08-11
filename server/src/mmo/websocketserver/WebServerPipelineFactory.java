package mmo.websocketserver;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class WebServerPipelineFactory implements ChannelPipelineFactory {
	private WebServer server;

	WebServerPipelineFactory(WebServer s) throws Exception {
		if (s == null)
			throw new Exception("WebServer is null");
		server = s;
	}

	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("channelGroupHandler", new SimpleChannelUpstreamHandler() {
			public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
				server.getChannels().add(e.getChannel());
			}
		});

		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", new mmo.websocketserver.WebServerHandler());

		return pipeline;
	}
}