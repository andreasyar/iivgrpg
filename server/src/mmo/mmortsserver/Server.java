package mmo.mmortsserver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.map.Map;
import mmo.mmortsserver.messages.*;
import mmo.mmortsserver.movement.MovementManager;
import mmo.mmortsserver.units.Unit;
import mmo.mmortsserver.units.UnitManager;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;


/**
 *
 * @author Sorc
 */
public class Server {
    // TODO А зачем логгеру тут (да и в других классах) быть статиком?
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private UnitManager unitManager = new UnitManager();
    private MovementManager movementManager = new MovementManager();
    private Map map = new Map(100, 100);
    private Runnable mainLoop;
    private final ConcurrentHashMap<Channel, Client> clients = new ConcurrentHashMap();
    private long step = 1;

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        mainLoop = new Runnable() {
            private int waitIterations = 0;

            @Override
            public void run() {
                Iterator clientsIterator, messagesIterator, actionsIterator;
                Entry pair;
                Channel channel;
                Client client;
                LinkedBlockingQueue<Message> messages;
                LinkedList<Message> actions;
                LinkedList<Channel> clientsToDisconnect = new LinkedList<>();
                Message message;
                MoveMessage moveMessage;
                StateReportMessage stateReportMessage;

                if (!allClientsReported(step) && waitIterations < 5) {
                    logger.log(Level.INFO, "Wait for clients report for step {0}", step);
                    waitIterations++;
                    return;
                } else if (!allClientsReported(step)) {
                    waitIterations = 0;
                    disconnectUnreportedClients(step);
                } else {
                    waitIterations = 0;
                }

                synchronized (clients) {

                    //
                    // Проверка состояний.
                    //

                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        messages = client.getInbox();

                        if (!channel.isWritable()) {
                            logger.log(Level.INFO, "Channel not writible on step {0}", step);
                            continue;
                        }

                        messagesIterator = messages.iterator();
                        while (messagesIterator.hasNext()) {
                            message = (Message) messagesIterator.next();
                            if (message instanceof StateReportMessage) {
                                stateReportMessage = (StateReportMessage) message;
                                if (unitManager.isStateValid(stateReportMessage.getUnits())) {
                                    client.getOutbox().add(new ConfirmMessage(step));
                                    client.getActions().addAll(stateReportMessage.getActions());
                                } else {
                                    clientsToDisconnect.add(channel);
                                    break;
                                }
                            }
                            messagesIterator.remove();
                        }
                    }

                    for (Channel c : clientsToDisconnect) {
                        removeClient(c);
                        logger.log(Level.INFO, "Disconnect bad client");
                    }
                    clientsToDisconnect.clear();

                    //
                    // Обработка действий.
                    //

                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        actions = client.getActions();

                        if (!channel.isWritable()) {
                            logger.log(Level.INFO, "Channel not writible on step {0}", step);
                            continue;
                        }

                        actionsIterator = actions.iterator();
                        logger.log(Level.INFO, "{0} actions on step {1}", new Object[] {actions.size(), step});
                        while (actionsIterator.hasNext()) {
                            message = (Message) actionsIterator.next();
                            if (message instanceof MoveMessage) {
                                moveMessage = (MoveMessage) message;
                                movementManager.startMovement(step, map, unitManager.getById(moveMessage.getUnitId()), moveMessage.getJ(), moveMessage.getI());
                                logger.log(Level.INFO, "Movement created on {0}", step);
                                // TODO Уведомить остальных клиентов!
                            }
                            actionsIterator.remove();
                        }
                    }
                    movementManager.updatePositions(step);

                    //
                    // Рассылка сообщений.
                    //

                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        messages = client.getOutbox();
                        actions = client.getActions();

                        if (!channel.isWritable()) {
                            logger.log(Level.INFO, "Channel not writible on step {0}", step);
                            continue;
                        }

                        actionsIterator = actions.iterator();
                        while (actionsIterator.hasNext()) {
                            message = (Message) actionsIterator.next();
                            messages.add(message);
                            actionsIterator.remove();
                        }

                        messagesIterator = messages.iterator();
                        while (messagesIterator.hasNext()) {
                            message = (Message) messagesIterator.next();
                            channel.write(new TextWebSocketFrame(message.toJSON()));
                            logger.log(Level.INFO, "Send message {0}", message.toJSON());
                            messagesIterator.remove();
                        }
                    }

                    step++;
                }
            }
        };
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(mainLoop, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void connectClient(Channel channel) {
        synchronized (clients) {
            Client client = new Client();
            clients.put(channel, client);
            client.getUnits().add(unitManager.createUnit(client.getId(), 4, 4));
            client.getUnits().add(unitManager.createUnit(client.getId(), 5, 5));
            client.getOutbox().add(new WelcomeMessage(client.getId()));
            client.getOutbox().add(MessageManager.createStateReportMessage(step, client.getUnits(), client.getActions()));
            logger.log(Level.INFO, "Client on channel {0} connected.", channel.toString());
        }
    }

    public void disconnectClient(Channel c) {
        synchronized (clients) {
            removeClient(c);
            logger.log(Level.INFO, "Client on channel {0} disconnected.", c.toString());
        }
    }

    public void inbox(Channel channel, Message m) {
        synchronized (clients) {
            Client client = clients.get(channel);
            if (client != null) {
                client.getInbox().add(m);
            } else {
                logger.log(Level.WARNING, "Client on channel {0} not found.", channel.toString());
            }
        }
    }

    public long getStep() {
        return step;
    }

    private boolean allClientsReported(long step) {
        Iterator i;
        Entry pair;
        Client client;

        i = clients.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (!client.isStateReported(step)) {
                return false;
            }
        }

        return true;
    }

    private void disconnectUnreportedClients(long step) {
        Iterator i;
        Entry pair;
        Channel channel;
        Client client;

        i = clients.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            channel = (Channel) pair.getKey();
            client = (Client) pair.getValue();
            if (!client.isStateReported(step)) {
                removeClient(channel);
            }
        }
    }

    private void removeClient(Channel c) {
        synchronized (clients) {
            Client client = clients.remove(c);
            c.close();
            if (client != null) {
                unitManager.removeUnits(client.getUnits());
            }
        }
    }
}
class Client {
    private long Id;
    private final LinkedBlockingQueue<Message> inbox = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> outbox = new LinkedBlockingQueue<>();
    private final LinkedList<Unit> units = new LinkedList<>();
    private final LinkedList<Message> actions = new LinkedList<>();

    public Client() {
        this.Id = IdGenerator.generateId();
    }

    public long getId() {
        return Id;
    }

    public LinkedBlockingQueue<Message> getInbox() {
        return inbox;
    }

    public LinkedBlockingQueue<Message> getOutbox() {
        return outbox;
    }

    public LinkedList<Unit> getUnits() {
        return units;
    }

    public LinkedList<Message> getActions() {
        return actions;
    }

    public boolean isStateReported(long step) {

        // Для вновь подключенных клиентов, которые ещё не были инициализированы.
        if (getOutbox().size() != 0) {
            return true;
        }

        Message message;
        Iterator i = inbox.iterator();
        boolean found = false;

        while (i.hasNext()) {
            message = (Message) i.next();
            if (message instanceof StateReportMessage) {
                if (((StateReportMessage) message).getStep() == step) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }
}
