package mmo.gameserver.gameobjects.effects;

import mmo.gameserver.gameobjects.Unit;

public abstract class ImplEffect implements Effect, Comparable<ImplEffect> {
	private static long effectCounter;
	
	private long effectId;
	
	protected Unit unit;
	protected boolean finalized;
	
	public ImplEffect(Unit unit) {
		this.unit = unit;
		this.finalized = false;
		effectId = effectCounter++;
	}
	
	public abstract void execute();
	
	public boolean isFinalized() {
		return this.finalized;
	}
	
	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}	

	@Override
	public int compareTo(ImplEffect ef) {		
		return (this.effectId == ef.effectId) ? 0 : (this.effectId < ef.effectId) ? -1 : 1;
	}
	
	
	
}
