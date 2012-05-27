package mmo.mmortsserver.units;

/**
 *
 * @author Sorc
 */
public class Unit {
    private long ownerId;
    private long id;
    private int j;
    private int i;

    public Unit(long ownerId, long id, int j, int i) {
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
    
    public String toJSON() {
        return "[\"UNIT\", " + ownerId + ", " + id + ", " + j + ", " + i + "]";
    }
}
