package mmo.gameserver.gameobjects.effects;

import mmo.gameserver.gameobjects.Unit;

/**
 * @author RzCoDer
 * Длительный эффект
 */
public abstract class TimeEffect extends ImplEffect {
	protected long lastTime;
	
	public TimeEffect(Unit unit) {
		super(unit);
		lastTime = -1; 
	}

	public abstract void execute();
}
