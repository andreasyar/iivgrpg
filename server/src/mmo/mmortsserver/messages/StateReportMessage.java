package mmo.mmortsserver.messages;

import com.google.gson.Gson;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sorc
 */
public class StateReportMessage implements Message {
    private static final Logger logger = Logger.getLogger(MessagePacket.class.getName());
    
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
        String out = "[\"" + type + "\"," + step + ",[";
        if (units.size() > 0) {
            for (int i = 0; i < units.size() - 1; i++) {
                out += units.get(i).toJSON() + ",";
            }
            out += units.get(units.size() - 1).toJSON() + "],[";
        } else {
            out += "],[";
        }
        if (actions.size() > 0) {
            for (int i = 0; i < actions.size() - 1; i++) {
                out += actions.get(i).toJSON() + ",";
            }
            out += actions.get(actions.size() - 1).toJSON() + "]]";
        } else {
            out += "]]";
        }
        return out;
    }
}
