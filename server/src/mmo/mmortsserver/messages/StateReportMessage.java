package mmo.mmortsserver.messages;

import com.google.gson.Gson;
import java.util.LinkedList;

/**
 *
 * @author Sorc
 */
public class StateReportMessage implements Message {
    private transient final Gson gson = new Gson();
    
    private String type = "STATE";
    private long step;
    private LinkedList<Message> units;
    private LinkedList<Message> actions;

    public StateReportMessage(long step, LinkedList<Message> units, LinkedList<Message> actions) {
        this.step = step;
        this.units = units;
        this.actions = actions;
    }

    public long getStep() {
        return step;
    }

    public LinkedList<Message> getUnits() {
        return units;
    }

    public LinkedList<Message> getActions() {
        return actions;
    }

    @Override
    public String toJSON() {
        return "[\"" + type + "\", " + step + ", " + gson.toJson(units) + ", " + gson.toJson(actions) + "]"; //gson.toJson(this);
    }
}
