package mmo.mmortsserver;

import java.util.Collection;
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
    //private final ConcurrentHashMap<Channel, Client> clients = new ConcurrentHashMap();
    //private final ConcurrentHashMap<Client, Channel> channels = new ConcurrentHashMap();
    private long step = 1;
    private MessageManager messageManager;
    private final ClientChannelMaps clientChannelMaps = new ClientChannelMaps();
    
    /**
     * Список запросов действий от всех клиентов на текущем шаге.
     */
    private final LinkedList<ActionRequest> actionsRequests = new LinkedList<>();

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        mainLoop = new Runnable() {
            private int waitIterations = 0;

            @Override
            public void run() {
                Iterator clientsIterator, messagesIterator, actionsIterator, actionRequestsIterator;
                Entry pair;
                Channel channel;
                Client client;
                LinkedList<Message> messages;
                LinkedList<Channel> clientsToDisconnect = new LinkedList<>();
                Message message;
                MoveMessage moveMessage;
                StateReportMessage stateReportMessage;
                LinkedList<Unit> missedUnits, excellUnits;
                
                // Список уведомлений, которые должны быть отправлены всем клиентам после выполнения всех действий.
                LinkedList<Message> messagesForAllClients = new LinkedList<>();
                Iterator messagesForAllClientsIterator;
                
                // Входящие сообщения.
                LinkedList<InboxMessage> inbox;
                Iterator inboxIterator;
                InboxMessage inboxMessage;
                
                ActionRequest actionRequest;
                Message action;

                if (!isAllClientsReported(step) && waitIterations < 5) {
                    logger.log(Level.INFO, "Wait for clients report for step {0}", step);
                    waitIterations++;
                    return;
                } else if (!isAllClientsReported(step)) {
                    waitIterations = 0;
                    disconnectUnreportedClients(step);
                } else {
                    waitIterations = 0;
                }

                synchronized (clientChannelMaps) {
                    
                    /*
                     * Пройдём по всем входящим сообщениям и обработаем их.
                     */
                    logger.log(Level.INFO, "Process incoming messages.");
                    inbox = messageManager.takeFromInbox();
                    inboxIterator = inbox.iterator();
                    while (inboxIterator.hasNext()) {
                        inboxMessage = (InboxMessage) inboxIterator.next();
                        channel = inboxMessage.getFrom();
                        message = inboxMessage.getMessage();
                        client = clientChannelMaps.getClients().get(channel);
                        
                        if (message instanceof StateReportMessage) {
                            stateReportMessage = (StateReportMessage) message;
                            
                            if (client.getLastConfirmedStep() == step) {
                                // Клиент прислал два или более отчётов за один и тот же шаг? Тут явно что-то не так.
                                messageManager.sendPacket(channel, messageManager.createErrorMessage("Dublicated state report."));
                                clientsToDisconnect.add(channel);
                                continue;
                            }
                            if (stateReportMessage.getStep() == step && unitManager.isStateValid(stateReportMessage.getUnits())) {
                                client.setLastConfirmedStep(step);
                                addAllActionsRequests(client, stateReportMessage.getActions());
                            } else {
                                messageManager.sendPacket(channel, messageManager.createErrorMessage(SimpleErrorService.getLastError()));
                                clientsToDisconnect.add(channel);
                            }
                            continue;
                        }
                    }

                    /*
                     * Отключим от сервера клиентов с неверным состоянием.
                     */
                    logger.log(Level.INFO, "Disconnect invalid clients.");
                    for (Channel c : clientsToDisconnect) {
                        client = clientChannelMaps.getClients().get(c);
                        cleanupClient(client);
                        clientChannelMaps.getChannels().remove(client);
                        clientChannelMaps.getClients().remove(c);
                        logger.log(Level.INFO, "Invalid client disconnected.");
                    }
                    clientsToDisconnect.clear();

                    /*
                     * Пройдём по запросам действий и выполним их.
                     */
                    logger.log(Level.INFO, "Handle clients actions.");
                    actionRequestsIterator = actionsRequests.iterator();
                    while (actionRequestsIterator.hasNext()) {
                        actionRequest = (ActionRequest) actionRequestsIterator.next();
                        client = actionRequest.getClient();
                        action = actionRequest.getRequest();
                        
                        if (action instanceof MoveMessage) {
                            moveMessage = (MoveMessage) action;
                            movementManager.startMovement(step, map, unitManager.getById(moveMessage.getUnitId()), moveMessage.getJ(), moveMessage.getI());
                            notifyAllOtherClients(client, action);
                        }
                        
                        actionRequestsIterator.remove();
                    }

                    /*
                     * Пройдём по всем клиентам и отправим им всё что накопилось
                     * в их исходящих сообщениях.
                     */
                    logger.log(Level.INFO, "Send messages.");
                    clientsIterator = clientChannelMaps.getClients().entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();

                        messageManager.sendConfirmMessage(channel, step, client.takeFromOutbox());
                    }

                    /*
                     * 1. Проверка состояний.
                     */

                    /*
                     * Пройдём по всем клиентам и проверим их состояния. Если
                     * состояние какого-то клиента окажется неверным, то этот
                     * клиент добавляется в список на отключение.
                     */
                    //logger.log(Level.INFO, "Check clietns state.");
                    /*clientsIterator = clientChannelMaps.getClients().entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        messages = client.getInbox();

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
                    }*/

                    /*
                     * Отключим от сервера клиентов с неверным состоянием.
                     */
                    //logger.log(Level.INFO, "Disconnect invalid clients.");
                    /*for (Channel c : clientsToDisconnect) {
                        cleanupClient(c);
                        logger.log(Level.INFO, "Invalid client disconnected.");
                    }
                    clientsToDisconnect.clear();*/

                    //
                    // Обработка действий.
                    //

                    /*
                     * Пройдём по всем клиентам и выполним все действия, которые
                     * они запросили (из списка actions).
                     */
                    //logger.log(Level.INFO, "Handle clients actions.");
                    /*clientsIterator = clients.entrySet().iterator();
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
                    }*/
                    
                    /*
                     * Уведомим клиентов о произошедших событиях.
                     */
                    /*messagesForAllClientsIterator = messagesForAllClients.iterator();
                    while (messagesForAllClientsIterator.hasNext()) {
                        message = (Message) messagesForAllClientsIterator.next();
                        
                        clientsIterator = clients.entrySet().iterator();
                        while (clientsIterator.hasNext()) {
                            pair = (Entry) clientsIterator.next();
                            client = ((Client) pair.getValue());
                            
                            client.getOutbox().add(message);
                        }
                    }
                    messagesForAllClients.clear();*/
                    
                    /*
                     * Уведомим каждого клиента о новых юнитах.
                     */
                    /*clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        client = ((Client) pair.getValue());

                        missedUnits = unitManager.getMissedUnits(client.getUnits());
                        client.getUnits().addAll(missedUnits);
                        for (Unit unit : missedUnits) {
                            client.getOutbox().add(messageManager.createNewUnitMessage(unit.getOwnerId(), unit.getId(), unit.getJ(), unit.getI()));
                        }
                    }*/
                    
                    /*
                     * Уведомим каждого клиента о юнитах, которые больше не существуют.
                     */
                    /*clientsIterator = clients.entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        client = ((Client) pair.getValue());
                        excellUnits = unitManager.getExcessUnits(client.getUnits());
                        client.getUnits().removeAll(excellUnits);
                        client.getOutbox().addAll(messageManager.createDelUnitMessages(excellUnits));
                    }*/
                    
                    /*
                     * Обновим позиции игроков.
                     */
                    //logger.log(Level.INFO, "Update units positions.");
                    //movementManager.updatePositions(step);

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
                    /*clientsIterator = clients.entrySet().iterator();
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
                    }*/

                    step++;
                    
                    /*
                     * Обновим позиции игроков.
                     */
                    logger.log(Level.INFO, "Update units positions.");
                    movementManager.updatePositions(step);
                }
            }
        };
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(mainLoop, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void clientConnected(Channel channel) {
        synchronized (clientChannelMaps) {
            
            
        }
        
        /*synchronized (clients) {
            Client client = new Client();
            clients.put(channel, client);
            client.getUnits().add(unitManager.createUnit(client.getId(), 4, 4));
            client.getUnits().add(unitManager.createUnit(client.getId(), 5, 5));
            client.getOutbox().add(new WelcomeMessage(client.getId()));
            client.getOutbox().add(MessageManager.createStateReportMessage(step, client.getUnits(), client.getRequestedActions()));
            logger.log(Level.INFO, "Client id:{0},channel:{1} connected.", new Object[] { client.getId(), channel.toString() });
        }*/
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }
    
    /**
     * Утверждает, что клиент отчитался за шаг step.
     * @param c канал
     * @param step шаг
     */
    public void clientReported(Channel c, long step) {
        synchronized (clientChannelMaps) {
            Client client = clientChannelMaps.getClients().get(c);
            if (client != null) {
                client.setLastReportedStep(step);
            } else {
                logger.log(Level.SEVERE, "Attempt to update last reported step of unexisted client. Ignore.");
            }
        }
    }

    /**
     * Добавляет уведомление о действии в исходящие сообщения всем клиентам кроме client.
     * @param client клиент
     * @param action уведомление
     */
    private void notifyAllOtherClients(Client client, Message action) {
        Entry pair;
        Client curClient;

        Iterator i = clientChannelMaps.getClients().entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            curClient = (Client) pair.getValue();
            if (!curClient.equals(client)) {
                curClient.addToOubox(action);
            }
        }
    }

    /**
     * Добавляет все запросы действий в список запросов действий клиентов.
     * @param client клиент, от которого исходят запросы действий.
     * @param requests запросы действий.
     */
    private void addAllActionsRequests(Client client, Collection<Message> requests) {
        for (Message r : requests) {
            actionsRequests.add(new ActionRequest(client, r));
        }
    }
    
    /**
     * Проверяет, все ли клиенты отчитались за шаг step.
     * @param step шаг
     */
    private boolean isAllClientsReported(long step) {
        Entry pair;
        Client client;

        synchronized (clientChannelMaps) {
            Iterator i = clientChannelMaps.getClients().entrySet().iterator();
            while (i.hasNext()) {
                pair = (Entry) i.next();
                client = (Client) pair.getValue();
                if (client.getLastReportedStep() != step) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Удаляет всех клиентов, не отчитавшихся за шаг step.
     * @param step шаг
     */
    private void disconnectUnreportedClients(long step) {
        Entry pair;
        Client client;

        synchronized (clientChannelMaps) {
            Iterator i = clientChannelMaps.getClients().entrySet().iterator();
            while (i.hasNext()) {
                pair = (Entry) i.next();
                client = (Client) pair.getValue();
                if (client.getLastReportedStep() != step) {
                    i.remove();
                    cleanupClient(client);
                }
            }
        }
    }

    /**
     * Очистка, которая производится при удалении клиента.
     */
    private void cleanupClient(Client client) {
        unitManager.removeUnitsByOwnerId(client.getId());
    }
    
    /**
     * Класс-обёртка для синхронизации доступа к хешам клиентов и каналов.
     */
    private class ClientChannelMaps {
        
        /**
         * Хеш клиентов. Канал -> Клиент.
         */
        private final ConcurrentHashMap<Channel, Client> clients = new ConcurrentHashMap();
        
        /**
         * Хеш каналов. Клиент -> Канал.
         */
        private final ConcurrentHashMap<Client, Channel> channels = new ConcurrentHashMap();
    
        /**
        * Вновь подключившиеся клиенты, ожидающие начального состояния, чтобы начать работу.
        */
        private final LinkedList<Client> awaitingStateClients = new LinkedList<>();

        public ConcurrentHashMap<Channel, Client> getClients() {
            return clients;
        }

        public ConcurrentHashMap<Client, Channel> getChannels() {
            return channels;
        }

        public LinkedList<Client> getAwaitingStateClients() {
            return awaitingStateClients;
        }
    }
    
    /**
     * Запрос действия.
     */
    private class ActionRequest {
    
       /**
        * Клиент, от которого исходит запрос действия.
        */
        private final Client client;

       /**
        * Запрос действия.
        */
        private final Message request;

        public ActionRequest(Client client, Message request) {
            this.client = client;
            this.request = request;
        }

        public Client getClient() {
            return client;
        }

        public Message getRequest() {
            return request;
        }
    }
}
