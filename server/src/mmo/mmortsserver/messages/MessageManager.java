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

    private final LinkedList<InboxMessage> commonInbox = new LinkedList<>();

    private Server gameServer;
    private final Gson gson = new Gson();

    public static StateReportMessage createStateReportMessage(long step, LinkedList<Unit> units, LinkedList<Message> actions) {
        LinkedList<Message> unitMessages = new LinkedList<>();

        for (Unit u : units) {
            unitMessages.add(new UnitMessage(u.getOwnerId(), u.getId(), u.getJ(), u.getI()));
        }

        return new StateReportMessage(step, unitMessages, actions);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object frame = e.getMessage();
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
        String type;

        for (JsonElement jsonMessage : jsonMessageArray) {
            messageFields = jsonMessage.getAsJsonArray();
            type = gson.fromJson(messageFields.get(0), String.class);

            switch (type) {
                case "LOGIN":
                    LoginMessage lm = gson.fromJson(messageFields.get(1), LoginMessage.class);
                    gameServer.clientConnected(ctx.getChannel());
                    break;
                case "STATE":
                    long step = Long.parseLong(gson.fromJson(messageFields.get(1), String.class));
                    JsonArray units = messageFields.get(2).getAsJsonArray();
                    JsonArray actions = messageFields.get(3).getAsJsonArray();
                    JsonArray unit;
                    JsonArray action;

                    LinkedList<Message> unitsList = new LinkedList<>();
                    LinkedList<Message> actionsList = new LinkedList<>();
                    UnitMessage unitMessage;
                    MoveMessage moveMessage;
                    String subType;

                    for (JsonElement elem : units) {
                        unit = elem.getAsJsonArray();
                        subType = gson.fromJson(unit.get(0), String.class);
                        switch (subType) {
                            case "UNIT":
                                unitMessage = gson.fromJson(unit.get(1), UnitMessage.class);
                                unitsList.add(unitMessage);
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
                                moveMessage = gson.fromJson(action.get(1), MoveMessage.class);
                                actionsList.add(moveMessage);
                                break;
                            default:
                                logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }

                    //gameServer.inbox(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList));
                    synchronized (commonInbox) {
                        commonInbox.add(new InboxMessage(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList)));
                    }
                    gameServer.clientReported(ctx.getChannel(), step);
                    break;
            }
        }
    }

    public void sendPacket(Channel channel, Message message) {
        String packet = new MessagePacket(message).toJSON();
        //logger.log(Level.INFO, "Attempt to send packet: {0}", packet);
        channel.write(new TextWebSocketFrame(packet));
    }

    public void sendPacket(Channel channel, LinkedList<Message> messages) {
        String packet = new MessagePacket(messages).toJSON();
        logger.log(Level.INFO, "Attempt to send packet: {0}", packet);
        channel.write(new TextWebSocketFrame(packet));
    }

    public void setGameServer(Server gameServer) {
        this.gameServer = gameServer;
    }

    public NewUnitMessage createNewUnitMessage(long ownerId, long id, int j, int i) {
        return new NewUnitMessage(ownerId, id, j, i);
    }

    public LinkedList<DelUnitMessage> createDelUnitMessages(LinkedList<Unit> excessUnits) {
        LinkedList<DelUnitMessage> delUnitMessages = new LinkedList<>();
        for (Unit u : excessUnits) {
            delUnitMessages.add(new DelUnitMessage(u.getId()));
        }
        return delUnitMessages;
    }

    public ErrorMessage createErrorMessage(String text) {
        return new ErrorMessage(text);
    }

    public LinkedList<InboxMessage> takeFromInbox() {
        LinkedList<InboxMessage> clone;

        synchronized (commonInbox) {
            clone = (LinkedList<InboxMessage>) commonInbox.clone();
            commonInbox.clear();
        }

        return clone;
    }

    public void sendConfirmMessage(Channel channel, long step, LinkedList<Message> messages) {
        String packet = new ConfirmMessage(step, messages).toJSON();
        logger.log(Level.INFO, "Attempt to send confirm: {0}", packet);
        channel.write(new TextWebSocketFrame(packet));
    }
}
