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
    private MessageManager messageManager;

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
                LinkedList<Message> messages;
                LinkedList<Message> actions;
                LinkedList<Channel> clientsToDisconnect = new LinkedList<>();
                Message message;
                MoveMessage moveMessage;
                StateReportMessage stateReportMessage;
                LinkedList<Unit> missedUnits, excellUnits;
                
                // Список уведомлений, которые должны быть отправлены всем клиентам после выполнения всех действий.
                LinkedList<Message> messagesForAllClients = new LinkedList<>();
                Iterator messagesForAllClientsIterator;

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

                    /*
                     * Пройдём по всем клиентам и для каждого из его входящих
                     * сообщений (inbox) возьмём состояние за текущий шаг и
                     * проверим это состояние. Если состояние неверное, то
                     * добавим клиента в список на отключение от сервера.
                     */
                    //logger.log(Level.INFO, "Check clietns state.");
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
                                    client.getRequestedActions().addAll(stateReportMessage.getActions());
                                } else {
                                    messageManager.sendPacket(
                                        channel,
                                        messageManager.createErrorMessage(SimpleErrorService.getLastError())
                                    );
                                    clientsToDisconnect.add(channel);
                                    break;
                                }
                                messagesIterator.remove();
                            }
                        }
                    }

                    /*
                     * Отключим от сервера клиентов с неверным состоянием.
                     */
                    //logger.log(Level.INFO, "Disconnect invalid clients.");
                    for (Channel c : clientsToDisconnect) {
                        removeClient(c);
                        logger.log(Level.INFO, "Invalid client disconnected.");
                    }
                    clientsToDisconnect.clear();

                    //
                    // Обработка действий.
                    //

                    /*
                     * Пройдём по всем клиентам и выполним все действия, которые
                     * они запросили (из списка actions).
                     */
                    //logger.log(Level.INFO, "Handle clients actions.");
                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        actions = client.getRequestedActions();

                        if (!channel.isWritable()) {
                            logger.log(Level.INFO, "Channel not writible on step {0}", step);
                            continue;
                        }

                        actionsIterator = actions.iterator();
                        while (actionsIterator.hasNext()) {
                            message = (Message) actionsIterator.next();
                            if (message instanceof MoveMessage) {
                                moveMessage = (MoveMessage) message;
                                movementManager.startMovement(step, map, unitManager.getById(moveMessage.getUnitId()), moveMessage.getJ(), moveMessage.getI());
                                messagesForAllClients.add(message);
                            }
                            actionsIterator.remove();
                        }
                    }
                    
                    /*
                     * Уведомим клиентов о произошедших событиях.
                     */
                    messagesForAllClientsIterator = messagesForAllClients.iterator();
                    while (messagesForAllClientsIterator.hasNext()) {
                        message = (Message) messagesForAllClientsIterator.next();
                        
                        clientsIterator = clients.entrySet().iterator();
                        while (clientsIterator.hasNext()) {
                            pair = (Entry) clientsIterator.next();
                            client = ((Client) pair.getValue());
                            
                            client.getOutbox().add(message);
                        }
                    }
                    messagesForAllClients.clear();
                    
                    /*
                     * Уведомим каждого клиента о новых юнитах.
                     */
                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        client = ((Client) pair.getValue());

                        missedUnits = unitManager.getMissedUnits(client.getUnits());
                        client.getUnits().addAll(missedUnits);
                        for (Unit unit : missedUnits) {
                            client.getOutbox().add(messageManager.createNewUnitMessage(unit.getOwnerId(), unit.getId(), unit.getJ(), unit.getI()));
                        }
                    }
                    
                    /*
                     * Уведомим каждого клиента о юнитах, которые больше не существуют.
                     */
                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        client = ((Client) pair.getValue());
                        excellUnits = unitManager.getExcessUnits(client.getUnits());
                        client.getUnits().removeAll(excellUnits);
                        client.getOutbox().addAll(messageManager.createDelUnitMessages(excellUnits));
                    }
                    
                    /*
                     * Обновим позиции игроков.
                     */
                    //logger.log(Level.INFO, "Update units positions.");
                    movementManager.updatePositions(step);

                    //
                    // Рассылка сообщений.
                    //

                    /*
                     * Пройдём по всем клиентам и отправим им всем уведомления
                     * о действиях других клиентов и другие исходящие (outbox)
                     * сообщения. Для этого все сообщения из actions сперва
                     * перемещаются в outbox, а затем все сообщения из outbox
                     * отправляются клиенту.
                     */
                    //logger.log(Level.INFO, "Send messages.");
                    clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        messages = client.getOutbox();

                        if (!channel.isWritable()) {
                            logger.log(Level.INFO, "Channel not writible on step {0}", step);
                            continue;
                        }

                        messageManager.sendPacket(channel, messages);
                        messages.clear();
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
            client.getOutbox().add(MessageManager.createStateReportMessage(step, client.getUnits(), client.getRequestedActions()));
            logger.log(Level.INFO, "Client id:{0},channel:{1} connected.", new Object[] { client.getId(), channel.toString() });
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

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
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
            //c.close();
            if (client != null) {
                unitManager.removeUnitsByOwnerId(client.getId());
            }
        }
    }
}
class Client {
    private long Id;
    private final LinkedList<Message> inbox = new LinkedList<>();
    private final LinkedList<Message> outbox = new LinkedList<>();
    private final LinkedList<Unit> units = new LinkedList<>();
    private final LinkedList<Message> requestedActions = new LinkedList<>();

    public Client() {
        this.Id = IdGenerator.generateId();
    }

    public long getId() {
        return Id;
    }

    public LinkedList<Message> getInbox() {
        return inbox;
    }

    public LinkedList<Message> getOutbox() {
        return outbox;
    }

    public LinkedList<Unit> getUnits() {
        return units;
    }

    public LinkedList<Message> getRequestedActions() {
        return requestedActions;
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
