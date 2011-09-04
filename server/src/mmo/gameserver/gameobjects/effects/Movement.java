package mmo.gameserver.gameobjects.effects;

import java.util.List;

import mmo.gameserver.gameobjects.Unit;
import mmo.gameserver.gameobjects.world.utils.Point;

/**
 * @author RzCoDer
 * Передвижение
 */
public class Movement extends TimeEffect {
	private List<Point> path;

	public Movement(Unit unit, List<Point> path) {
		super(unit);
		this.path = path;
	}

	public void execute() {
		for (int i = 0; i < (unit.getGameserver().getTime() - lastTime) / 1000 * unit.getSpeed(); i++) {
			if (path != null && !path.isEmpty()) {
				unit.setPosition(path.get(0));
				path.remove(0);
				finalized = path.isEmpty();
			} else {
				finalized = true;
				break;
			}			
		}
	}
}
