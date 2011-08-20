package mmo.gameserver.gameobjects;

public enum UnitTypes {
	PLAYER(1), NPC(2);
	
	private int id;
	
	public int getId() {
		return id;
	}
	
	private UnitTypes(int id)
	{
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.valueOf(getId());
	}
}
