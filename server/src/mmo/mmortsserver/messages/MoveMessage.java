package mmo.mmortsserver.messages;

import com.google.gson.Gson;

/**
 *
 * @author Sorc
 */
public class MoveMessage implements Message {
    private transient final Gson gson = new Gson();

    private Long unitId;
    private int j;
    private int i;

    public MoveMessage(Long unitId, int j, int i) {
        this.unitId = unitId;
        this.j = j;
        this.i = i;
    }

    public Long getUnitId() {
        return unitId;
    }

    public int getJ() {
        return j;
    }

    public int getI() {
        return i;
    }

    @Override
    public String toJSON() {
        return "[\"MOVE\",{\"unitId\":" + unitId + ",\"j\":" + j + ",\"i\":" + i + "}]";
    }
}
