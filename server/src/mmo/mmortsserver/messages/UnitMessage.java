package mmo.mmortsserver.messages;

import com.google.gson.Gson;

/**
 *
 * @author Sorc
 */
public class UnitMessage implements Message {
    private transient final Gson gson = new Gson();
    
    private String type = "UNIT";
    private long ownerId;
    private long id;
    private int j;
    private int i;

    public UnitMessage(long ownerId, long id, int j, int i) {
        this.ownerId = ownerId;
        this.id = id;
        this.j = j;
        this.i = i;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public long getId() {
        return id;
    }

    public int getJ() {
        return j;
    }

    public int getI() {
        return i;
    }

    @Override
    public String toJSON() {
        return gson.toJson(this);
    }
}
