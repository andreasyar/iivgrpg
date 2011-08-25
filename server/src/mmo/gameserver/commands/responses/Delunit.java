package mmo.gameserver.commands.responses;

public class Delunit extends ResponseObjectCommand {
	private static final String COMMAND = "delunit";
	
	private int id;
	
	public int getId() {
		return id;
	}

	public Delunit(int id) {
		super(COMMAND);
		this.id = id;
	}
}
