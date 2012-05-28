package mmo.mmortsserver.messages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.Server;
import mmo.mmortsserver.units.Unit;
import org.jboss.netty.channel.Channel;
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
    private final Gson gson = new Gson();
    
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

        String request = ((TextWebSocketFrame) frame).getText();
        JsonParser parser = new JsonParser();
        
        /*
         * Распарсим сообщение, все сообщения представляют собой массив [],
         * который содержит массивы с сообщениями. Эти массивы с сообщениями
         * имеют формат ["TYPE", ...]. Нулевой элемент задаёт тип сообщения,
         * дальше идут различные поля, которые зависят от типа сообщения.
         * [["TYPE", ...], ["TYPE", ...], ...]
         */
        JsonArray jsonMessageArray = parser.parse(request).getAsJsonArray();
        JsonArray messageFields;
        String type, subType;
        
        for (JsonElement jsonMessage : jsonMessageArray) {
            messageFields = jsonMessage.getAsJsonArray();
            type = gson.fromJson(messageFields.get(0), String.class);

            switch (type) {
                case "LOGIN":
                    // Example: ["LOGIN",{"login":"test","password":"test"}]
                    LoginMessage lm = gson.fromJson(messageFields.get(1), LoginMessage.class);
                    gameServer.connectClient(ctx.getChannel());
                    break;
                case "STATE":
                    // Example: ["STATE",2514,[["UNIT",{"ownerId":4,"id":5,"j":4,"i":4}],["UNIT",{"ownerId":4,"id":6,"j":5,"i":5}]],[]]
                    long step;
                    try {
                        step = Long.parseLong(gson.fromJson(messageFields.get(1), String.class));
                    } catch (NumberFormatException ex) {
                        logger.log(Level.INFO, "-> {0}", request);
                        logger.log(Level.SEVERE, null, ex);
                        break;
                    }
                    JsonArray units = messageFields.get(2).getAsJsonArray();
                    JsonArray actions = messageFields.get(3).getAsJsonArray();
                    JsonArray unit;
                    JsonArray action;

                    LinkedList<Message> unitsList = new LinkedList<>();
                    LinkedList<Message> actionsList = new LinkedList<>();
                    UnitMessage um;
                    MoveMessage mm;

                    for (JsonElement elem : units) {
                        unit = elem.getAsJsonArray();
                        subType = gson.fromJson(unit.get(0), String.class);
                        switch (subType) {
                            case "UNIT":
                                um = gson.fromJson(unit.get(1), UnitMessage.class);
                                unitsList.add(um);
                                break;
                            default:
                                logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }

                    for (JsonElement elem : actions) {
                        action = elem.getAsJsonArray();
                        subType = gson.fromJson(action.get(0), String.class);
                        switch (subType) {
                            case "MOVE":
                                mm = gson.fromJson(action.get(1), MoveMessage.class);
                                actionsList.add(mm);
                                break;
                            default:
                                logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }

                    gameServer.inbox(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList));
                    break;
            }
        }
    }
    
    public void sendPacket(Channel channel, LinkedList<Message> messages) {
        ArrayList<Message> messagesArray = new ArrayList<>();
        
        for (Message message : messages) {
            messagesArray.add(message);
        }
        
        String packet = gson.toJson(messagesArray.toArray());
        logger.log(Level.INFO, "Attempt to send packet: {0}", packet);
        channel.write(new TextWebSocketFrame(packet));
    }
}
