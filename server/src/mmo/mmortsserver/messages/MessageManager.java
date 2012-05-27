package mmo.mmortsserver.messages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.Server;
import mmo.mmortsserver.units.Unit;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 *
 * @author Sorc
 */
public class MessageManager extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(MessageManager.class.getName());
    
    private Server gameServer;
    
    public static StateReportMessage createStateReportMessage(long step, LinkedList<Unit> units, LinkedList<Message> actions) {
        LinkedList<Message> unitMessages = new LinkedList<>();
        
        for (Unit u : units) {
            unitMessages.add(new UnitMessage(u.getOwnerId(), u.getId(), u.getJ(), u.getI()));
        }
        
        return new StateReportMessage(step, unitMessages, actions);
    }

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
        
        JsonArray messageFields = parser.parse(request).getAsJsonArray();
        String type = gson.fromJson(messageFields.get(0), String.class);

        switch (type) {
            case "LOGIN":
                LoginMessage m = gson.fromJson(messageFields.get(1), LoginMessage.class);
                gameServer.connectClient(ctx.getChannel());
                break;
            case "STATE":
                long step = Long.parseLong(gson.fromJson(messageFields.get(1), String.class));
                JsonArray units = messageFields.get(2).getAsJsonArray();
                JsonArray actions = messageFields.get(3).getAsJsonArray();
                JsonArray action;
                
                LinkedList<Message> unitsList = new LinkedList<>();
                LinkedList<Message> actionsList = new LinkedList<>();
                UnitMessage um;
                MoveMessage mm;
                
                for (JsonElement elem : units) {
                    um = gson.fromJson(elem, UnitMessage.class);
                    unitsList.add(um);
                }
                
                for (JsonElement elem : actions) {
                    action = elem.getAsJsonArray();
                    switch (gson.fromJson(action.get(0), String.class)) {
                        case "MOVE":
                            mm = gson.fromJson(action.get(1), MoveMessage.class);
                            actionsList.add(mm);
                            logger.log(Level.INFO, "Add action to actions list.");
                            break;
                    }
                }
                
                gameServer.inbox(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList));
                break;
        }
    }
}
