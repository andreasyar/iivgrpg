/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmo.mmortsserver;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import mmo.mmortsserver.messages.Message;
import mmo.mmortsserver.messages.StateReportMessage;
import mmo.mmortsserver.units.Unit;

/**
 *
 * @author Sorc
 */

class Client {
    private long Id;
    private final LinkedList<Message> inbox = new LinkedList<>();
    private final LinkedList<Message> outbox = new LinkedList<>();
    private final LinkedList<Unit> units = new LinkedList<>();
    private final LinkedList<Message> requestedActions = new LinkedList<>();

    Client() {
        this.Id = IdGenerator.generateId();
    }

    long getId() {
        return Id;
    }

    LinkedList<Message> getInbox() {
        return inbox;
    }

    LinkedList<Message> getOutbox() {
        return outbox;
    }

    LinkedList<Unit> getUnits() {
        return units;
    }

    LinkedList<Message> getRequestedActions() {
        return requestedActions;
    }

    boolean isStateReported(long step) {

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
    
    void addToOubox(Message m) {
        inbox.add(m);
    }
    
    void addAllToOubox(Collection<Message> m) {
        inbox.addAll(m);
    }
    
    LinkedList<Message> getFromOutbox() {
        return (LinkedList<Message>) outbox.clone();
    }
    
    LinkedList<Message> takeFromOutbox() {
        LinkedList<Message> clone = (LinkedList<Message>) outbox.clone();
        outbox.clear();
        return clone;
    }
}
