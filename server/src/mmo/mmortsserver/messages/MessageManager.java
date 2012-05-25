package mmo.mmortsserver.messages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import mmo.mmortsserver.Server;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 *
 * @author Sorc
 */
public class MessageManager extends SimpleChannelUpstreamHandler {
    private Server gameServer;

    public MessageManager(Server gameServer) {
        this.gameServer = gameServer;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object frame = e.getMessage();
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        
        // Парсим.
        String request = ((TextWebSocketFrame) frame).getText();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(request).getAsJsonArray();
        String type = gson.fromJson(array.get(0), String.class);
        Object body = null;
        Message m = null;
        
        switch (type) {
            case "LOGIN":
                body = gson.fromJson(array.get(1), JSONLoginMessage.class);
                m = new LoginMessage(((JSONLoginMessage) body).getLogin(), ((JSONLoginMessage) body).getPassword());
                break;
            case "MOVE":
                body = gson.fromJson(array.get(1), JSONMoveMessage.class);
                m = new MoveMessage(null, ((JSONMoveMessage) body).getJ(), ((JSONMoveMessage) body).getI());
                break;
        }
        
        // Отправляем сообщение обратно.
        //ctx.getChannel().write(new TextWebSocketFrame("[" + gson.toJson(type) + "," + gson.toJson(body) + "]"));
        if (m != null) {
            gameServer.inbox(ctx.getChannel(), m);
        }
    }
    
    private class JSONLoginMessage {
        private String login;
        private String password;
        public JSONLoginMessage() {}
        public String getLogin() {
            return login;
        }
        public String getPassword() {
            return password;
        }
    }
    
    private class JSONMoveMessage {
        private int j;
        private int i;
        public JSONMoveMessage() {}
        public int getJ() {
            return j;
        }
        public int getI() {
            return i;
        }
    }
}
