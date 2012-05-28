package mmo.mmortsserver.units;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.IdGenerator;
import mmo.mmortsserver.messages.Message;
import mmo.mmortsserver.messages.UnitMessage;

/**
 *
 * @author Sorc
 */
public class UnitManager {
    private static final Logger logger = Logger.getLogger(UnitManager.class.getName());

    private final LinkedList<Unit> allUnits = new LinkedList<>();

    public Unit createUnit(long ownerId, int j, int i) {
        Unit unit = new Unit(ownerId, IdGenerator.generateId(), j, i);
        synchronized (allUnits) {
            allUnits.add(unit);
        }
        return unit;
    }

    public boolean isStateValid(LinkedList<Message> unitMessages) {
        UnitMessage m;
        //boolean found;
        int i;
        Unit u;

        /*
         * Пройдём по всем юнитам и проверим, соответвует ли их состояние
         * состояниям в сообщениях (unitMessages). Пока что все клиенты
         * занют обо всех юнитах, поэтому не важно по чему пойдёт внешний
         * цикл по всем юнитам или по сообщениям.
         */
        synchronized (allUnits) {
            for (Message um : unitMessages) {
                m = (UnitMessage) um;
                if ( (i = allUnits.indexOf(m)) != -1) {
                    u = allUnits.get(i);
                    if (u.getOwnerId() != m.getOwnerId() || u.getJ() != m.getJ() || u.getI() != m.getI()) {
                        logger.log(
                            Level.INFO,
                            "State invalid.\nUnit id:{6}\nServer: ownerId:{0},j:{2},i:{4}\nClient: ownerId:{1},j:{3},i:{5}",
                            new Object[] { u.getOwnerId(), m.getOwnerId(), u.getJ(), m.getJ(), u.getI(), m.getI(), u.getId() }
                        );
                        return false;
                    }
                } else {
                    logger.log(Level.INFO, "State invalid. Unit id:{0} not found.", m.getId());
                    return false;
                }
            }
            /*for (Unit u : allUnits) {
                found = false;
                for (Message um : unitMessages) {
                    m = (UnitMessage) um;
                    if (u.getId() == m.getId()) {
                        found = true;
                        if (u.getOwnerId() != m.getOwnerId() || u.getJ() != m.getJ() || u.getI() != m.getI()) {
                            logger.log(
                                Level.INFO,
                                "State invalid.\nUnit id:{6}\nServer: ownerId:{0},j:{2},i:{4}\nClient: ownerId:{1},j:{3},i:{5}",
                                new Object[] { u.getOwnerId(), m.getOwnerId(), u.getJ(), m.getJ(), u.getI(), m.getI(), u.getId() }
                            );
                            return false;
                        } else {
                            break;
                        }
                    }
                }
                if (!found) {
                    logger.log(Level.INFO, "State invalid. Unit id:{0} not found.", u.getId());
                    return false;
                }
            }*/
        }

        return true;
    }

    public void removeUnits(LinkedList<Unit> units) {
        synchronized (allUnits) {
            for (Unit u : units) {
                allUnits.remove(u);
            }
        }
    }

    public Unit getById(Long unitId) {
        synchronized (allUnits) {
            for (Unit u : allUnits) {
                if (u.getId() == unitId) {
                    return u;
                }
            }
        }
        
        return null;
    }
}
