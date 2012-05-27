package mmo.mmortsserver.messages;

/**
 *
 * @author Sorc
 */
public class ConfirmMessage implements Message {
    private long step;

    public ConfirmMessage(long step) {
        this.step = step;
    }

    public long getStep() {
        return step;
    }

    @Override
    public String toJSON() {
        return "[\"CONFIRM\", " + step + "]";
    }
}
