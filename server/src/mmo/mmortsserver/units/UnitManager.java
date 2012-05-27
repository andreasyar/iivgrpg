package mmo.mmortsserver.units;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.IdGenerator;
import mmo.mmortsserver.Server;
import mmo.mmortsserver.messages.Message;
import mmo.mmortsserver.messages.UnitMessage;

/**
 *
 * @author Sorc
 */
public class UnitManager {
    private static final Logger logger = Logger.getLogger(UnitManager.class.getName());

    // TODO Syncronization!!!
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
                        logger.log(Level.WARNING, "Incorrect unit state.\nUnit id: {6}\nownerId: s {0} == c {1}, j: s {2} == c {3}, i: s {4} == c {5}", new Object[]{u.getOwnerId(), m.getOwnerId(), u.getJ(), m.getJ(), u.getI(), m.getI(), u.getId()});
                        return false;
                    }
                }
            }
            if (!found) {
                logger.log(Level.INFO, "Unit id {0} not found", u.getId());
                return false;
            }
        }

        return true;
    }

    public void removeUnits(LinkedList<Unit> units) {
        for (Unit u : units) {
            allUnits.remove(u);
        }
    }

    public Unit getById(Long unitId) {
        for (Unit u : allUnits) {
            if (u.getId() == unitId) {
                return u;
            }
        }
        
        return null;
    }
}
