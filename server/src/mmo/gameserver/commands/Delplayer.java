package mmo.gameserver.commands;

public class Delplayer extends ResponseObjectCommand {
	private static final String COMMAND = "delpayer";
	
	private int id;
	
	public int getId() {
		return id;
	}
	
	public Delplayer(int id) {
		super(COMMAND);
		this.id = id;
	}
}
