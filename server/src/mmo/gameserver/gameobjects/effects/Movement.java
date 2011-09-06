package mmo.gameserver.gameobjects.effects;

import java.util.Queue;

import mmo.gameserver.gameobjects.Unit;
import mmo.gameserver.gameobjects.world.utils.Point;

/**
 * @author RzCoDer
 * Передвижение
 */
public class Movement extends TimeEffect {
	private Queue<Point> path;

	public Movement(Unit unit, Queue<Point> path) {
		super(unit);
		this.path = path;
	}
	
	private boolean nextMove() {
		if (path != null && !path.isEmpty()) {
			unit.setPosition(path.poll());
			setFinalized(isFinalized() || path.isEmpty());
		} else {
			setFinalized(true);
			return true;
		}			
		return false;
	}

	public void execute() {
		if(lastTime < 0) {
			lastTime = unit.getGameserver().getTime();
			nextMove();
		}
		
		boolean needTimeUpdate = false;
		long iterations = (unit.getGameserver().getTime() - lastTime) * unit.getSpeed() / 1000;
		for (int i = 0; i < iterations; i++) {
			if(nextMove()) break;
			needTimeUpdate = true;			
		}
		
		if(needTimeUpdate) {
			lastTime = unit.getGameserver().getTime() - (unit.getGameserver().getTime() - lastTime) * unit.getSpeed() % 1000;
		}
	}
}
