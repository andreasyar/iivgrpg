package mmo.gameserver.commands;

public class Delunit extends ResponseObjectCommand {
	private static final String COMMAND = "delunit";
	
	private int id;
	private UnitTypes type;	
	
	public int getId() {
		return id;
	}
	
	public UnitTypes getType() {
		return type;
	}
	
	public Delunit(UnitTypes type, int id) {
		super(COMMAND);
		this.type = type;
		this.id = id;
	}
}
