/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mmo.mmortsserver.messages;

import java.util.LinkedList;
import java.util.logging.Logger;

/**
 *
 * @author Sorc
 */
public class MessagePacket implements Message {
    private static final Logger logger = Logger.getLogger(MessagePacket.class.getName());
    
    private LinkedList<Message> messages;

    public MessagePacket(LinkedList<Message> messages) {
        this.messages = messages;
    }

    public MessagePacket(Message message) {
        this.messages = new LinkedList<>();
        this.messages.add(message);
    }

    @Override
    public String toJSON() {
        String out = "[";
        for (int i = 0; i < messages.size() - 1; i++) {
                out += messages.get(i).toJSON() + ",";
        }
        out += messages.get(messages.size() - 1).toJSON() + "]";
        return out;
    }
}
