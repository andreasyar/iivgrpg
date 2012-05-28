package mmo.mmortsserver.messages;

/**
 *
 * @author Sorc
 */
public class WelcomeMessage implements Message {
    private long clientId;

    public WelcomeMessage(long clientId) {
        this.clientId = clientId;
    }

    public long getClientId() {
        return clientId;
    }

    @Override
    public String toJSON() {
        return "[\"WELCOME\", " + clientId + "]";
    }
}
