package mmo.mmortsserver.movement;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmo.mmortsserver.units.Unit;

/**
 *
 * @author Sorc
 */
public class Movement {
    private static final Logger logger = Logger.getLogger(MovementManager.class.getName());
    
    private Unit unit;
    private int j;
    private int i;
    
    /**
     * Скорость движения в клетках за шаг.
     */
    private float speed;
    
    LinkedList<Point> path;
    private long begStep;
    private boolean stopped;

    public Movement(Unit unit, int j, int i, float speed, LinkedList<Point> path, long begStep) {
        this.unit = unit;
        this.j = j;
        this.i = i;
        this.speed = speed;
        this.path = path;
        this.begStep = begStep;
        this.stopped = false;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    void updatePosition(long step) {
        /*var n = Math.ceil((step - this.beg_step) * this.speed);
        if (n >= path.length - 1) {
            this.unit.j = this.path[path.length - 1].j
            this.unit.i = this.path[path.length - 1].i
            stopMovementCallback(this);
        } else {
            this.unit.j = this.path[n].j
            this.unit.i = this.path[n].i
        }*/
        int n = (int) Math.floor(((double) step - (double) begStep) * (double) speed);
        logger.log(Level.INFO, "{0} {1} {2} {3}", new Object[] {((double) step - (double) begStep), ((double) step - (double) begStep) * (double) speed, Math.floor(((double) step - (double) begStep) * (double) speed), n});
        if (n >= path.size()) {
            unit.setJ(path.get(path.size() - 1).getX());
            unit.setI(path.get(path.size() - 1).getY());
            stopped = true;
        } else {
            unit.setJ(path.get(n).getX());
            unit.setI(path.get(n).getY());
        }
    }

    public LinkedList<Point> getPath() {
        return path;
    }
}
