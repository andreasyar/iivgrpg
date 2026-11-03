package mmo.webserver;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import mmo.config.XMLConfig;
import mmo.gameserver.GameClient;
import mmo.gameserver.GameServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class WebServer {
    private static final int DEFAULT_PORT = 8015;
    private static final String DEFAULT_CLIENT_PATH =  File.separator + ".." + File.separator +"client";
    private static final String DEFAULT_FILE =  "index.html";

    private int port;
    private String clientFolder;
    private String defaultFile;
    private ServerBootstrap bootstrap;
    private ChannelGroup channels = new DefaultChannelGroup();

    private GameServer gameserver;

    public ChannelGroup getChannels() {
        return channels;
    }

    public int getPort() {
        return port;
    }

    public String getClientFolder() {
        return clientFolder;
    }

    public String getDefaultFile() {
        return defaultFile;
    }

    public WebServer(XMLConfig xmlconfig, GameServer gameserver) {
        if (xmlconfig!= null) {
            port = xmlconfig.getInt("webserver/port", DEFAULT_PORT);
            clientFolder = xmlconfig.getString("webserver/clientpath", DEFAULT_CLIENT_PATH);
            defaultFile = xmlconfig.getString("webserver/defaultfile", DEFAULT_FILE);
        } else {
            port = DEFAULT_PORT;
            clientFolder = DEFAULT_CLIENT_PATH;
            defaultFile = DEFAULT_FILE;
        }
        this.gameserver = gameserver;
    }

    public void start() throws Exception {
        bootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool())
        );
        bootstrap.setPipelineFactory(new WebServerPipelineFactory(this,gameserver));
        channels.add(bootstrap.bind(new InetSocketAddress(port)));
    }

    public void stop() throws Exception {
        channels.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }

    @Override
    protected void finalize() throws Exception {
        stop();
    }

    public void onClientChannelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        getChannels().add(e.getChannel());
    }

    public void onClientChannelClose(ChannelHandlerContext ctx, ChannelStateEvent e) {
        getChannels().remove(e.getChannel());
    }

    public void onClientDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        gameserver.onClientDisconnect(GameClient.getClient(ctx.getChannel()));
    }
}
