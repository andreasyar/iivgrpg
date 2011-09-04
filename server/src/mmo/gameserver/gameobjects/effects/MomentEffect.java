package mmo.gameserver.gameobjects.effects;

import mmo.gameserver.gameobjects.Unit;

/**
 * @author RzCoDer
 * Единоразовый моментальный эффект
 */
public abstract class MomentEffect extends ImplEffect {

	public MomentEffect(Unit unit) {
		super(unit);
	}

	public abstract void execute();
}
