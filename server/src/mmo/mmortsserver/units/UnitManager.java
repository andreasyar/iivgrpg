package mmo.mmortsserver.units;

import java.util.LinkedList;
import mmo.mmortsserver.IdGenerator;
import mmo.mmortsserver.messages.Message;
import mmo.mmortsserver.messages.UnitMessage;

/**
 *
 * @author Sorc
 */
public class UnitManager {
    private final LinkedList<Unit> allUnits = new LinkedList<>();
    
    public Unit createUnit(long ownerId, int j, int i) {
        Unit unit = new Unit(ownerId, IdGenerator.generateId(), j, i);
        allUnits.add(unit);
        return unit;
    }
    
    public boolean isStateValid(LinkedList<Message> unitMessages) {
        UnitMessage m;
        boolean found;
        
        for (Unit u : allUnits) {
            found = false;
            for (Message um : unitMessages) {
                m = (UnitMessage) um;
                if (u.getId() == m.getId()) {
                    found = true;
                    if (u.getOwnerId() != m.getOwnerId() || u.getJ() != m.getJ() || u.getI() != m.getI()) {
                        return false;
                    }
                }
            }
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
}
